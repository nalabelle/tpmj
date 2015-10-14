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
 * See section 9.5 TPM_BOUND_DATA of TPM Structures 1.2 Specification
 * 
 * @author lfgs
 */
public class TPM_BOUND_DATA extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_BOUND_DATA {
     *      TPM_STRUCT_VER ver;
     *      TPM_PAYLOAD_TYPE payload;
     *      BYTE[] payloadData;
     * } TPM_BOUND_DATA;
     * 
     */

    private TPM_STRUCT_VER ver;
    private byte payload;
    private byte[] payloadData;

//    /**
//     * Note: This is the empty constructor for use with readStruct 
//     * or initializing using fromBytes,
//     * but note that unlike most TPMStructs, fromBytes expects
//     * the rest of the source array starting from offset
//     * to be the TPM_BOUND_DATA struct.  This is because
//     * the payloadData size is not specified, so there
//     * is no way to tell how many bytes to read.
//     * <p>
//     * Fortunately, however, there should be no need
//     * to read TPM_BOUND_DATA from an array because
//     * the TPM doesn't return TPM_BOUND_DATA structures.   
//     */
//    protected TPM_BOUND_DATA()
//    {
//        // do nothing
//    }
    
    public TPM_BOUND_DATA( TPM_STRUCT_VER ver, byte payload, byte[] payloadData )
    {
        this.ver = ver;
        this.payload = payload;
        this.payloadData = payloadData;
    }

    /**
     * Constructs a TPM_BOUND_DATA using the default (actually, required)
     * version of 1.1.0.0 and payload type TPM_PT_BIND.
     * @param payloadData
     */
    public TPM_BOUND_DATA( byte[] payloadData )
    {
        this( TPM_STRUCT_VER.TPM_1_1_VER, TPMConsts.TPM_PT_BIND, payloadData );
    }
    
    
    public byte getPayload()
    {
        return payload;
    }

    public void setPayload( byte payload )
    {
        this.payload = payload;
    }

    public byte[] getPayloadData()
    {
        return payloadData;
    }

    public void setPayloadData( byte[] payloadData )
    {
        this.payloadData = payloadData;
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
        /*
         * typedef struct tdTPM_BOUND_DATA {
         *      TPM_STRUCT_VER ver;
         *      TPM_PAYLOAD_TYPE payload;
         *      BYTE[] payloadData;
         * } TPM_BOUND_DATA;
         * 
         */

        return ByteArrayUtil.buildBuf( this.ver, this.payload, this.payloadData );
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
        this.payload = brw.readByte();

//        this.setPayloadData( brw.readBytesToEnd() );

        byte[] payloadBytes = brw.readBytes( source.length - offset );
        this.setPayloadData( payloadBytes );
    }

    public String toString()
    {
        return "TPM_BOUND_DATA: " + "ver: " + this.ver + "\n"
            + "payload type: " + this.payload + "\n"
            + "payloadData: " + ByteArrayUtil.toPrintableHexString( this.payloadData );
    }
}
