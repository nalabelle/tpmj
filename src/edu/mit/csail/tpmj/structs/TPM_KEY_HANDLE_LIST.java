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

/**
 * 5.7 TPM_KEY_HANDLE_LIST
 * <p>
 * TPM_KEY_HANDLE_LIST is a structure used to describe the handles of all keys currently
 * loaded into a TPM.
 * 
 * @author lfgs
 */
public class TPM_KEY_HANDLE_LIST extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_KEY_HANDLE_LIST {
     *      UINT16 loaded;
     *      [size_is(loaded)] TPM_KEY_HANDLE handle[];
     * } TPM_KEY_HANDLE_LIST;
     */

    private int[] handles;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_KEY_HANDLE_LIST()
    {
        // do nothing
    }

    public TPM_KEY_HANDLE_LIST( int[] keyHandles )
    {
        this.setHandles( keyHandles );
    }

    /**
     * Returns number of loaded handles reported in this structure.
     * 
     * @return
     */
    public short getLoaded()
    {
        return (short) this.handles.length;
    }

    public int[] getHandles()
    {
        return handles;
    }

    public void setHandles( int[] keyHandles )
    {
        this.handles = keyHandles;
    }

    @Override
    public byte[] toBytes()
    {
        short numHandles = this.getLoaded();
        int numEntries = ((int) numHandles) & 0xffff; // strip sign-extension if any
        byte[] buf = new byte[2 + (4 * numEntries)];
        ByteArrayReadWriter brw = new ByteArrayReadWriter( buf, 0 );
        brw.writeShort( numHandles );
        for ( int i = 0; i < numEntries; i++ )
        {
            brw.writeInt32( this.handles[i] );
        }
        return buf;
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        if ( source.length == 0 )
        {
            // NOTE: For some reason, I'm getting this on the STMicro chip
            this.handles = new int[0];
        }
        else
        {
            ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
            int numEntries = brw.readUInt16();
            this.handles = new int[numEntries];
            for ( int i = 0; i < numEntries; i++ )
            {
                this.handles[i] = brw.readInt32();
            }
        }
    }

    public String toString()
    {
        int numEntries = this.getLoaded();
        String ret = "TPM_KEY_HANDLES_LIST: " + numEntries + " loaded handles"
            + "\n";
        for ( int i = 0; i < numEntries; i++ )
        {
            ret = ret + "0x" + Integer.toHexString( this.handles[i] ) + "\n";
        }
        return ret;
    }

}
