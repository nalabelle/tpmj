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
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_Sign extends TPMKeyAuth1Command
{
    public static final int AREATOSIGNSIZE_OFFSET = 14;
    public static final int AREATOSIGN_OFFSET = 18;

    private byte[] areaToSign;

    /**
     * This is protected to prevent setting the areaToSign
     * to an arbitrary value.
     * 
     * TODO: allow for non-SHA1 areas to sign
     * 
     * @param keyHandle
     * @param areaToSign
     */
    protected TPM_Sign( int keyHandle, byte[] areaToSign )
    {
        super( TPMConsts.TPM_ORD_Sign, keyHandle );
        this.setAreaToSign( areaToSign );
        int dataParamSize = AREATOSIGN_OFFSET + this.getAreaToSignSize();
        this.setParamSize( this.computeParamSize( dataParamSize ) );
    }

    public TPM_Sign( int keyHandle, TPM_DIGEST digest )
    {
        this( keyHandle, digest.toBytes() );
    }
    
    
    public int getAreaToSignSize()
    {
        return this.areaToSign.length;
    }

    public byte[] getAreaToSign()
    {
        return this.areaToSign;
    }

    public void setAreaToSign( byte[] areaToSign )
    {
        if ( areaToSign == null )
        {
            this.areaToSign = new byte[0];
        }
        else
        {
            this.areaToSign = areaToSign;
        }
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // No passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest()
    {
        Object[] inParams =
            { this.getAreaToSignSize(), this.areaToSign };
        return inParams;
    }

    @Override
    public Class getReturnType()
    {
        return TPM_SignOutput.class;
    }

    @Override
    public TPM_SignOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_SignOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_SignOutput execute( TPMAuthorizationSession authSession1,
        boolean continueAuthSession1 ) throws TPMException
    {
        return (TPM_SignOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.getKeyHandle(),
            this.getAreaToSignSize(), this.areaToSign );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setKeyHandle( brw.readInt32() );
        int areaToSignSize = brw.readInt32();
        this.setAreaToSign( brw.readBytes( areaToSignSize ) );
        this.readAuthData( source, offset );
    }

}
