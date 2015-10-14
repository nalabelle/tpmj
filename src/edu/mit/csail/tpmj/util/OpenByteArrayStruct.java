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
package edu.mit.csail.tpmj.util;

/**
 * This class can be used as a "view" into a ByteArrayable
 * or ByteArrayStruct to allow one to read directly into the byte array
 * by making the setter and getter methods public (instead of protected).
 * This can be used for "quick-and-dirty" temporary development
 * work.  If the source object is a ByteArrayStruct, then
 * this class sets its own internal array to the source's
 * array, making it possible to modify the original struct as well.
 * 
 * @author lfgs
 */
public class OpenByteArrayStruct extends ByteArrayStruct
{
    /**
     * Constructs an OpenByteArrayStruct given a 
     * ByteArrayable source.
     * If the source is not a ByteArrayStruct but is
     * a ByteArrayable, then this constructs a view using
     * toBytes.  Changes to this object will <b>not</b> change
     * the original object.  If the source is a ByteArrayStruct,
     * then this points this instance's internal byte array 
     * to the ByteArrayStruct's internal byte array.
     * This allows any modification done using this object
     * to affect the original object.
     * 
     * @param source
     */
    public OpenByteArrayStruct( ByteArrayable source )
    {
        if ( source instanceof ByteArrayStruct )
        {
            this.setInternalByteArray( ((ByteArrayStruct) source).getInternalByteArray() );
        }
        else
        {
            this.setInternalByteArray( source.toBytes() );
        }
    }

    /**
     * Create a new ByteArrayStruct of a particular size.
     * 
     * @param size
     */
    public OpenByteArrayStruct( int size )
    {
        super( size );
    }

    @Override
    public byte[] getInternalByteArray()
    {
        return super.getInternalByteArray();
    }

    @Override
    public byte getByte( int offset )
    {
        return super.getByte( offset );
    }

    @Override
    public byte[] getBytes( int offset, int length )
    {
        return super.getBytes( offset, length );
    }

    @Override
    public int getInt32( int offset )
    {
        return super.getInt32( offset );
    }

    @Override
    public short getShort( int offset )
    {
        return super.getShort( offset );
    }

    @Override
    public void getStruct( int offset, ByteArrayable ba )
    {
        super.getStruct( offset, ba );
    }

    @Override
    public int getUInt16( int offset )
    {
        return super.getUInt16( offset );
    }

    @Override
    public void setByte( int offset, byte b )
    {
        super.setByte( offset, b );
    }

    @Override
    public void setBytes( int offset, byte[]... arrays )
    {
        super.setBytes( offset, arrays );
    }

    @Override
    public void setBytes( int offset, byte[] data )
    {
        super.setBytes( offset, data );
    }

    @Override
    public void setBytes( int offset, ByteArrayable source )
    {
        super.setBytes( offset, source );
    }

    @Override
    public void setBytes( int offset, Object... fields )
    {
        super.setBytes( offset, fields );
    }

    @Override
    public void setInt32( int offset, int w )
    {
        super.setInt32( offset, w );
    }

    @Override
    public void setShort( int offset, short w )
    {
        super.setShort( offset, w );
    }

    @Override
    public boolean getBoolean( int offset )
    {
        return super.getBoolean( offset );
    }

    @Override
    public long getLong( int offset )
    {
        return super.getLong( offset );
    }

    @Override
    public void setBoolean( int offset, boolean b )
    {
        super.setBoolean( offset, b );
    }

    @Override
    public void setLong( int offset, long l )
    {
        super.setLong( offset, l );
    }

    
}
