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

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPMOSAPSession extends TPMAuthorizationSession
{
    /**
     * Creates a session object.
     * Doesn't connect yet.  Call startSession to connect.
     *  
     * @param tpmDriver
     */
    public TPMOSAPSession( TPMDriver tpmDriver )
    {
        super( tpmDriver );
    }

    public int startSession( short entityType, int entityValue,
        TPM_SECRET keyUsageAuth ) throws TPMException
    {
        // FIXME: Figure out the right way of replacing sharedSecret with all zeros secret if sharedSecret is null
        if ( keyUsageAuth == null )
        {
            // NOTE: If secret is null, replace with zero secret
            keyUsageAuth = TPM_SECRET.NULL;
        }
        
        if ( this.isActive() )
        {
            throw new TPMAuthSessionStateException(
                "Attempt to startSession on an active OSAP session." );
        }
        // TODO: Call TPM_Reset() to be safe ?

        Debug.println( "Calling TPM_OSAP()" );

        // generate nonceOddOSAP
        TPM_NONCE nonceOddOSAP = CryptoUtil.generateRandomNonce();

        TPM_OSAP cmd = new TPM_OSAP( entityType, entityValue, nonceOddOSAP );
        TPM_OSAPOutput osapOut = (TPM_OSAPOutput) cmd.execute( tpmDriver );

        // generate shared secret
        TPM_NONCE nonceEvenOSAP = osapOut.getNonceEvenOSAP();
        TPM_SECRET generatedSecret = this.generateSharedSecret( keyUsageAuth, nonceEvenOSAP, nonceOddOSAP ); 
        this.setSharedSecret( generatedSecret );
        Debug.println( "nonceEvenOSAP= ", nonceEvenOSAP, "\ngenerated sharedSecret= ", generatedSecret );

        // This part is the same as in TPMOIAPSession except for getting the data
        // from an osapOut object.

        // Save authHandle, authLastNonceEven
        this.initialize( osapOut.getAuthHandle(), osapOut.getNonceEven() );

        return this.getAuthHandle();
    }

    protected TPM_SECRET generateSharedSecret( TPM_SECRET keyUsageAuth,
        TPM_NONCE nonceEvenOSAP, TPM_NONCE nonceOddOSAP )
    {
        // FIXME: Figure out right way of dealing with null secrets
        if ( keyUsageAuth == null )
        {
            // NOTE: This should never happen because keyUsageAuth is already
            // checked for null in startSession
            keyUsageAuth = TPM_SECRET.NULL;
        }
        
        return CryptoUtil.computeHMAC_TPM_DIGEST(
            keyUsageAuth, nonceEvenOSAP, nonceOddOSAP );
    }
    
//    /**
//     * Convenience method for returning the appropriate entityType
//     * for a given key handle, specifically, checks if keyHandle is SRK
//     * and returns TPMConsts.TPM_ET_SRK.
//     * <p>  
//     * NOTE: This is most useful for taking care of Infineon TPM 1.1 bug which
//     * requires that TPMConsts.TPM_ET_KEYHANDLE be used with the SRK.
//     * <p>
//     * TODO: This doesn't really differentiate between non-SRK keyhandles.
//     * 
//     * @param keyHandle
//     * @return
//     */
//    public static short getKeyHandleEntityType( int keyHandle, TPMDriver tpmDriver )
//    {
//        switch ( keyHandle )
//        {
//            case TPMConsts.TPM_KH_SRK:
//                // FIXME: This hasn't been tested on Infineon 1.2 chips
//                int manuf = tpmDriver.getTPMManufacturer();
//                TPM_STRUCT_VER ver = tpmDriver.getTPMVersion();
//                byte majorVer = ver.major;
//                byte minorVer = ver.minor;
//                if ( ( manuf == TPMDriver.TPM_MANUFACTURER_INFINEON )
//                     && ( majorVer == 1 ) && ( minorVer == 1 ) )
//                {
//                    return TPMConsts.TPM_ET_KEYHANDLE;
//                }
//                else
//                {
//                    return TPMConsts.TPM_ET_SRK;
//                }
//                // break; // unreachable code
//            
//            default:
//                return TPMConsts.TPM_ET_KEYHANDLE;
//        }
//    }

    /**
     * Uses a one-shot authorization session just for this command
     */
    public static TPMOutputStruct executeOSAPSession( TPMDriver tpmDriver,
        TPMAuth1Command cmd, short entityType, int entityValue,
        TPM_SECRET keyUsageAuth ) throws TPMException
    {
        if ( cmd.isNoAuth() )
        {
            // command does not require authorization, so don't even start a session
            return cmd.execute( tpmDriver );
        }
        
        TPMOSAPSession osapSession = new TPMOSAPSession( tpmDriver );
        osapSession.startSession( entityType, entityValue, keyUsageAuth );
        return osapSession.executeAuth1Cmd( cmd, false );
    }

    /**
     * Uses a one-shot authorization session for a 1-key Auth1 command.
     */
    public static TPMOutputStruct executeKeyOSAPSession( TPMDriver tpmDriver,
        TPMKeyAuth1Command cmd, TPM_SECRET keyUsageAuth ) throws TPMException
    {
        if ( cmd.isNoAuth() )
        {
            // command does not require authorization, so don't even start a session
            return cmd.execute( tpmDriver );
        }
        
        TPMOSAPSession osapSession = new TPMOSAPSession( tpmDriver );
        int entityValue = cmd.getKeyHandle();
        // use getKeyHandleEntityType here to allow us to check for Infineon TPM 1.1 SRK quirk
        // But actually, we don't need it since it should work if we just 
        // use TPM_ET_KEYHANDLE in any case.
//        short entityType = TPMOSAPSession.getKeyHandleEntityType( entityValue, tpmDriver );
        short entityType = TPMConsts.TPM_ET_KEYHANDLE;
        osapSession.startSession( entityType, entityValue, keyUsageAuth );
        return osapSession.executeAuth1Cmd( cmd, false );
    }

}
