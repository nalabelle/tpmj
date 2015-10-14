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
package edu.mit.csail.tpmj.structs;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_TRANSPORT_INTERNAL extends SimpleTaggedTPMStruct
{
    private TPM_AUTHDATA authData;
    private TPM_TRANSPORT_PUBLIC tranPublic;
    private int transHandle;
    private TPM_NONCE transEven;
    private TPM_DIGEST transDigest;

    public TPM_TRANSPORT_INTERNAL( TPM_AUTHDATA authData,
        TPM_TRANSPORT_PUBLIC tranPublic, int transHandle, TPM_NONCE transEven,
        TPM_DIGEST transDigest )
    {
        super( TPMConsts.TPM_TAG_TRANSPORT_INTERNAL );
        this.authData = authData;
        this.tranPublic = tranPublic;
        this.transHandle = transHandle;
        this.transEven = transEven;
        this.transDigest = transDigest;
    }

    public TPM_AUTHDATA getAuthData()
    {
        return authData;
    }

    public void setAuthData( TPM_AUTHDATA authData )
    {
        this.authData = authData;
    }

    public TPM_TRANSPORT_PUBLIC getTranPublic()
    {
        return tranPublic;
    }

    public void setTranPublic( TPM_TRANSPORT_PUBLIC tranPublic )
    {
        this.tranPublic = tranPublic;
    }

    public TPM_DIGEST getTransDigest()
    {
        return transDigest;
    }

    public void setTransDigest( TPM_DIGEST transDigest )
    {
        this.transDigest = transDigest;
    }

    public TPM_NONCE getTransEven()
    {
        return transEven;
    }

    public void setTransEven( TPM_NONCE transEven )
    {
        this.transEven = transEven;
    }

    public int getTransHandle()
    {
        return transHandle;
    }

    public void setTransHandle( int transHandle )
    {
        this.transHandle = transHandle;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.authData, this.tranPublic,
            this.transHandle, this.transEven, this.transDigest );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.setAuthData( new TPM_AUTHDATA( brw.readBytes( TPM_AUTHDATA.SIZE ) ) );
        this.tranPublic = new TPM_TRANSPORT_PUBLIC();
        brw.readStruct( this.tranPublic );
        this.setTransHandle( brw.readInt32() );
        this.setTransEven( new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) ) );
        this.setTransDigest( new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) ) );
    }

//    // This probably won't be useful anyway.
//    @Override
//    public String toString()
//    {
//    }

}
