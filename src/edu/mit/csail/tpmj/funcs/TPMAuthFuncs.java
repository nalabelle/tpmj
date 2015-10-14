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
package edu.mit.csail.tpmj.funcs;

import java.io.IOException;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.tests.TPMKeyTest;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMAuthFuncs extends TPMUtilityFuncs
{
    public static byte[] TPM_ChangeAuth( int parentHandle, TPM_SECRET parentAuth,
        short protocolID, TPM_SECRET plainOldAuth, TPM_SECRET plainNewAuth,
        short entityType, byte[] blob ) throws TPMException
    {
        if ( parentAuth == null || plainOldAuth == null || plainNewAuth == null )
        {
            throw new IllegalArgumentException(
                "Parent and old and new child auths can't be null" );
        }

        TPM_ChangeAuth cmd = new TPM_ChangeAuth( parentHandle, protocolID,
            plainNewAuth, entityType, blob );

        // Note: the IBM code uses TPM_ET_KEYHANDLE even if the parent is the SRK
        // Note that we don't have to call getKeyHandleEntityType here since
        // whether or not we are using Infineon, we will use TPMConsts.TPM_ET_KEYHANDLE
        // anyway for the SRK.

        short parentEntityType = TPMConsts.TPM_ET_KEYHANDLE;
        int parentEntityValue = parentHandle;

        TPMOSAPSession parentSession = new TPMOSAPSession( tpmDriver );
        parentSession.startSession( parentEntityType, parentEntityValue,
            parentAuth );

        TPMOIAPSession entitySession = new TPMOIAPSession( tpmDriver );
        entitySession.startSession();
        entitySession.setSharedSecret( plainOldAuth );

        TPM_ChangeAuthOutput output = (TPM_ChangeAuthOutput) cmd.execute(
            parentSession, false, entitySession, false );
        
        return output.getOutData();
    }

    /**
     * Takes a key, calls TPM_ChangeAuth, and replaces the encDataBytes
     * with the new encrypted data with the new key.
     * 
     * @param parentHandle
     * @param parentAuth
     * @param plainOldAuth
     * @param plainNewAuth
     * @param key
     * @throws TPMException
     */
    public static void TPM_ChangeKeyAuth( int parentHandle,
        TPM_SECRET parentAuth, TPM_SECRET plainOldAuth,
        TPM_SECRET plainNewAuth, TPM_KEY key ) throws TPMException
    {
        byte[] keyEncData = key.getEncDataBytes();
        byte[] newEncData = TPM_ChangeAuth( parentHandle, parentAuth, TPMConsts.TPM_PID_ADCP,
            plainOldAuth, plainNewAuth, TPMConsts.TPM_ET_KEY, keyEncData );
        key.setEncDataBytes( newEncData );
    }
    
    public static void TPM_ChangeOwnerAuth( TPM_SECRET oldAuth,
        TPM_SECRET newAuth ) throws TPMException
    {
        if ( oldAuth == null || newAuth == null )
        {
            throw new IllegalArgumentException( "owner auth can't be null" );
        }

        TPM_ChangeAuthOwner cmd = new TPM_ChangeAuthOwner(
            TPMConsts.TPM_PID_ADCP, newAuth, TPMConsts.TPM_ET_OWNER );

        // NOTE: IBM's tpm-3.0.3 seems to use 0 for the entity value here, not TPM_KH_OWNER
        // However, using TPM_KH_OWNER seems to work fine here for both 
        // the Broadcom 1.2 and Infineon 1.1 chips
        TPM_ChangeAuthOwnerOutput output = (TPM_ChangeAuthOwnerOutput) TPMOSAPSession.executeOSAPSession(
            tpmDriver, cmd, TPMConsts.TPM_ET_OWNER, TPMConsts.TPM_KH_OWNER,
            oldAuth );
    }

    public static void TPM_ChangeSRKAuth( TPM_SECRET ownerAuth,
        TPM_SECRET newSRKAuth ) throws TPMException
    {
        if ( ownerAuth == null )
        {
            throw new IllegalArgumentException( "owner auth can't be null" );
        }
        if ( newSRKAuth == null )
        {
            newSRKAuth = TPM_SECRET.NULL;
        }

        TPM_ChangeAuthOwner cmd = new TPM_ChangeAuthOwner(
            TPMConsts.TPM_PID_ADCP, newSRKAuth, TPMConsts.TPM_ET_SRK );

        // NOTE: IBM's tpm-3.0.3 seems to use 0 for the entity value here, and an entity type of TPM_ET_OWNER
        // not TPM_ET_SRK
        // I'm continuing to use TPMConsts.TPM_KH_OWNER here since the authorization
        // is based on owner authorization.
        TPM_ChangeAuthOwnerOutput output = (TPM_ChangeAuthOwnerOutput) TPMOSAPSession.executeOSAPSession(
            tpmDriver, cmd, TPMConsts.TPM_ET_OWNER, TPMConsts.TPM_KH_OWNER,
            ownerAuth );
    }

    // Unit test

    public static void main( String[] args )
    {
        Debug.setDebugOn( TPMDriver.class, true );
        Debug.setDebugOn( TPMAuthorizationSession.class, true );
        
//        try
//        {
//            System.out.println( "Changing owner password ..." );
//            TPM_SECRET oldAuth = CryptoUtil.createTPM_SECRETFromStringInfineonStyle( "tpmowner" );
//            TPM_SECRET newAuth = CryptoUtil.createTPM_SECRETFromString( "tpmowner" );
//            TPMAuthFuncs.TPM_ChangeOwnerAuth( oldAuth, newAuth );
//            System.out.println( "Done." );
//        }
//        catch ( TPMException e )
//        {
//            TPMDemo.handleTPMException( e );
//        }
//
//        try
//        {
//            System.out.println( "Loading key using no authorization ... " );
//            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
//                "testkey.key", null );
//            System.out.println( "keyHandle=0x"
//                + Integer.toHexString( keyHandle ) );
//
//            System.out.println( "Changing SRK password ..." );
//            TPM_SECRET ownerAuth = CryptoUtil.createTPM_SECRETFromString( "tpmowner" );
////            TPM_SECRET ownerAuth = CryptoUtil.createTPM_SECRETFromStringInfineonStyle( "tpmowner" );
//            TPM_SECRET newSRKAuth = TPM_SECRET.NULL; // CryptoUtil.createTPM_SECRETFromStringInfineonStyle( "srkpass" );
//            TPMAuthFuncs.TPM_ChangeSRKAuth( ownerAuth, newSRKAuth );
//            System.out.println( "Done." );
//
//            System.out.println( "Loading key using no authorization ... " );
//            keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
//                "testkey.key", null );
//            System.out.println( "keyHandle=0x"
//                + Integer.toHexString( keyHandle ) );
//
//            System.out.println( "Loading key using new authorization ... " );
//            keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
//                "testkey.key", newSRKAuth );
//            System.out.println( "keyHandle=0x"
//                + Integer.toHexString( keyHandle ) );
//
//            System.out.println( "Loading key using NULL authorization ... " );
//            keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
//                "testkey.key", TPM_SECRET.NULL );
//            System.out.println( "keyHandle=0x"
//                + Integer.toHexString( keyHandle ) );
//
//        }
//        catch ( TPMException e )
//        {
//            TPMDemo.handleTPMException( e );
//        }
//        catch ( IOException e )
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        
//        TPMEvictKey.main( args );
//
        try
        {
            TPMUtilityFuncs.initTPMDriver();

            System.out.println( "Changing key password ..." );
            byte[] keyBlob = FileUtil.readIntoByteArray( "testkey.key" );
            TPM_SECRET parentAuth = TPM_SECRET.NULL;
            TPM_SECRET oldAuth = TPMToolsUtil.convertAuthString( "test", "oldAuth" );
            TPM_SECRET newAuth = TPMToolsUtil.convertAuthString( "test2", "newAuth" );
            int parentHandle = TPMConsts.TPM_KH_SRK;
            
            TPM_KEY key = new TPM_KEY( keyBlob );
            TPMAuthFuncs.TPM_ChangeKeyAuth(parentHandle, parentAuth, oldAuth, newAuth, key );
            FileUtil.writeByteArray( "testkey2.key", key.toBytes() );

            // NOTE: Cleanup here because TPMKeyTest.main has its own init and cleanup
            TPMUtilityFuncs.cleanupTPMDriver();

            // NOTE: TPMKeyTest.main has its own init and cleanup for the TPM driver.
            
            System.out.println( "\n**** Testing new key with new password **** " );
            String[] testArgs = { "testkey2.key", "test2" };
            TPMKeyTest.main( testArgs );

            System.out.println( "\n**** Testing old key with old password **** " );
            String[] testArgs2 = { "testkey.key", "test" };
            TPMKeyTest.main( testArgs2 );
            
            
            System.out.println( "Done." );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( IOException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
