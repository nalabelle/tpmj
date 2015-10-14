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

public class TPM_Unseal extends TPMAuth2Command
{
    private int parentHandle;
    private TPM_STORED_DATA inData;

    /**
     * Constructs a command, given the <b>unencrypted</b> auth secrets.
     * 
     */
    public TPM_Unseal( int parentHandle, TPM_STORED_DATA inData )
    {
        super( TPMConsts.TPM_ORD_Unseal );
        this.setParentHandle( parentHandle );
        this.setInData( inData );
        this.setParamSize( this.computeParamSize( 14 + inData.toBytes().length ) );
    }

    public int getParentHandle()
    {
        return parentHandle;
    }

    public void setParentHandle( int parentHandle )
    {
        this.parentHandle = parentHandle;
    }

    public TPM_STORED_DATA getInData()
    {
        return inData;
    }

    public void setInData( TPM_STORED_DATA inData )
    {
        this.inData = inData;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_UnsealOutput.class;
    }

    @Override
    public TPM_UnsealOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_UnsealOutput) super.execute( tpmDriver );
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // No passwords
    }

    @Override
    public void computeEncryptedPasswords(
        TPMAuthorizationSession authSession1,
        TPMAuthorizationSession authSession2 )
    {
        // No passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest()
    {
        Object[] inParams =
            { this.inData };
        return inParams;
    }

    /**
     * If encrypted newAuth is null, sets it to new (zero-valued) TPM_ENCAUTH,
     * otherwise, uses it as is. 
     */
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.parentHandle, this.inData );
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
        this.inData = new TPM_STORED_DATA();
        brw.readStruct( this.inData );
        
        this.readAuthData( source, offset );
    }
}
