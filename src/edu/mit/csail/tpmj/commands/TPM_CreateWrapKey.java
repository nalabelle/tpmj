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

public class TPM_CreateWrapKey extends TPMKeyAuth1Command
{
    private TPM_AUTHDATA plainDataUsageAuth;
    private TPM_AUTHDATA plainDataMigrationAuth;
    
    private TPM_ENCAUTH dataUsageAuth;
    private TPM_ENCAUTH dataMigrationAuth;
    private TPM_KEY keyInfo;

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secrets.
     * 
     * @param parentHandle
     * @param plainDataUsageAuth
     * @param plainDataMigrationAuth
     * @param keyInfo
     */
    public TPM_CreateWrapKey( int parentHandle, TPM_AUTHDATA plainDataUsageAuth,
        TPM_AUTHDATA plainDataMigrationAuth, TPM_KEY keyInfo )
    {
        super( TPMConsts.TPM_ORD_CreateWrapKey, parentHandle );
        this.setPlainDataUsageAuth( plainDataUsageAuth );
        this.setPlainDataMigrationAuth( plainDataMigrationAuth );
        this.setKeyInfo( keyInfo );
        // TODO: Is there a more efficient way to compute total paramSize without calling toBytes to keyInfo?
        this.setParamSize( this.computeParamSize( 14 + 20 + 20 + keyInfo.toBytes().length ) );
    }

    public int getParentHandle()
    {
        return this.getKeyHandle();
    }

    public void setParentHandle( int parentHandle )
    {
        this.setKeyHandle( parentHandle );
    }

    /**
     * Returns the unencrypted dataMigrationAuth data.
     * 
     * @return
     */
    public TPM_AUTHDATA getPlainDataMigrationAuth()
    {
        return plainDataMigrationAuth;
    }

    /**
     * Sets the unencrypted dataMigrationAuth data.
     * 
     * @return
     */
    public void setPlainDataMigrationAuth( TPM_AUTHDATA plainDataMigrationAuth )
    {
        this.plainDataMigrationAuth = plainDataMigrationAuth;
    }

    /**
     * Returns the unencrypted dataUsageAuth data.
     * 
     * @return
     */
    public TPM_AUTHDATA getPlainDataUsageAuth()
    {
        return plainDataUsageAuth;
    }

    /**
     * Sets the unencrypted dataUsageAuth data.
     * 
     * @return
     */
    public void setPlainDataUsageAuth( TPM_AUTHDATA plainDataUsageAuth )
    {
        this.plainDataUsageAuth = plainDataUsageAuth;
    }
    
    public TPM_ENCAUTH getDataMigrationAuth()
    {
        return dataMigrationAuth;
    }

//    public void setDataMigrationAuth( TPM_ENCAUTH dataMigrationAuth )
//    {
//        this.dataMigrationAuth = dataMigrationAuth;
//    }
//
    public TPM_ENCAUTH getDataUsageAuth()
    {
        return dataUsageAuth;
    }

//    public void setDataUsageAuth( TPM_ENCAUTH dataUsageAuth )
//    {
//        this.dataUsageAuth = dataUsageAuth;
//    }

    public TPM_KEY getKeyInfo()
    {
        return keyInfo;
    }

    public void setKeyInfo( TPM_KEY inKey )
    {
        this.keyInfo = inKey;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_CreateWrapKeyOutput.class;
    }

    @Override
    public TPM_CreateWrapKeyOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_CreateWrapKeyOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_CreateWrapKeyOutput execute( TPMAuthorizationSession authSession1,
        boolean continueAuthSession1 ) throws TPMException
    {
        return (TPM_CreateWrapKeyOutput) super.execute( authSession1,
            continueAuthSession1 );
    }
    
    /**
     * Calculates and returns encrypted dataUsageAuth and dataMigrationAuth, and (unencrypted) keyInfo.
     * (Calls back into the give authSession to ask it to do the encryption.)
     * <p>
     * Note: if plainDataUsageAuth is null, the encrypted dataUsageAuth is the
     * encryption of an all-zeros secret.  If plainDataMigrationAuth is null,
     * dataMigrationAuth is not touched.  
     * <p>
     * After checking for nulls in plainDataUsageAuth, 
     * if this.dataMigrationAuth is null, then the encrypted dataMigrationAuth is itself set to all-zeros.  
     * (This seems to be how tpm-3.0.3 from IBM does it.)
     * The same is done with this.dataUsageAuth, but in the current implementation,
     * it should never be null because it is always set to something in this method,
     * based on this.plainDataUsageAuth.
     * <p>
     * Note: if  
     */
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        if ( this.plainDataUsageAuth == null )
        {
            // NOTE: In tpm-3.0.3's TPM_CreateWrapKey code, the keyAuth is encrypted even if it is all null
            this.dataUsageAuth = authSession.encryptAuthWithEvenNonce( TPM_SECRET.NULL );
        }
        else
        {
            this.dataUsageAuth = authSession.encryptAuthWithEvenNonce( this.plainDataUsageAuth );
        }
        
        // FIXME: Make sure this is correct behavior for migratable keys
        // NOTE: In tpm-3.0.3's TPM_CreateWrapKey code, they encrypted migAuth is set to all zeros if there is no migration auth
        if ( this.plainDataMigrationAuth != null )
        {
            this.dataMigrationAuth = authSession.encryptAuthWithOddNonce( this.plainDataMigrationAuth );
        }
        
        if ( this.dataUsageAuth == null )
        {
            // I don't think this can ever happen.
            this.dataUsageAuth = new TPM_ENCAUTH();
        }
        
        if ( this.dataMigrationAuth == null )
        {
            this.dataMigrationAuth = new TPM_ENCAUTH();
        }
        
        Debug.println( "TPM_CreateWrapKey.computeEncryptedPasswords: dataUsageAuth=", this.dataUsageAuth );
        Debug.println( "TPM_CreateWrapKey.computeEncryptedPasswords: dataMigrationAuth=", this.dataMigrationAuth );
    }

    @Override
    public Object[] getInParamsForAuthDigest()
    {
        Object[] inParams = { this.dataUsageAuth, this.dataMigrationAuth, this.keyInfo };
        return inParams;
    }

    /**
     * If encrypted dataUsageAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * (and same with dataMigrationAuth), otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        if ( this.dataUsageAuth == null )
        {
            this.dataUsageAuth = new TPM_ENCAUTH();
        }
        if ( this.dataMigrationAuth == null )
        {
            this.dataMigrationAuth = new TPM_ENCAUTH();
        }
        return this.createHeaderAndBody( this.getParentHandle(), this.dataUsageAuth, 
            this.dataMigrationAuth, this.getKeyInfo() );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> usage and migration auth
     * data, and sets the plain (unencrypted) versions to null.
     * NOTE: that the encrypted dataUsageAuth will be overwritten
     * anyway in computeEncryptedPasswords if this command is ever executed afterwards.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setParentHandle( brw.readInt32() );
        this.dataUsageAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainDataUsageAuth = null;
        this.dataMigrationAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainDataMigrationAuth = null;
        this.keyInfo = new TPM_KEY();
        brw.readStruct( this.keyInfo );
        this.readAuthData( source, offset );
    }
}
