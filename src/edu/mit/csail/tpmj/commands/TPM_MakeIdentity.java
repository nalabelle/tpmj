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
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.Debug;

public class TPM_MakeIdentity extends TPMAuth2Command
{
    private TPM_AUTHDATA plainIdentityAuth;
    private TPM_ENCAUTH identityAuth;
    private TPM_DIGEST labelPrivCADigest;
    private TPM_KEY idKeyParams;

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secret.
     * 
     */
    public TPM_MakeIdentity( TPM_AUTHDATA plainIdentityAuth,
        TPM_DIGEST labelPrivCADigest, TPM_KEY idKeyParams )
    {
        super( TPMConsts.TPM_ORD_MakeIdentity );
        this.setPlainIdentityAuth( plainIdentityAuth );
        this.setLabelPrivCADigest( labelPrivCADigest );
        this.setIdKeyParams( idKeyParams );
        // TODO: Is there a more efficient way to compute total paramSize without calling toBytes?
        this.setParamSize( this.computeParamSize( 50 + idKeyParams.toBytes().length ) );
    }

    /**
     * Gets encrypted identityAuth.
     * @return
     */
    public TPM_ENCAUTH getIdentityAuth()
    {
        return identityAuth;
    }

    //    /**
    //     * Directly sets encrypted identityAuth.
    //     * 
    //     * @return
    //     */
    //    public void setIdentityAuth( TPM_ENCAUTH identityAuth )
    //    {
    //        this.identityAuth = identityAuth;
    //    }

    public TPM_KEY getIdKeyParams()
    {
        return idKeyParams;
    }

    public void setIdKeyParams( TPM_KEY idKeyParams )
    {
        this.idKeyParams = idKeyParams;
    }

    public TPM_DIGEST getLabelPrivCADigest()
    {
        return labelPrivCADigest;
    }

    public void setLabelPrivCADigest( TPM_DIGEST labelPrivCADigest )
    {
        this.labelPrivCADigest = labelPrivCADigest;
    }

    /**
     * Gets unencrypted identityAuth.
     * @return
     */
    public TPM_AUTHDATA getPlainIdentityAuth()
    {
        return plainIdentityAuth;
    }

    /**
     * Sets unencrypted identityAuth and clears encrypted identityAuth.
     * 
     * @return
     */
    public void setPlainIdentityAuth( TPM_AUTHDATA plainIdentityAuth )
    {
        this.plainIdentityAuth = plainIdentityAuth;
        this.identityAuth = null;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_MakeIdentityOutput.class;
    }

    @Override
    public TPM_MakeIdentityOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_MakeIdentityOutput) super.execute( tpmDriver );
    }

    
    /**
     * This calls the one-argument version of 
     * computeEncryptedPasswords with authSession2, since that's what matters in this case.
     */
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession1, TPMAuthorizationSession authSession2 )
    {
        this.computeEncryptedPasswords( authSession2 );        
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        if ( this.plainIdentityAuth == null )
        {
            // NOTE: In tpm-3.0.3's TPM_CreateWrapKey code, the keyAuth is encrypted even if it is all null
            this.identityAuth = authSession.encryptAuthWithEvenNonce( TPM_SECRET.NULL );
        }
        else
        {
            this.identityAuth = authSession.encryptAuthWithEvenNonce( this.plainIdentityAuth );
        }
    }

    /**
     * Calculates and returns encrypted identityAuth
     * <p>
     * Note: if plainIdentityAuth is null, the encrypted newAuth is the
     * encryption of an all-zeros secret.
     * 
     */
    @Override
    public Object[] getInParamsForAuthDigest( )
    {
//        // FIXME: Remove this when I figure out the bug.
//        Debug.println( "TPM_MakeIdentity.getInParamsForAuthDigest: " 
//            + "this.plainIdentityAuth = ", 
//            this.plainIdentityAuth, 
//            "\nidentityAuth = ",
//            this.identityAuth );

        Object[] inParams =
            { this.identityAuth, this.labelPrivCADigest, this.idKeyParams };
        return inParams;
    }

    /**
     * If encrypted identityAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        if ( this.identityAuth == null )
        {
            this.identityAuth = new TPM_ENCAUTH();
        }
        return this.createHeaderAndBody( this.identityAuth,
            this.labelPrivCADigest, this.idKeyParams );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> identityAuth
     * data, and sets the plain (unencrypted) version to null.
     * NOTE: if this command is used, then 
     * that the encrypted identityAuth will be overwritten
     * in getInParamsForAuthDigest.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.identityAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainIdentityAuth = null;
        this.labelPrivCADigest = new TPM_DIGEST(
            brw.readBytes( TPM_DIGEST.SIZE ) );
        this.idKeyParams = new TPM_KEY();
        brw.readStruct( this.idKeyParams );
        this.readAuthData( source, offset );
    }
}
