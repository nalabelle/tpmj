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

public class TPM_LoadKey extends TPMKeyAuth1Command
{
    private TPM_KEY inKey;

    public TPM_LoadKey( int parentHandle, TPM_KEY inKey )
    {
        super( TPMConsts.TPM_ORD_LoadKey, parentHandle );
        this.setInKey( inKey );
        // TODO: Is there a more efficient way to compute total paramSize without calling toBytes to inKey?
        this.setParamSize( this.computeParamSize( 14 + inKey.toBytes().length ) );
    }

    public int getParentHandle()
    {
        return this.getKeyHandle();
    }

    public void setParentHandle( int parentHandle )
    {
        this.setKeyHandle( parentHandle );
    }

    public TPM_KEY getInKey()
    {
        return inKey;
    }

    public void setInKey( TPM_KEY inKey )
    {
        this.inKey = inKey;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_LoadKeyOutput.class;
    }

    @Override
    public TPM_LoadKeyOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_LoadKeyOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_LoadKeyOutput execute( TPMAuthorizationSession authSession1,
        boolean continueAuthSession1 ) throws TPMException
    {
        return (TPM_LoadKeyOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // No passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest()
    {
        Object[] inParams = { this.inKey };
        return inParams;
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.getParentHandle(), this.getInKey() );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setParentHandle( brw.readInt32() );
        this.inKey = new TPM_KEY();
        brw.readStruct( this.inKey );
        this.readAuthData( source, offset );
    }
}
