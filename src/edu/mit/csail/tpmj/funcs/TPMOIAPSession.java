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
import edu.mit.csail.tpmj.util.Debug;

public class TPMOIAPSession extends TPMAuthorizationSession
{
    /**
     * Creates a session object.
     * Doesn't connect yet.  Call startSession to connect.
     *  
     * @param tpmDriver
     */
    public TPMOIAPSession( TPMDriver tpmDriver )
    {
        super( tpmDriver );
    }

    public int startSession() throws TPMException
    {
        if ( this.isActive() )
        {
            throw new TPMAuthSessionStateException(
                "Attempt to startSession on an active OIAP session." );
        }
        // TODO: Call TPM_Reset() to be safe ?

        Debug.println( "Calling TPM_OIAP()" );

        TPM_OIAP cmd = new TPM_OIAP();
        TPM_OIAPOutput oiapOut = (TPM_OIAPOutput) cmd.execute( tpmDriver );
        
        // Save authHandle, authLastNonceEven
        this.initialize( oiapOut.getAuthHandle(), oiapOut.getNonceEven() );

        return this.getAuthHandle();
    }

    /**
     * End this session using a call to TPM_TerminateHandle.
     * Note that this is unnecessary if your last call to execute
     * has a continueAuthSession value of false.
     * <p>
     * Note that the use of TPM_TerminateHandle is depecrated in TPM 1.2.
     * @throws TPMException
     */
    public void endSession() throws TPMException
    {
        TPM_TerminateHandle cmd = new TPM_TerminateHandle( this.getAuthHandle() );
        cmd.execute( tpmDriver );
        // if we reach this point, there was no error
        this.setActive( false );
    }
    
    // static convenience method

    /**
     * Overloaded version that allows the use of a different secret.
     * This sets the internal sharedSecret, so subsequent commands
     * executed without specifying the secret will use the last
     * secret that was used.
     */
    public TPMOutputStruct executeAuth1Cmd( TPMAuth1Command cmd, TPM_SECRET secret,
        boolean continueAuthSession ) throws TPMException
    {
        this.setSharedSecret( secret );
        return this.executeAuth1Cmd(cmd, continueAuthSession);
    }

    /**
     * Uses a one-shot authorization session just for this command
     */
    public static TPMOutputStruct executeOIAPSession( TPMDriver tpmDriver, TPMAuth1Command cmd,
        TPM_SECRET secret ) throws TPMException
    {
        if ( cmd.isNoAuth() )
        {
            // command does not require authorization, so don't even start a session
            return cmd.execute( tpmDriver );
        }
        
        TPMOIAPSession oiapSession = new TPMOIAPSession( tpmDriver );
        oiapSession.startSession();
        return oiapSession.executeAuth1Cmd( cmd, secret, false );
    }
}
