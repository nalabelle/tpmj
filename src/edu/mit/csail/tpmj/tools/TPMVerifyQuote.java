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

public class TPMVerifyQuote
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMVerifyQuote <fileName> <keyFile>\n\n"
            + "Inputs:\n"
            + "  fileName - name of .quot and .sig file pair to be verified\n" 
            + "  keyFile - .key or .pubkey file containing a TPM_KEY or TPM_STORE_PUBKEY structure" );
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
        SwitchParams params = new SwitchParams( args, "fileName", "keyFile" );

        // filename
        String fileName = params.getString( "fileName" );
        String keyFile = params.getString( "keyFile" );

        // get external data from hash of fileName
        TPM_NONCE externalData = TPMToolsUtil.convertAuthString( fileName, "nonce (external data)" );
        
        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            String quoteFileName = fileName + ".quot";
            System.out.println( "\n*** Reading Quote Data from " + quoteFileName + " *** ... " );
            byte[] pcrBytes = FileUtil.readIntoByteArray( quoteFileName );
            TPM_PCR_COMPOSITE pcrComposite = new TPM_PCR_COMPOSITE( pcrBytes );
            System.out.println( pcrComposite );
            
            String sigFileName = fileName + ".sig";
            System.out.println( "\n*** Reading Signature from " + sigFileName + " *** ... " );
            byte[] sigBytes = FileUtil.readIntoByteArray( sigFileName );
            System.out.println( ByteArrayUtil.toPrintableHexString( sigBytes ) );
            
            System.out.println( "\n*** Getting the public key from " + keyFile + " ... " );

            byte[] keyBlob = FileUtil.readIntoByteArray( keyFile );

            short tag = ByteArrayUtil.readShortBE( keyBlob, 0 );

            TPM_STORE_PUBKEY pubKey = null;
            
            if ( (tag == TPMConsts.TPM_TAG_KEY12) || (tag == 0x0101) )
            {
                // data is a TPM_KEY12 or TPM_KEY structure
                TPM_KEY parentKey = new TPM_KEY( keyBlob );
                pubKey = parentKey.getPubKey();
            }
            else
            {
                pubKey = new TPM_STORE_PUBKEY();
                pubKey.fromBytes( keyBlob, 0 );
            }
            
            byte[] pubKeyBytes = pubKey.getKeyBytes();
            
            System.out.println( "Public key bytes (" + pubKeyBytes.length + " bytes) =\n" 
                + ByteArrayUtil.toPrintableHexString( pubKeyBytes ) );

            System.out.println( "\n*** Verifying signature on quote by software using public key ... " );

            boolean signOK = TPMPcrFuncs.verifyQuote( pubKeyBytes, pcrComposite, sigBytes, externalData );
            System.out.println( "Signature OK? " + signOK );

        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }

        TPMToolsUtil.cleanupTPMDriver();
    }

}
