/*
 * Copyright (c) 2006, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006
 */
package edu.mit.csail.tpmj.tests;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

/** 
 * This class tests running various commands surrounded in OIAP and OSAP sessions.
 *
 */
public class TPMAuthTest
{
    public void run( int keyHandle, String password )
    {
        //        try
        //        {
        //            TPMDriver tpmDriver = TPMUtilityFuncs.getTpmDriver();
        //            TPM_Reset resetCmd = new TPM_Reset();
        //            resetCmd.execute( tpmDriver );
        //
        //            Debug.println( "Trying GetPubKey with no auth..." );
        //            TPMInputStruct cmd0 = new TPMInputStruct();
        //            cmd0.setParamSize( 14 );
        //            cmd0.setTag( TPMConsts.TPM_TAG_RQU_COMMAND );
        //            cmd0.setOrdinal( TPMConsts.TPM_ORD_GetPubKey );
        //            cmd0.setInt32( TPM_GetPubKey.KEYHANDLE_OFFSET, keyHandle );
        //            TPMOutputStruct out0 = tpmDriver.transmit( cmd0 );
        //            Debug.println( "Output: " + out0 );
        //        }
        //        catch ( TPMException e )
        //        {
        //            TPMDemo.handleTPMException( e );
        //        }

        try
        {
            Debug.println( "Trying OIAP Session ... " );
            Debug.println( "Trying GetPubKey ( 0x"
                + Integer.toHexString( keyHandle ) + " with auth..." );

            TPMDriver tpmDriver = TPMUtilityFuncs.getTPMDriver();

            // FIXME: Commenting this out, since TPM_Reset seems to cause problems 
            // in both Mac OS X and Windows Vista
//            TPM_Reset resetCmd = new TPM_Reset();
//            resetCmd.execute( tpmDriver );

            TPM_GetPubKey cmd = new TPM_GetPubKey( keyHandle );

            TPM_SECRET secret = TPM_SECRET.NULL;
            if ( password != null )
            {
                secret = TPMToolsUtil.createTPM_SECRETFromPrefixedString( password );
            }

            TPM_GetPubKeyOutput output = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, secret );

            System.out.println( "\nPubKey: " + output.getPubKey() );

            Debug.println( "\n***********************" );
            Debug.println( "Trying OSAP Session ... " );
            Debug.println( "Trying GetPubKey ( 0x"
                + Integer.toHexString( keyHandle ) + " with auth..." );

            // FIXME: Commenting this out, since TPM_Reset seems to cause problems 
            // in both Mac OS X and Windows Vista
//            // note: we can reuse a command since it's just data
//            resetCmd.execute( tpmDriver );

            // Note: we can actually reuse cmd2 here, and it works (tested)
            // But that is discouraged since you would actually be overwriting
            // old auth data.  It works since the old auth data is overwritten, 
            // but it is "messy".
            TPM_GetPubKey cmd2 = new TPM_GetPubKey( keyHandle );

            output = (TPM_GetPubKeyOutput) TPMOSAPSession.executeKeyOSAPSession(
                tpmDriver, cmd2, secret );

            System.out.println( "PubKey: " + output.getPubKey() );

            System.out.println( "Trying explicit OSAPSession using cmd.execute( ... ) ..." );
            TPMOSAPSession osapSession2 = new TPMOSAPSession( tpmDriver );
            short entityType = TPMConsts.TPM_ET_KEYHANDLE;
            int entityValue = keyHandle;
            osapSession2.startSession( entityType, entityValue, secret );
            TPM_GetPubKey cmd3 = new TPM_GetPubKey( keyHandle );
            output = cmd3.execute( osapSession2, false );
            System.out.println( "PubKey: " + output.getPubKey() );

            System.out.println( "\nTrying GetPubKey with no authorization (should only work if key was created without authorization) ..." );

            cmd = new TPM_GetPubKey( keyHandle );
            cmd.setNoAuth();
            output = (TPM_GetPubKeyOutput) cmd.execute( tpmDriver );
            System.out.println( "PubKey: " + output.getPubKey() );

            System.out.println( "\nTrying it within an OIAP Session ..." );
            output = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, secret );
            System.out.println( "PubKey: " + output.getPubKey() );

            System.out.println( "\nTrying OIAPSession with wrong secret ..." );
            byte[] wrongSecretBytes = new byte[20];
            wrongSecretBytes[0] = 0x01;
            TPM_SECRET wrongSecret = new TPM_SECRET( wrongSecretBytes );

            cmd = new TPM_GetPubKey( keyHandle );
            TPM_GetPubKeyOutput outputWrong = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, wrongSecret );
            System.out.println( "PubKey: " + outputWrong.getPubKey() );

        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

    }

    public static void main( String[] args )
    {
        Debug.setThisClassDebugOn( true );
        Debug.setDebugOn( TPMOIAPSession.class, true );
        Debug.setDebugOn( TPMOSAPSession.class, true );
        Debug.setDebugOn( TPMAuthorizationSession.class, true );

        TPMUtilityFuncs.initTPMDriver();

        TPMAuthTest demo = new TPMAuthTest();

        int keyHandle = TPMConsts.TPM_KH_SRK;
        String password = null;

        if ( args.length > 0 )
        {
            if ( args[0].startsWith( "0x" ) )
            {
                args[0] = args[0].substring( 2 );
            }
            keyHandle = Integer.parseInt( args[0], 16 );
        }
        if ( args.length > 1 )
        {
            password = args[1];
        }

        demo.run( keyHandle, password );
        
        TPMUtilityFuncs.cleanupTPMDriver();
    }

}
