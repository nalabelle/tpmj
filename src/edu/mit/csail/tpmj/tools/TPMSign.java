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
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

public class TPMSign
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMSign\n"
            + "           <fileName> <keyHandle> [keyPwd]\n\n"
            + "Output:\n"
            + "  Binary file <filename>.sig, containing the signature\n" 
            + "  of the SHA-1 hash of the file contents" );
    }

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
            return;
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "fileName", "keyHandle", "keyPwd" );

        // filename
        String fileName = params.getString( "fileName" );

        int keyHandle = params.getInt( "keyHandle" );
        
        // key authorization
        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "keyPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "\n*** Reading Data *** ... " );
            byte[] dataBytes = FileUtil.readIntoByteArray( fileName );

            byte[] sig = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle,
                dataBytes, keyAuth );
            System.out.println( "Signature returned (" + sig.length
                + " bytes): " + ByteArrayUtil.toPrintableHexString( sig ) );
            
            String outFileName = fileName + ".sig";
            System.out.println( "Writing signature to file " + outFileName );
            FileUtil.writeByteArray( outFileName, sig );
            System.out.println( "DONE." );
            

            System.out.println( "\n*** Verifying signature by software using public key ... " );
//            System.out.println( "\n*** Getting the public key via TPM_GetPubKey ... " );
            TPM_PUBKEY pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle,
                keyAuth );
//            System.out.println( "PubKey=" + pubKey );
            // NOTE: this uses helloWorld not helloWorldHash 
            // since verifySignature already uses SHA1withRSA
            boolean signOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature(
                pubKey, sig, dataBytes );
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
