/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of MIT nor the names of its contributors may be used 
 *    to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * Original author:  Luis F. G. Sarmenta, MIT, 2007
 */
package edu.mit.csail.tpmj.tools;

import java.io.*;
import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

public class TPMSeal
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMSeal\n"
            + "           <dataFile> <keyHandle> <keyPwd> <dataPwd> [pcrNums...]\n\n"
            + "Inputs:\n"
            + "  dataFile - file with the original data\n"
            + "  keyHandle - handle of loaded storage key (or \"SRK\")\n"
            + "  keyPwd - key password\n"
            + "  dataPwd - data password\n"
            + "  pcrNums - space delimited list of pcr IDs to seal the data to\n"
            + "            (omit to seal independent of PCRs; use \"all\" to seal to all PCRs)\n"
            + "Output:\n"
            + "  file <dataFile>.sealed containing the encrypted form of the data" );
    }

    public static void main( String[] args )
    {
        if ( args.length < 4 )
        {
            usage();
            return;
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "dataFile", "keyHandle",
            "keyPwd", "dataPwd", "pcrNums" );

        // filename
        String fileName = params.getString( "dataFile" );

        int keyHandle = TPMConsts.TPM_KH_SRK;
        String keyHandleString = params.getString( "keyHandle" );
        if ( (keyHandleString == null)
            || "srk".equalsIgnoreCase( keyHandleString ) )
        {
            System.out.println( "Using SRK to seal." );
            keyHandle = TPMConsts.TPM_KH_SRK;
        }
        else
        {
            keyHandle = params.getInt( "keyHandle" );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );
        }

        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "keyPwd" );
        TPM_SECRET dataAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "dataPwd" );


        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "\n*** Reading Data *** ... " );
            byte[] text = FileUtil.readIntoByteArray( fileName );
            System.out.println( ByteArrayUtil.toPrintableHexString( text ) );

            System.out.println( "\n*** Reading PCR configuation to seal data to *** ... " );

            // NOTE: need to parse PCR list after starting driver because we need to call getNumPcrs
            TPM_PCR_COMPOSITE pcrComposite = null;
            if ( args.length > 4 )
            {
                int[] pcrNums = null;
                if ( args[4].toLowerCase().equals( "all" ) ) 
                    {
                        pcrNums = new int[TPMPcrFuncs.getNumPcrs()];
                        for ( int i = 0; i < pcrNums.length; i++ )
                        {
                            pcrNums[i] = i;
                        }
                    }
                else
                {
                    pcrNums = new int[args.length - 4];
                    int actualNums = 0;

                    System.out.print( "PCRs: " );
                    for ( int i = 0; i < pcrNums.length; i++ )
                    {
                        try
                        {
                            pcrNums[actualNums] = Integer.parseInt( args[i + 4] );
                            System.out.print( pcrNums[actualNums] + " " );
                            actualNums++;
                        }
                        catch ( NumberFormatException e )
                        {
                            // System.out.println( "Argument " + args[i + 4] + " is not a valid integer." );
                        }
                    }
                    if ( actualNums != pcrNums.length )
                    {
                        int[] newArr = new int[actualNums];
                        System.arraycopy( pcrNums, 0, newArr, 0, newArr.length );
                        pcrNums = newArr;
                    }
                    System.out.println();
                }
                pcrComposite = TPMPcrFuncs.readPCRsIntoComposite( pcrNums );
            }
            
            if ( pcrComposite == null )
            {
                System.out.println( "PCRs will NOT be used as a condition for unsealing" );
            }
            else
            {
                System.out.println( pcrComposite );
                System.out.println( "digest: " + CryptoUtil.computeTPM_DIGEST( pcrComposite ) );
            }
            
            System.out.println( "\n*** Sealing Data *** ... " );
            TPM_STORED_DATA encData = TPMStorageFuncs.TPM_Seal( keyHandle, keyAuth, dataAuth, text, pcrComposite );

            System.out.println( "Encrypted data structure =\n" + encData );

            String encDataFile = fileName + ".sealed";
            System.out.println( "Writing to file " + encDataFile );
            FileUtil.writeByteArray( encDataFile, encData.toBytes() );
            System.out.println( "DONE." );

            System.out.println( "\n*** Testing Unseal *** ..." );
            byte[] unencData = TPMStorageFuncs.TPM_Unseal( keyHandle, keyAuth, encData, dataAuth );
            System.out.println( ByteArrayUtil.toPrintableHexString( unencData ) );

            
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( IOException ioe )
        {
            System.out.println( "IOException " + ioe );
            ioe.printStackTrace();
        }

        TPMToolsUtil.cleanupTPMDriver();
    }
}
