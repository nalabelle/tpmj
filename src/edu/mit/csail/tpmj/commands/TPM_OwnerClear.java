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
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_OwnerClear extends TPMAuth1Command
{
    public TPM_OwnerClear()
    {
        super( TPMConsts.TPM_ORD_OwnerClear );
        this.setParamSize( this.computeParamSize( 10 ) );
    }

    public Class getReturnType()
    {
        return TPM_OwnerClearOutput.class;
    }

    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // no passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams = new Object[0];
        return inParams;
    }

    @Override
    public TPM_OwnerClearOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_OwnerClearOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_OwnerClearOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_OwnerClearOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody();
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.readAuthData( source, offset );
    }

}
