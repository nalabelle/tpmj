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

public class TPM_KEY_PARMS extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_KEY_PARMS {
     *      TPM_ALGORITHM_ID algorithmID;
     *      TPM_ENC_SCHEME encScheme;
     *      TPM_SIG_SCHEME sigScheme;
     *      UINT32 parmSize;
     *      [size_is(parmSize)] BYTE* parms;
     * } TPM_KEY_PARMS;
     */

    private int algorithmID;
    private short encScheme;
    private short sigScheme;
    private byte[] parmsBytes;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_KEY_PARMS()
    {
        // do nothing
    }
    
    public TPM_KEY_PARMS( int algorithmID, short encScheme, short sigScheme,
        byte[] parmsBytes )
    {
        this.algorithmID = algorithmID;
        this.encScheme = encScheme;
        this.sigScheme = sigScheme;
        this.setParmsBytes( parmsBytes );
    }

    public TPM_KEY_PARMS( int algorithmID, short encScheme, short sigScheme,
        TPM_KEY_PARMS_Data parmData )
    {
        this.algorithmID = algorithmID;
        this.encScheme = encScheme;
        this.sigScheme = sigScheme;
        this.setParmData( parmData );
    }
    
    
    public int getAlgorithmID()
    {
        return algorithmID;
    }

    public void setAlgorithmID( int algorithmID )
    {
        this.algorithmID = algorithmID;
    }

    public short getEncScheme()
    {
        return encScheme;
    }

    public void setEncScheme( short encScheme )
    {
        this.encScheme = encScheme;
    }

    public TPM_KEY_PARMS_Data getParmData()
    {
        if ( (this.parmsBytes == null) || (this.parmsBytes.length == 0) )
        {
            return null;
        }
        return TPM_KEY_PARMS_Data.createKeyParmsData( this.algorithmID, this.parmsBytes );
    }
    
    public void setParmData( TPM_KEY_PARMS_Data parmData )
    {
        this.setParmsBytes( parmData.toBytes() );
    }
    
    public int getParmSize()
    {
        if ( this.parmsBytes == null )
        {
            return 0;
        }
        else
        {
            return this.parmsBytes.length;
        }
    }
    
    public byte[] getParmsBytes()
    {
        return parmsBytes;
    }

    public void setParmsBytes( byte[] parmsBytes )
    {
        this.parmsBytes = parmsBytes;
    }

    public short getSigScheme()
    {
        return sigScheme;
    }

    public void setSigScheme( short sigScheme )
    {
        this.sigScheme = sigScheme;
    }

    @Override
    public byte[] toBytes()
    {
        /*
         * typedef struct tdTPM_KEY_PARMS {
         *      TPM_ALGORITHM_ID algorithmID;
         *      TPM_ENC_SCHEME encScheme;
         *      TPM_SIG_SCHEME sigScheme;
         *      UINT32 parmSize;
         *      [size_is(parmSize)] BYTE* parms;
         * } TPM_KEY_PARMS;
         */

        return ByteArrayUtil.buildBuf(
            this.algorithmID,
            this.encScheme,
            this.sigScheme,
            this.getParmSize(), this.parmsBytes );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.algorithmID = brw.readInt32();
        this.encScheme = brw.readShort();
        this.sigScheme = brw.readShort();
        int parmSize = brw.readInt32();
        this.setParmsBytes( brw.readBytes( parmSize ) );
    }

    public String toString()
    {
        TPM_KEY_PARMS_Data parmData = this.getParmData();
        String parmDataString = ( parmData == null ) 
            ? ByteArrayUtil.toHexString( this.parmsBytes )
            : parmData.toString(); 
        
        return "TPM_KEY_PARMS: algorithmID= 0x" + Integer.toHexString( this.algorithmID )
            + ", encScheme= 0x" + Integer.toHexString( this.encScheme )
            + ", sigScheme= 0x" + Integer.toHexString( this.sigScheme )
            + "\nParm (" + this.getParmSize() + " bytes):\n" 
            + parmDataString;
    }
}
