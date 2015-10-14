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

public class TPM_ChangeAuth extends TPMAuth2Command
{
    private int parentHandle;
    private short protocolID;
    private TPM_AUTHDATA plainNewAuth;
    private TPM_ENCAUTH newAuth;
    private short entityType;
    private byte[] encData;

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secrets.
     * 
     */
    public TPM_ChangeAuth( int parentHandle, short protocolID,
        TPM_AUTHDATA plainNewAuth, short entityType, byte[] encData )
    {
        super( TPMConsts.TPM_ORD_ChangeAuth );
        this.setParentHandle( parentHandle );
        this.setProtocolID( protocolID );
        this.setPlainNewAuth( plainNewAuth );
        this.setEntityType( entityType );
        this.setEncData( encData );
        this.setParamSize( this.computeParamSize( 42 + this.getEncDataSize() ) );
    }

    public int getParentHandle()
    {
        return parentHandle;
    }

    public void setParentHandle( int parentHandle )
    {
        this.parentHandle = parentHandle;
    }

    public byte[] getEncData()
    {
        return this.encData;
    }

    public void setEncData( byte[] encData )
    {
        if ( encData == null )
        {
            encData = new byte[0];
        }
        this.encData = encData;
    }

    public int getEncDataSize()
    {
        if ( this.encData == null )
        {
            return 0;
        }
        else
        {
            return this.encData.length;
        }
    }

    public short getEntityType()
    {
        return entityType;
    }

    public void setEntityType( short entityType )
    {
        this.entityType = entityType;
    }

    public short getProtocolID()
    {
        return protocolID;
    }

    public void setProtocolID( short protocolID )
    {
        this.protocolID = protocolID;
    }

    /**
     * Returns the unencrypted newAuth data.
     * 
     * @return
     */
    public TPM_AUTHDATA getPlainNewAuth()
    {
        return this.plainNewAuth;
    }

    /**
     * Sets the unencrypted newAuth data.
     * 
     * @return
     */
    public void setPlainNewAuth( TPM_AUTHDATA plainNewAuth )
    {
        this.plainNewAuth = plainNewAuth;
    }

    public TPM_ENCAUTH getNewAuth()
    {
        return this.newAuth;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_ChangeAuthOutput.class;
    }

    @Override
    public TPM_ChangeAuthOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_ChangeAuthOutput) super.execute( tpmDriver );
    }
    
    

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // NOTE: this method actually should not be called,
        // but just in case, it will still work because
        // the encryption is done with the OSAP shared secret
        // of the first session.
        if ( this.plainNewAuth == null )
        {
            // NOTE: In tpm-3.0.3's TPM_CreateWrapKey code, the keyAuth is encrypted even if it is all null
            this.newAuth = authSession.encryptAuthWithEvenNonce( TPM_SECRET.NULL );
        }
        else
        {
            this.newAuth = authSession.encryptAuthWithEvenNonce( this.plainNewAuth );
        }
    }
    
    /**
     * This simply calls computeEncryptedPasswords with only authSession1, since that's what matters in this case.
     */
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession1, TPMAuthorizationSession authSession2 )
    {
        this.computeEncryptedPasswords( authSession1 );
    }

    /**
     * Calculates and returns encrypted newAuth
     * <p>
     * Note: if plainNewAuth is null, the encrypted newAuth is the
     * encryption of an all-zeros secret.
     * 
     */
    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams =
            { this.protocolID, this.newAuth, this.entityType,
                this.getEncDataSize(), this.encData };
        return inParams;
    }

    /**
     * If encrypted newAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        if ( this.newAuth == null )
        {
            this.newAuth = new TPM_ENCAUTH();
        }
        return this.createHeaderAndBody( this.parentHandle, this.protocolID,
            this.newAuth, this.entityType, this.getEncDataSize(), this.encData );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> newAuth
     * data, and sets the plain (unencrypted) version to null.
     * NOTE: that the encrypted newAuth will be overwritten
     * anyway in getInParamsForAuthDigest.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.setParentHandle( brw.readInt32() );
        this.setProtocolID( brw.readShort() );
        this.newAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainNewAuth = null;
        this.setEntityType( brw.readShort() );
        int encDataSize = brw.readInt32();
        this.setEncData( brw.readBytes( encDataSize ) );
        this.readAuthData( source, offset );
    }
}
