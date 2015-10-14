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

import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_KEY;

/**
 * The output structure for TPM_MakeIdentity.
 * Note: this assumes that the contents of the 
 * structure does not change.  (In particular,
 * for efficiency, the offset for identityBinding is determined
 * the first time it is needed and not determined again.)
 * 
 * @author lfgs
 */
public class TPM_MakeIdentityOutput extends TPMAuth2CommandOutput
{
    public static final int IDKEY_OFFSET = 10;

    // NOTE: This isn't really a part of the data structure.
    private int identityBindingSizeOffset = 0;

    protected int getIdentityBindingSizeOffset()
    {
        if ( this.identityBindingSizeOffset == 0 )
        {
            TPM_KEY idKey = this.getIdKey();
            this.identityBindingSizeOffset = IDKEY_OFFSET + idKey.toBytes().length;
        }
        
        return this.identityBindingSizeOffset;
    }
    
    public TPM_KEY getIdKey()
    {
        TPM_KEY idKey = new TPM_KEY();
        this.getStruct( IDKEY_OFFSET, idKey );

        if ( this.identityBindingSizeOffset == 0 )
        {
            this.identityBindingSizeOffset = IDKEY_OFFSET + idKey.toBytes().length;
        }
        
        return idKey;
    }
    
    public int getIdentityBindingSize()
    {
        int offset = this.getIdentityBindingSizeOffset();
        return this.getInt32( offset );
    }
    
    public byte[] getIdentityBinding()
    {
        int offset = this.getIdentityBindingSizeOffset() + 4;
        int length = this.getIdentityBindingSize();
        return this.getBytes( offset, length );
    }
    
    public void fromBytes( byte[] source, int offset )
    {
        super.fromBytes( source, offset );
    }
    
}
