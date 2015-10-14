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

public class TPM_TakeOwnership extends TPMAuth1Command
{
    private short protocolID;

    private byte[] encOwnerAuth;
    private byte[] encSrkAuth;
    private TPM_KEY keyInfo;

    /**
     * Constructs a command, given the <b>encrypted</b> auth secrets.
     * 
     */
    public TPM_TakeOwnership( byte[] encOwnerAuth, byte[] encSrkAuth,
        TPM_KEY keyInfo )
    {
        super( TPMConsts.TPM_ORD_TakeOwnership );
        this.protocolID = TPMConsts.TPM_PID_OWNER;

        this.setEncOwnerAuth( encOwnerAuth );
        this.setEncSrkAuth( encSrkAuth );
        this.setKeyInfo( keyInfo );
        // TODO: Is there a more efficient way to compute total paramSize without calling toBytes to keyInfo?
        this.setParamSize( this.computeParamSize( 10 + 2 + 4 + encOwnerAuth.length
            + 4 + encSrkAuth.length + keyInfo.toBytes().length ) );
    }

    public short getProtocolID()
    {
        return protocolID;
    }

    public void setProtocolID( short protocolID )
    {
        this.protocolID = protocolID;
    }

    public byte[] getEncOwnerAuth()
    {
        return encOwnerAuth;
    }

    /**
     * Sets encrypted owner auth data.  
     * 
     * @param encOwnerAuth
     */
    public void setEncOwnerAuth( byte[] encOwnerAuth )
    {
        this.encOwnerAuth = encOwnerAuth;
    }

    public byte[] getEncSrkAuth()
    {
        return encSrkAuth;
    }

    /**
     * Sets encrypted SRK auth data.  
     * 
     * @param encSrkAuth
     */
    public void setEncSrkAuth( byte[] encSrkAuth )
    {
        this.encSrkAuth = encSrkAuth;
    }

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
        return TPM_TakeOwnershipOutput.class;
    }

    @Override
    public TPM_TakeOwnershipOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_TakeOwnershipOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_TakeOwnershipOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_TakeOwnershipOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // No passwords
    }

    /**
     */
    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams =
            { this.protocolID, this.encOwnerAuth.length, this.encOwnerAuth,
                this.encSrkAuth.length, this.encSrkAuth, this.keyInfo };
        return inParams;
    }

    /**
     */
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.protocolID,
            this.encOwnerAuth.length, this.encOwnerAuth,
            this.encSrkAuth.length, this.encSrkAuth, this.keyInfo );
    }

    /**
     * NOTE: This reads in the <b>encrypted</b> usage and migration auth
     * data, and sets the plain (unencrypted) versions to null.
     * NOTE: that the encrypted dataUsageAuth will be overwritten
     * anyway in getInParamsForAuthDigest.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setProtocolID( brw.readShort() );
        int encOwnerAuthLength = brw.readInt32();
        this.setEncOwnerAuth( brw.readBytes( encOwnerAuthLength ) );
        int encSrkAuthLength = brw.readInt32();
        this.setEncSrkAuth( brw.readBytes( encSrkAuthLength ) );
        this.keyInfo = new TPM_KEY();
        brw.readStruct( this.keyInfo );
        this.readAuthData( source, offset );
    }
}
