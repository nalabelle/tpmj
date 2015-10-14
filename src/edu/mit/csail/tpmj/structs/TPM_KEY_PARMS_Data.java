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

/**
 * This is the abstract superclass of classes which
 * can be stored in TPM_KEY_PARMS's parms field (i.e., TPM_RSA_KEY_PARMS, and TPM_SYMMETRIC_KEY_PARMS.
 * <p>
 * Note: This is NOT a TPM-standard structure, and is just defined here
 * for the purpose of having an abstract superclass for TPM_*_KEY_PARMS
 * 
 * @author lfgs
 *
 */
public abstract class TPM_KEY_PARMS_Data extends SimpleTPMStruct
{
    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_KEY_PARMS_Data()
    {
        // do nothing
    }

    public static TPM_KEY_PARMS_Data createKeyParmsData( int algorithmID, byte[] source )
    {
        if ( (source == null) || (source.length == 0) )
        {
            return null;
        }
        
        TPM_KEY_PARMS_Data parms = null;
        switch ( algorithmID )
        {
            case TPMConsts.TPM_ALG_RSA:
                parms = new TPM_RSA_KEY_PARMS();
                break;
            case TPMConsts.TPM_ALG_DES:
            case TPMConsts.TPM_ALG_3DES:
            case TPMConsts.TPM_ALG_AES128:
            case TPMConsts.TPM_ALG_AES192:
            case TPMConsts.TPM_ALG_AES256:
                parms = new TPM_SYMMETRIC_KEY_PARMS();
                break;
            case TPMConsts.TPM_ALG_SHA:
            case TPMConsts.TPM_ALG_HMAC:
            case TPMConsts.TPM_ALG_MGF1:
            case TPMConsts.TPM_ALG_XOR:
                // FIXME: The spec doesn't mention if TPM_ALG_XOR should use TPM_SYMMETRIC_KEY_PARMS
                return null;
            default:
                throw new IllegalArgumentException( "Can't create Key Parms data for unknown algorithmID 0x" 
                    + Integer.toHexString( algorithmID ) );
        }
        // if we reach this point, then parms is not null
        
        parms.fromBytes( source, 0 );
        return parms;
    }

}
