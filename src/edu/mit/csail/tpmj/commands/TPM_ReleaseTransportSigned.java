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

public class TPM_ReleaseTransportSigned extends TPMAuth2Command
{
    private int keyHandle;
    private TPM_NONCE antiReplay;

    public TPM_ReleaseTransportSigned()
    {
        super( TPMConsts.TPM_ORD_ReleaseTransportSigned );
        // needed for instantiation of new instance before calling fromBytes
    }
    
    
    /**
     * Constructs a command, given the <b>unencrypted</b> auth secrets.
     * 
     */
    public TPM_ReleaseTransportSigned( int keyHandle, TPM_NONCE antiReplay )
    {
        super( TPMConsts.TPM_ORD_ReleaseTransportSigned );
        this.setKeyHandle( keyHandle );
        this.setAntiReplay( antiReplay );
        this.setParamSize( this.computeParamSize( 34 ) );
    }

    public TPM_NONCE getAntiReplay()
    {
        return antiReplay;
    }

    public void setAntiReplay( TPM_NONCE antiReplay )
    {
        this.antiReplay = antiReplay;
    }

    public int getKeyHandle()
    {
        return keyHandle;
    }

    public void setKeyHandle( int keyHandle )
    {
        this.keyHandle = keyHandle;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_ReleaseTransportSignedOutput.class;
    }

    @Override
    public TPM_ReleaseTransportSignedOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_ReleaseTransportSignedOutput) super.execute( tpmDriver );
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // No passwords
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession1, TPMAuthorizationSession authSession2 )
    {
        // No passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams = { this.antiReplay };
        return inParams;
    }

    /**
     * If encrypted newAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.keyHandle, this.antiReplay );
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
        this.setKeyHandle( brw.readInt32() );
        this.antiReplay = new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) );
        this.readAuthData( source, offset );
    }
}
