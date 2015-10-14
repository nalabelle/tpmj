/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2007
 */
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.Debug;

public class TPM_Seal extends TPMKeyAuth1Command
{
    private TPM_AUTHDATA plainAuth;
    private TPM_ENCAUTH encAuth;
    private TPM_PCR_INFO pcrInfo;
    private byte[] inData;

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secret.
     * pcrInfo can be null to seal data not independent of PCRs.
     */
    public TPM_Seal( int keyHandle, TPM_AUTHDATA plainAuth, TPM_PCR_INFO pcrInfo, byte[] inData )
    {
        super( TPMConsts.TPM_ORD_Seal, keyHandle );
        this.setPlainAuth( plainAuth );
        this.setPcrInfo( pcrInfo );
        this.setInData( inData );
        // TODO: Is there a more efficient way to compute total paramSize without calling toBytes
        byte[] pcrInfoBytes = this.getPcrInfoBytes();
        this.setParamSize( this.computeParamSize( 14 + 20 + 4 + pcrInfoBytes.length + 4 + inData.length ) );
    }

    /**
     * Returns the unencrypted dataUsageAuth data.
     * 
     * @return
     */
    public TPM_AUTHDATA getPlainAuth()
    {
        return plainAuth;
    }

    /**
     * Sets the unencrypted dataUsageAuth data.
     * 
     * @return
     */
    public void setPlainAuth( TPM_AUTHDATA plainDataUsageAuth )
    {
        this.plainAuth = plainDataUsageAuth;
    }

    public TPM_ENCAUTH getEncAuth()
    {
        return encAuth;
    }

    public byte[] getInData()
    {
        return inData;
    }

    public void setInData( byte[] inData )
    {
        this.inData = inData;
    }

    public TPM_PCR_INFO getPcrInfo()
    {
        return pcrInfo;
    }
    
    /**
     * Returns toBytes of pcrInfo, or a zero-length byte array if pcrInfo is null.
     * This is used in toBytes, the constructor and getInParamsForAuthDigest
     * 
     * @return
     */
    public byte[] getPcrInfoBytes()
    {
        return ( pcrInfo != null ) ? pcrInfo.toBytes() : new byte[0];
    }

    public void setPcrInfo( TPM_PCR_INFO pcrInfo )
    {
        this.pcrInfo = pcrInfo;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_SealOutput.class;
    }

    @Override
    public TPM_SealOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_SealOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_SealOutput execute( TPMAuthorizationSession authSession1,
        boolean continueAuthSession1 ) throws TPMException
    {
        return (TPM_SealOutput) super.execute( authSession1,
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
        Debug.println( "TPM_Seal.computeEncryptedPasswords: encAuth=",
            this.encAuth );
    }

    @Override
    public Object[] getInParamsForAuthDigest()
    {
        byte[] pcrInfoBytes = this.getPcrInfoBytes();
        Object[] inParams =
            { this.encAuth, pcrInfoBytes.length, pcrInfoBytes, this.inData.length, this.inData };
        return inParams;
    }

    /**
     * If encrypted dataUsageAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * (and same with dataMigrationAuth), otherwise, uses it as is.
     * If pcrInfo is null, then a zero-length array is used.
     */
    @Override
    public byte[] toBytes()
    {
        if ( this.encAuth == null )
        {
            this.encAuth = new TPM_ENCAUTH();
        }
        byte[] pcrInfoBytes = this.getPcrInfoBytes();
        return this.createHeaderAndBody( this.getKeyHandle(), this.encAuth,
            pcrInfoBytes.length, pcrInfoBytes, this.inData.length, this.inData  );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> usage auth
     * data, and sets the plain (unencrypted) versions to null.
     * NOTE: that the encrypted auth will be overwritten
     * anyway in computeEncryptedPasswords if this command is ever executed afterwards.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setKeyHandle( brw.readInt32() );
        this.encAuth = new TPM_ENCAUTH( brw.readBytes( TPM_ENCAUTH.SIZE ) );
        this.plainAuth = null;
        int pcrInfoSize = brw.readInt32();
        byte[] pcrInfoData = brw.readBytes( pcrInfoSize );
        this.pcrInfo = new TPM_PCR_INFO( pcrInfoData );
        int inDataSize = brw.readInt32();
        this.inData = brw.readBytes( inDataSize );
        
        this.readAuthData( source, offset );
    }
}
