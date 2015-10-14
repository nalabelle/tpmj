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
import edu.mit.csail.tpmj.util.CryptoUtil;

public class TPM_QUOTE_INFO extends SimpleTPMStruct
{
    public static final byte[] FIXED_QUOT = { 'Q', 'U', 'O', 'T' };
    
    /*
     * typedef struct tdTPM_QUOTE_INFO{
     *      TPM_STRUCT_VER version;
     *      BYTE fixed[4];
     *      TPM_COMPOSITE_HASH digestValue;
     *      TPM_NONCE externalData;
     * } TPM_QUOTE_INFO;
     */

    private TPM_STRUCT_VER version;
    private byte[] fixed;
    private TPM_DIGEST digestValue;
    private TPM_NONCE externalData;

    public TPM_QUOTE_INFO()
    {
        this.version = TPM_STRUCT_VER.TPM_1_1_VER;
        this.fixed = FIXED_QUOT;
    }
    
    public TPM_QUOTE_INFO( TPM_STRUCT_VER version, TPM_DIGEST digestValue, TPM_NONCE externalData )
    {
        this();
        this.version = version;
        this.digestValue = digestValue;
        this.externalData = externalData;
    }
    
    public TPM_QUOTE_INFO( TPM_STRUCT_VER version, TPM_PCR_COMPOSITE pcrComposite, TPM_NONCE externalData )
    {
        this();
        this.version = version;
        this.digestValue = CryptoUtil.computeTPM_DIGEST( pcrComposite );
        this.externalData = externalData;
    }
    

    @Override
    public byte[] toBytes()
    {
        /*
         * typedef struct tdTPM_QUOTE_INFO{
         *      TPM_STRUCT_VER version;
         *      BYTE fixed[4];
         *      TPM_COMPOSITE_HASH digestValue;
         *      TPM_NONCE externalData;
         * } TPM_QUOTE_INFO;
         */
        return ByteArrayUtil.buildBuf( this.version, this.fixed, this.digestValue, this.externalData );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.version = new TPM_STRUCT_VER();
        brw.readStruct( this.version );
        this.fixed = brw.readBytes( 4 );
        this.digestValue = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) );
        this.externalData = new TPM_NONCE( brw.readBytes( TPM_NONCE.SIZE ) );
    }

    public String toString()
    {
        return "TPM_QUOTE_INFO:\n"
            + "version: " + this.version + "\n"
            + "fixed: " + ByteArrayUtil.toPrintableHexString( this.fixed ) + "\n"
            + "digestValue: " + this.digestValue + "\n"
            + "externalData: " + this.externalData;
    }
    
}
