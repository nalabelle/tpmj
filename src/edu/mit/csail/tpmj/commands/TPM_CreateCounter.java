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

public class TPM_CreateCounter extends TPMAuth1Command
{
    private TPM_AUTHDATA plainAuth;
    private TPM_ENCAUTH encAuth;
    byte[] label;

    public TPM_CreateCounter()
    {
        this( TPM_SECRET.NULL, new byte[4] );
    }
    
    /**
     * Constructs a command, given the <b>unencrypted</b> auth secret.
     */
    public TPM_CreateCounter( TPM_AUTHDATA plainAuth, byte[] label )
    {
        super( TPMConsts.TPM_ORD_CreateCounter );
        this.setPlainAuth( plainAuth );
        this.setLabel( label );
        this.setParamSize( this.computeParamSize( 34 ) );
    }

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secret
     * and a label as a String (must be exactly 4 bytes long)
     */
    public TPM_CreateCounter( TPM_AUTHDATA plainAuth, String labelString )
    {
        this( plainAuth, labelString.getBytes() );
    }

    public byte[] getLabel()
    {
        return label;
    }

    public void setLabel( byte[] label )
    {
        if ( label.length != 4 )
        {
            throw new IllegalArgumentException( "label must have length 4" );
        }
        this.label = label;
    }

    public TPM_AUTHDATA getPlainAuth()
    {
        return plainAuth;
    }

    public void setPlainAuth( TPM_AUTHDATA plainAuth )
    {
        this.plainAuth = plainAuth;
    }

    public TPM_ENCAUTH getEncAuth()
    {
        return encAuth;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_CreateCounterOutput.class;
    }

    @Override
    public TPM_CreateCounterOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_CreateCounterOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_CreateCounterOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_CreateCounterOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    
    
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        if ( this.plainAuth == null )
        {
            // NOTE: In tpm-3.0.3's TPM_CreateWrapKey code, the keyAuth is encrypted even if it is all null
            this.encAuth = authSession.encryptAuthWithEvenNonce( TPM_SECRET.NULL );
        }
        else
        {
            this.encAuth = authSession.encryptAuthWithEvenNonce( this.plainAuth );
        }

        if ( this.encAuth == null )
        {
            // I don't think this can ever happen.
            this.encAuth = new TPM_ENCAUTH();
        }
    }

    /**
     * Calculates and returns encrypted encAuth and label.
     * (Calls back into the given authSession to ask it to do the encryption.)
     * <p>
     * Note: if plainAuth is null, the encrypted encAuth is the
     * encryption of an all-zeros secret.  If plainAuth is null,
     * dataMigrationAuth is not touched.  
     */
    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams =
            { this.encAuth, this.label };
        return inParams;
    }

    /**
     * If encrypted encAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        if ( this.encAuth == null )
        {
            this.encAuth = new TPM_ENCAUTH();
        }

        return this.createHeaderAndBody( this.encAuth, this.label );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> encAuth
     * data, and sets the plain (unencrypted) versions to null.
     * NOTE: that the encrypted encAuth will be overwritten
     * anyway in getInParamsForAuthDigest if the command is ever used again
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.encAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainAuth = null;
        this.label = brw.readBytes( 4 );
        this.readAuthData( source, offset );
    }
}
