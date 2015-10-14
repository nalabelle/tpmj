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

public class TPM_DirWriteAuth extends TPMAuth1Command
{
    private int dirIndex;
    private TPM_DIGEST newContents;

    public TPM_DirWriteAuth( int dirIndex, TPM_DIGEST newContents )
    {
        super( TPMConsts.TPM_ORD_DirWriteAuth );
        this.setParamSize( this.computeParamSize( 34 ) );
        this.setDirIndex( dirIndex );
        this.setNewContents( newContents );
    }

    // Return Output Struct

    public int getDirIndex()
    {
        return dirIndex;
    }

    public void setDirIndex( int dirIndex )
    {
        this.dirIndex = dirIndex;
    }

    public TPM_DIGEST getNewContents()
    {
        return newContents;
    }

    public void setNewContents( TPM_DIGEST newContents )
    {
        this.newContents = newContents;
    }

    public Class getReturnType()
    {
        return TPM_DirWriteAuthOutput.class;
    }
    
    

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // no passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams =
            { this.dirIndex, this.newContents };
        return inParams;
    }

    @Override
    public TPM_DirWriteAuthOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_DirWriteAuthOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_DirWriteAuthOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_DirWriteAuthOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.dirIndex, this.newContents );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.setDirIndex( brw.readInt32() );
        this.setNewContents( new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE )) );
        this.readAuthData( source, offset );
    }

}
