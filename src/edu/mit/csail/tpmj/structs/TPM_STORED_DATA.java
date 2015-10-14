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

/**
 * See section 9.1 TPM_STORED_DATA of TPM Structures 1.2 Specification
 * 
 * @author lfgs
 */
public class TPM_STORED_DATA extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_STORED_DATA {
     *    TPM_STRUCT_VER ver;
     *    UINT32 sealInfoSize;
     *    [size_is(sealInfoSize)] BYTE* sealInfo;
     *    UINT32 encDataSize;
     *    [size_is(encDataSize)] BYTE* encData;
     * } TPM_STORED_DATA;
     * 
     */

    private TPM_STRUCT_VER ver;
    private TPM_PCR_INFO sealInfo;
    private byte[] encData;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_STORED_DATA()
    {
        // do nothing
    }
    
    /**
     * Construct using fromBytes 
     * 
     * @param bytes
     */
    public TPM_STORED_DATA( byte[] source )
    {
       this.fromBytes( source, 0 ); 
    }

    public TPM_STORED_DATA( TPM_PCR_INFO sealInfo, byte[] encData )
    {
        super();
        this.ver = TPM_STRUCT_VER.TPM_1_1_VER;
        this.sealInfo = sealInfo;
        this.encData = encData;
    }



    public byte[] getEncData()
    {
        return encData;
    }

    public void setEncData( byte[] encData )
    {
        this.encData = encData;
    }

    public TPM_PCR_INFO getSealInfo()
    {
        return sealInfo;
    }

    public void setSealInfo( TPM_PCR_INFO sealInfo )
    {
        this.sealInfo = sealInfo;
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
        byte[] sealInfoBytes = ( sealInfo == null ) ? new byte[0] : sealInfo.toBytes(); 
        
        return ByteArrayUtil.buildBuf( this.ver, sealInfoBytes.length, sealInfoBytes,
            this.encData.length, this.encData );
    }

    /**
     * Note: since the payload size is NOT specified here,
     * the caller must ensure that the rest of source,
     * starting from offset is all part of TPM_BOUND_DATA
     * <p>
     * Fortunately, however, there should be no need
     * to read TPM_BOUND_DATA from an array because
     * the TPM doesn't return TPM_BOUND_DATA structures.   
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.ver = new TPM_STRUCT_VER();
        brw.readStruct( this.ver );
        int sealInfoSize = brw.readInt32();
        if ( sealInfoSize > 0 )
        {
            byte[] sealInfoBytes = brw.readBytes( sealInfoSize );
            this.sealInfo = new TPM_PCR_INFO( sealInfoBytes );
        }
        else
        {
            this.sealInfo = null;
        }
        int encDataSize = brw.readInt32();
        this.encData = brw.readBytes( encDataSize );
    }

    public String toString()
    {
        return "TPM_STORED_DATA: " + "ver: " + this.ver + "\n"
            + "sealInfo: " + this.sealInfo + "\n"
            + "encData (" + encData.length + " bytes): " + ByteArrayUtil.toPrintableHexString( this.encData );
    }
}
