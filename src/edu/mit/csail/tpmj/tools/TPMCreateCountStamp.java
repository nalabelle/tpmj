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
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.counters.CountStamp;
import edu.mit.csail.tpmj.counters.CountStampFuncs;
import edu.mit.csail.tpmj.counters.TPMCountStamp;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

public class TPMCreateCountStamp
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMCreateCountStamp\n"
            + "           <fileName> <R|I> <counterID> <counterPwd> <keyHandle> [keyPwd]\n\n"
            + "Note: for all zeroes counterPwd, use \"\"\n\n"
            + "Output:\n"
            + "  Binary file <filename>.cntstmp, containing the TPMCountStamp\n"
            + "  of the SHA-1 hash of the file contents" );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        if ( args.length < 5 )
        {
            usage();
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "fileName", "opType",
            "counterID", "counterPwd", "keyHandle", "keyPwd" );

        // filename
        String fileName = params.getString( "fileName" );

        int opType = 0;
        char opTypeCh = params.getString( "opType" ).toUpperCase().charAt( 0 );
        switch ( opTypeCh )
        {
            case 'R':
                opType = TPMConsts.TPM_ORD_ReadCounter;
                break;
            case 'I':
                opType = TPMConsts.TPM_ORD_IncrementCounter;
                break;
            default:
                usage();
        }

        int countID = params.getInt( "counterID" );

        // <counter password>
        TPM_SECRET counterAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "counterPwd" );

        if ( counterAuth == null )
        {
            counterAuth = TPM_SECRET.NULL;
            System.out.println( "Using NULL (all zeroes) as counterAuth" );
        }

        int keyHandle = params.getInt( "keyHandle" );

        // key authorization
        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "keyPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "\n*** Reading Data *** ... " );
            byte[] dataBytes = FileUtil.readIntoByteArray( fileName );

            CountStamp countStamp = CountStampFuncs.createCountStamp(
                dataBytes, opType, countID, counterAuth, keyHandle, keyAuth );

            String outFileName = fileName + ".cntstmp";
            System.out.println( "Writing CountStamp to file " + outFileName );
            FileUtil.writeByteArray( outFileName, countStamp.toBytes() );
            System.out.println( "DONE." );

            System.out.println( "\n" );

            System.out.println( "Reading CountStamp to verify it ... " );

            byte[] buf = FileUtil.readIntoByteArray( outFileName );
            TPMCountStamp cs2 = new TPMCountStamp( buf, 0 );

            System.out.println( "CountStamp: " + cs2 );

            System.out.println( "\n*** Verifying signature by software using public key ... " );
            //            System.out.println( "\n*** Getting the public key via TPM_GetPubKey ... " );
            TPM_PUBKEY pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle,
                keyAuth );
            boolean signOK = CountStampFuncs.verifyCountStamp( dataBytes, cs2,
                pubKey.getPubKey().getKeyBytes() );
            System.out.println( "Signature OK? " + signOK );

        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }

        TPMToolsUtil.cleanupTPMDriver();
    }

}
