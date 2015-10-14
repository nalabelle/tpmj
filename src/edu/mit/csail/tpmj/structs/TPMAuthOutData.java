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

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

/**
 * This is the structure at the end of both the input
 * and output structures of commands with authorization.
 * 
 * @author lfgs
 */
public class TPMAuthOutData extends SimpleTPMStruct
{
    public static final int NONCEEVEN_OFFSET = 0;
    public static final int CONTINUEAUTHSESSION_OFFSET = 20;
    public static final int RESAUTH_OFFSET = 21;
    public static final int STRUCT_SIZE = 41;
    
    private TPM_NONCE nonceEven;
    private boolean continueAuthSession;
    private TPM_DIGEST resAuth;
    
    public TPMAuthOutData()
    {
        super();
    }

    public TPMAuthOutData( byte[] source )
    {
        super(source);
    }

    public TPMAuthOutData( TPM_NONCE nonceEven, boolean continueAuthSession, TPM_DIGEST resAuth )
    {
        super();
        this.nonceEven = nonceEven;
        this.continueAuthSession = continueAuthSession;
        this.resAuth = resAuth;
    }

    public boolean isContinueAuthSession()
    {
        return continueAuthSession;
    }
    
    public byte getContinueAuthSessionByte()
    {
        return (byte) ( this.continueAuthSession ? 1 : 0 );
    }

    public void setContinueAuthSession( boolean continueAuthSession )
    {
        this.continueAuthSession = continueAuthSession;
    }

    public TPM_NONCE getNonceEven()
    {
        return nonceEven;
    }

    public void setNonceEven( TPM_NONCE nonceEven )
    {
        this.nonceEven = nonceEven;
    }

    public TPM_DIGEST getResAuth()
    {
        return resAuth;
    }

    public void setResAuth( TPM_DIGEST resAuth )
    {
        this.resAuth = resAuth;
    }

    public byte[] toBytes()
    {
        byte continueByte = (byte) (continueAuthSession ? 1 : 0);
        return ByteArrayUtil.buildBuf( this.nonceEven, continueByte, this.resAuth );
    }

    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.nonceEven = new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) );
        this.continueAuthSession = (brw.readByte() != 0);
        this.resAuth = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) );
    }

    public String toString()
    {
        return "TPMAuthOutData\n"
            + "nonceEven: " + this.nonceEven + "\n"
            + "continueAuthSession: " + this.continueAuthSession + "\n"
            + "resAuth: " + this.resAuth + "\n";
    }
}
