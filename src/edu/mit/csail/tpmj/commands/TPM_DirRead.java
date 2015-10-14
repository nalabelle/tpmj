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
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_DirRead extends TPMCommand
{
    private int dirIndex = 0;
    
    public TPM_DirRead( int dirIndex )
    {
        super( TPMConsts.TPM_TAG_RQU_COMMAND, 14, TPMConsts.TPM_ORD_DirRead );
        this.dirIndex = dirIndex;
    }

    // Return Output Struct
    public Class getReturnType()
    {
        return TPM_DirReadOutput.class;
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_DirReadOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_DirReadOutput) super.execute(tpmDriver);
    }
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.dirIndex );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.dirIndex = brw.readInt32();
    }
}