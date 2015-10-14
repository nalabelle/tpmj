/*
 * Copyright (c) 2006,2007 Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006-2007
 */
package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateWrapKey;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMStorageFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_KEY;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMLoadKey
{

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMLoadKey <fileName> [parentHandle] [parentPwd]\n\n"
            + "- parentHandle should be the handle number of the loaded parent key\n"
            + "  Use \"SRK\" for the SRK.  Default is SRK with no authorization.\n\n"
            + "- If parentPwd is not given, null password with no authorization is assumed." );
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
        SwitchParams params = new SwitchParams( args, "fileName", "parentHandle", "parentPwd" );

        String fileName = params.getString( "fileName" );

        int parentHandle = TPMConsts.TPM_KH_SRK;
        String parentHandleString = params.getString( "parentHandle" );
        if ( (parentHandleString == null) || "srk".equalsIgnoreCase( parentHandleString ) )
        {
            System.out.println( "Using SRK as parent." );
            parentHandle = TPMConsts.TPM_KH_SRK;
        }
        else
        {
            parentHandle = params.getInt( "parentHandle" );
            System.out.println( "parentHandle = 0x"
                + Integer.toHexString( parentHandle ) );
        }

        TPM_SECRET parentAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "parentPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "\nRead " + fileName + " ..." );
            byte[] blob = FileUtil.readIntoByteArray( fileName );

            System.out.println( "\nLoading the key into the TPM ..." );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( parentHandle, blob,
                parentAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );
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
