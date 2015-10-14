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
public class TPMAuthInData extends SimpleTPMStruct
{
    public static final int AUTHHANDLE_OFFSET = 0;
    public static final int NONCEODD_OFFSET = 4;
    public static final int CONTINUEAUTHSESSION_OFFSET = 24;
    public static final int INAUTH_OFFSET = 25;
    public static final int STRUCT_SIZE = 45;

    private int authHandle;
    private TPM_NONCE nonceOdd;
    private boolean continueAuthSession;
    private TPM_DIGEST inAuth;

    public TPMAuthInData()
    {
        super();
    }

    public TPMAuthInData( byte[] source )
    {
        super( source );
    }

    public TPMAuthInData( int authHandle, TPM_NONCE nonceOdd,
        boolean continueAuthSession, TPM_DIGEST inAuth )
    {
        super();
        this.authHandle = authHandle;
        this.nonceOdd = nonceOdd;
        this.continueAuthSession = continueAuthSession;
        this.inAuth = inAuth;
    }

    public int getAuthHandle()
    {
        return authHandle;
    }

    public void setAuthHandle( int authHandle )
    {
        this.authHandle = authHandle;
    }

    public boolean isContinueAuthSession()
    {
        return continueAuthSession;
    }

    public void setContinueAuthSession( boolean continueAuthSession )
    {
        this.continueAuthSession = continueAuthSession;
    }

    public TPM_DIGEST getInAuth()
    {
        return inAuth;
    }

    public void setInAuth( TPM_DIGEST inAuth )
    {
        this.inAuth = inAuth;
    }

    public TPM_NONCE getNonceOdd()
    {
        return nonceOdd;
    }

    public void setNonceOdd( TPM_NONCE nonceOdd )
    {
        this.nonceOdd = nonceOdd;
    }

    public byte[] toBytes()
    {
        byte continueByte = (byte) (continueAuthSession ? 1 : 0);
        return ByteArrayUtil.buildBuf( this.authHandle, this.nonceOdd,
            continueByte, this.inAuth );
    }

    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.authHandle = brw.readInt32();
        this.nonceOdd = new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) );
        byte continueByte = brw.readByte();
        this.continueAuthSession = (continueByte != 0);
        this.inAuth = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ));
    }

    public String toString()
    {
        return "TPMAuthInData\n" + "authHandle: 0x" + Integer.toHexString( this.authHandle ) + "\n"
            + "nonceOdd: " + this.nonceOdd + "\n" + "continueAuthSession: "
            + this.continueAuthSession + "\n" + "inAuth: " + this.inAuth + "\n";
    }
}
