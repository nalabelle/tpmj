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

public class TPMQuote
{
    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMQuote\n"
            + "           <fileName> <keyHandle> [keyPwd]\n\n"
            + "Inputs:\n"
            + "  fileName - root file name for output files to be generated\n" 
            + "    This fileName is also hashed to produce the nonce used for the quote.\n"
            + "  keyHandle - handle of loaded signing key or AIK\n" 
            + "  keyPwd - password for key (default to no authorization)\n\n"
            + "Outputs:\n"
            + "  <fileName>.quot - file containing TPM_PCR_COMPOSITE structure with PCR values\n" 
            + "  <fileName>.sig - file containing signature file on TPM_QUOTE_INFO\n" 
            + "      which can be verified by using tools.TPMVerifyQuote" );
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

        // get external data from hash of fileName
        TPM_NONCE externalData = TPMToolsUtil.convertAuthString( fileName, "nonce (external data)" );
        
        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "\n*** Getting Quote *** ... " );

            TPM_QuoteOutput quoteOut = TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, externalData );
            
            System.out.println( "DONE.\n" 
                + "Output=" + quoteOut  );
            TPM_PCR_COMPOSITE pcrData = quoteOut.getPcrData(); 
            byte[] pcrBytes = pcrData.toBytes();
            byte[] sig = quoteOut.getSig();
            
            String quoteFileName = fileName + ".quot";
            System.out.println( "\nWriting signature to file " + quoteFileName );
            FileUtil.writeByteArray( quoteFileName, pcrBytes );
            System.out.println( "DONE." );

            String sigFileName = fileName + ".sig";
            System.out.println( "\nWriting signature to file " + sigFileName );
            FileUtil.writeByteArray( sigFileName, sig );
            System.out.println( "DONE." );
            
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
