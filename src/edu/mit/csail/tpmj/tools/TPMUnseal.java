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

public class TPMUnseal
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMUnseal\n"
            + "           <dataFile> <keyHandle> <keyPwd> <dataPwd>\n\n"
            + "Inputs:\n"
            + "  dataFile - file name (without .sealed extension)\n"
            + "  keyHandle - handle of loaded storage key (or \"SRK\")\n"
            + "  keyPwd - key password\n"
            + "  dataPwd - data password\n"
            + "Output:\n"
            + "  file <dataFile>.unsealed containing the encrypted form of the data" );
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
            "keyPwd", "dataPwd" );

        // filename
        String fileName = params.getString( "dataFile" );
        String rootName = fileName;
        if ( rootName.toLowerCase().endsWith( ".sealed" ) )
        {
            rootName = rootName.substring( 0, rootName.length() - 7 );
        }
        else
        {
            fileName = fileName + ".sealed";
        }

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
            System.out.println( "\n*** Reading SealedData from file " + fileName + " *** ... " );
            byte[] encDataBytes = FileUtil.readIntoByteArray( fileName );
            TPM_STORED_DATA encData = new TPM_STORED_DATA( encDataBytes );
            System.out.println( "Encrypted data structure =\n" + encData );

            System.out.println( "\n*** Unsealing *** ..." );
            byte[] unencData = TPMStorageFuncs.TPM_Unseal( keyHandle, keyAuth, encData, dataAuth );
            System.out.println( ByteArrayUtil.toPrintableHexString( unencData ) );

            String outDataFile = rootName + ".unsealed";
            System.out.println( "\n*** Writing to file " + outDataFile + " *** ..." );
            FileUtil.writeByteArray( outDataFile, unencData );
            System.out.println( "DONE." );
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
