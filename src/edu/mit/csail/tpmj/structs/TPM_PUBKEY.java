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

public class TPM_PUBKEY extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_PUBKEY{
     *      TPM_KEY_PARMS algorithmParms;
     *      TPM_STORE_PUBKEY pubKey;
     * } TPM_PUBKEY;
     * 
     */

    private TPM_KEY_PARMS algorithmParms;
    private TPM_STORE_PUBKEY pubKey;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_PUBKEY()
    {
        // do nothing
    }

    public TPM_PUBKEY( TPM_KEY_PARMS algorithmParms, TPM_STORE_PUBKEY pubKey )
    {
        this.algorithmParms = algorithmParms;
        this.pubKey = pubKey;
    }

    public TPM_KEY_PARMS getAlgorithmParms()
    {
        return algorithmParms;
    }

    public void setAlgorithmParms( TPM_KEY_PARMS algorithmParms )
    {
        this.algorithmParms = algorithmParms;
    }
    
    public TPM_STORE_PUBKEY getPubKey()
    {
        return pubKey;
    }

    public void setPubKey( TPM_STORE_PUBKEY pubKey )
    {
        this.pubKey = pubKey;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( algorithmParms, pubKey );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.algorithmParms = new TPM_KEY_PARMS();
        brw.readStruct( this.algorithmParms );
        this.pubKey = new TPM_STORE_PUBKEY();
        brw.readStruct( this.pubKey );
    }

    public String toString()
    {
        return "TPM_PUBKEY: \n" 
            + "algorithmParms = " + this.algorithmParms + "\n" 
            + "pubKey: " + this.pubKey;
    }
}
