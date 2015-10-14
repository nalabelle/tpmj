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
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_IDENTITY_CONTENTS extends SimpleTPMStruct
{
    /* 
     * typedef struct tdTPM_IDENTITY_CONTENTS {
     *    TPM_STRUCT_VER ver;
     *    UINT32 ordinal;
     *    TPM_CHOSENID_HASH labelPrivCADigest;
     *    TPM_PUBKEY identityPubKey;
     * } TPM_IDENTITY_CONTENTS;
     */

    private TPM_STRUCT_VER ver;
    int ordinal;
    private TPM_DIGEST labelPrivCADigest;
    private TPM_PUBKEY identityPubKey;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_IDENTITY_CONTENTS()
    {
        // do nothing
    }

    /**
     * Note: This is <b>not</b> the recommended way to construct this,
     * since ver and ordinal are defined mandatorily by the spect.
     * 
     * @param ver
     * @param ordinal
     * @param labelPrivCADigest
     * @param identityPubKey
     */
    public TPM_IDENTITY_CONTENTS( TPM_STRUCT_VER ver, int ordinal, 
        TPM_DIGEST labelPrivCADigest, TPM_PUBKEY identityPubKey )
    {
        this.ver = ver;
        this.ordinal = ordinal;
        this.labelPrivCADigest = labelPrivCADigest;
        this.identityPubKey = identityPubKey;
    }

    /**
     * This is the recommended way to construct an instance.
     * This automatically sets ver and ordinal to the values required
     * by the spec.
     * 
     * @param labelPrivCADigest
     * @param identityPubKey
     */
    public TPM_IDENTITY_CONTENTS( TPM_DIGEST labelPrivCADigest, TPM_PUBKEY identityPubKey )
    {
        this( TPM_STRUCT_VER.TPM_1_1_VER, TPMConsts.TPM_ORD_MakeIdentity, 
            labelPrivCADigest, identityPubKey  );
    }
    
    


    public TPM_PUBKEY getIdentityPubKey()
    {
        return identityPubKey;
    }

    public void setIdentityPubKey( TPM_PUBKEY identityPubKey )
    {
        this.identityPubKey = identityPubKey;
    }

    public TPM_DIGEST getLabelPrivCADigest()
    {
        return labelPrivCADigest;
    }

    public void setLabelPrivCADigest( TPM_DIGEST labelPrivCADigest )
    {
        this.labelPrivCADigest = labelPrivCADigest;
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal( int ordinal )
    {
        this.ordinal = ordinal;
    }

    public TPM_STRUCT_VER getVer()
    {
        return ver;
    }

    public void setVer( TPM_STRUCT_VER ver )
    {
        this.ver = ver;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( ver, ordinal, labelPrivCADigest, identityPubKey );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.ver = new TPM_STRUCT_VER();
        brw.readStruct( this.ver );
        this.ordinal = brw.readInt32();
        this.labelPrivCADigest = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ) );
        this.identityPubKey = new TPM_PUBKEY();
        brw.readStruct( this.identityPubKey );
    }

    public String toString()
    {
        return "TPM_IDENTITY_CONTENTS: \n"
            + "ver = " + this.ver + "\n"
            + "ordinal = 0x" + Integer.toHexString( this.ordinal ) + "\n"
            + "labelPrivCADigest = " + this.labelPrivCADigest + "\n"
            + "identityPubKey: " + this.identityPubKey;
    }
}
