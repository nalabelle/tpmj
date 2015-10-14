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
package edu.mit.csail.tpmj.counters;

import edu.mit.csail.tpmj.util.BasicByteArrayable;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPMCounterID extends BasicByteArrayable
{
    private int countID;
    private byte[] label = new byte[4];

    public TPMCounterID()
    {
        // do nothing
    }

    /**
     * Constructs an instance from a copy of the byte array
     * (uses this.fromBytes).
     * 
     * @param source
     */
    public TPMCounterID( byte[] source )
    {
        this.fromBytes( source, 0 );
    }

    public TPMCounterID( int countID, byte[] label )
    {
        super();
        this.countID = countID;
        this.setLabel( label );
    }
    
    public TPMCounterID( int countID, String labelString )
    {
        super();
        this.countID = countID;
        byte[] labelBytes = labelString.getBytes();
        this.setLabel( labelBytes );
    }

    public int getCountID()
    {
        return countID;
    }

    public void setCountID( int countID )
    {
        this.countID = countID;
    }

    public byte[] getLabel()
    {
        return label;
    }

    public void setLabel( byte[] label )
    {
        if ( label.length != 4 )
        {
            throw new IllegalArgumentException(
                "TPMCounterID.label must have length 4" );
        }
        this.label = label;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.countID, this.label );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setCountID( brw.readInt32() );
        this.setLabel( brw.readBytes( 4 ) );
    }
    
    @Override
    public String toString()
    {
        return "[0x" + Integer.toHexString( this.countID ) + ", " + new String( this.label ) + "]";
    }
}
