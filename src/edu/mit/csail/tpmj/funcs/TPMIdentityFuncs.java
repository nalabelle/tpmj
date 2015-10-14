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

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.interfaces.RSAPrivateCrtKey;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.tests.TPMPcrTest;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMIdentityFuncs extends TPMUtilityFuncs
{
    public static TPM_MakeIdentityOutput TPM_CreateAIK( TPM_SECRET ownerAuth,
        TPM_SECRET srkAuth, TPM_SECRET plainIdentityAuth,
        TPM_DIGEST labelPrivCADigest, TPM_KEY keyInfo ) throws TPMException
    {
        TPM_MakeIdentity cmd = new TPM_MakeIdentity( plainIdentityAuth,
            labelPrivCADigest, keyInfo );

        // FIXME: I'm not sure if this is necessary.  It's not clear what happens if there is no SRK auth.
        if ( srkAuth == null )
        {
            srkAuth = TPM_SECRET.NULL;
        }

        TPMOIAPSession srkSession = new TPMOIAPSession( tpmDriver );
        srkSession.startSession();
        srkSession.setSharedSecret( srkAuth );

        short ownerEntityType = TPMConsts.TPM_ET_OWNER;
        int ownerEntityValue = TPMConsts.TPM_KH_OWNER;

        TPMOSAPSession ownerSession = new TPMOSAPSession( tpmDriver );
        ownerSession.startSession( ownerEntityType, ownerEntityValue, ownerAuth );

        TPM_MakeIdentityOutput output = (TPM_MakeIdentityOutput) cmd.execute(
            srkSession, false, ownerSession, false );

        return output;
    }

    public static TPM_KEY createAIKKeyInfo( int keyUsageFlags,
        byte authDataUsage )
    {
        TPM_KEY keyInfo = new TPM_KEY();

        keyInfo.setKeyFlags( keyUsageFlags );
        keyInfo.setAuthDataUsage( authDataUsage );

        TPM_KEY_PARMS keyParms = new TPM_KEY_PARMS();
        keyParms.setAlgorithmID( TPMConsts.TPM_ALG_RSA );
        keyInfo.setKeyUsage( TPMConsts.TPM_KEY_IDENTITY );
        // Note: copied settings for TPM_KEY_SIGNING
        keyParms.setEncScheme( TPMConsts.TPM_ES_NONE );
        keyParms.setSigScheme( TPMConsts.TPM_SS_RSASSAPKCS1v15_SHA1 );
        TPM_RSA_KEY_PARMS rsaKeyParms = new TPM_RSA_KEY_PARMS( 2048, 2,
            new byte[0] );
        keyParms.setParmData( rsaKeyParms );
        keyInfo.setAlgorithmParms( keyParms );

        return keyInfo;
    }

    public static TPM_MakeIdentityOutput TPM_CreateAIK( TPM_SECRET ownerAuth,
        TPM_SECRET srkAuth, TPM_SECRET plainIdentityAuth,
        TPM_DIGEST labelPrivCADigest ) throws TPMException
    {
        byte authDataUsage = (plainIdentityAuth == null) ? (byte) 0 : (byte) 1;
        int keyUsageFlags = 0;
        TPM_KEY keyInfo = createAIKKeyInfo( keyUsageFlags, authDataUsage );
        return TPM_CreateAIK( ownerAuth, srkAuth, plainIdentityAuth,
            labelPrivCADigest, keyInfo );
    }
}
