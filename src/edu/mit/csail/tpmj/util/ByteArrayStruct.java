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
 * This class is a superclass used to put structure
 * to byte arrays.
 * <p>
 * Note: For now, this is a BIG ENDIAN structure.
 * TODO: Make base class abstract, then define
 * LittleEndian, BigEndian, and ChangeableEndian subclasses.
 * (Think about how to do global runtime changes 
 * to endian-ness, so structs
 * can switch between BigEndian and LittleEndian
 * without recompiling.  One way this can be
 * done is with a static boolean in the base class.)
 * 
 * @author lfgs
 */
public class ByteArrayStruct extends BasicByteArrayable
{
    private byte[] data;

    public ByteArrayStruct()
    {
        super();
    }
    
    /**
     * Constructs a new ByteArrayStruct using the source array
     * as the internal array.
     * 
     * @param source
     */
    public ByteArrayStruct( byte[] source )
    {
        this.data = source;
    }

    public ByteArrayStruct( int size )
    {
        this( new byte[size] );
    }


    protected byte[] getInternalByteArray()
    {
        return this.data;
    }

    protected void setInternalByteArray( byte[] data )
    {
        this.data = data;
    }

    /**
     * Points this instance's internal byte array to 
     * same internal byte array in the source.  When inherited
     * in a specific subclass, this allows one to create a
     * specific subclass "view" of the same bytes from
     * another generic or specific ByteArrayStruct.
     * See how this is used in TPMCommand.
     * 
     * @param source
     * @see TPMCommand
     */
    public void recast( ByteArrayStruct source )
    {
        this.setInternalByteArray( source.getInternalByteArray() );
    }

    /**
     * Read array of length bytes starting from offset.
     * 
     * @param offset -- offset from the original data array
     * @param length -- number of bytes to read
     * @return
     */
    protected byte[] getBytes( int offset, int length )
    {
        return ByteArrayUtil.readBytes( this.data, offset, length );
    }

    /**
     * Read 1-byte from offset as a boolean
     *
     * @param offset -- offset from the byte array
     * @return
     */
    protected boolean getBoolean( int offset )
    {
        return ByteArrayUtil.readBoolean( this.data, offset );
    }
    
    /**
     * Read a single byte from offset.
     * 
     * @param offset
     * @return
     */
    protected byte getByte( int offset )
    {
        return this.data[offset];
    }
    
    /**
     * Read 16-bit int from offset as a short
     *
     * @param offset -- offset from the byte array
     * @return
     */
    protected short getShort( int offset )
    {
        return ByteArrayUtil.readShortBE( this.data, offset );
    }
    
    /**
     * Read 16-bit int from offset as unsigned value
     * and return an int (to be used when the return
     * value will be used in a place where int's are
     * required, e.g., as an index or offset into an array).
     * Note that if you use getShort instead,
     * the short that is returned will automatically
     * be converted to an int, and any values above
     * 2^15-1 will be turned into a negative number
     * even though it's not supposed to be.
     *
     * @param offset -- offset from the byte array
     * @return
     */
    protected int getUInt16( int offset )
    {
        return ByteArrayUtil.readUInt16BE( this.data, offset );
    }
    
    /**
     * Read 32-bit int from offset
     * 
     * @param offset
     * @return
     */
    protected int getInt32( int offset )
    {
        return ByteArrayUtil.readInt32BE( this.data, offset );
    }
    
    protected long getLong( int offset )
    {
        return ByteArrayUtil.readLongBE( this.data, offset );
    }

    /**
     * Fill an instantiated ByteArrayable 
     * from the bytes starting from offset.
     * @param offset -- offset from internal byte array to start reading
     * @param ba -- instance of a ByteArrayable to call fromBytes on
     * 
     */
    protected void getStruct( int offset, ByteArrayable ba )
    {
        ba.fromBytes( this.data, offset );
    }

    /**
     * Write a boolean as 1 byte into offset
     * 
     * @param offset -- offset to write it into
     * @param b -- boolean value to write
     */
    protected void setBoolean( int offset, boolean b )
    {
        ByteArrayUtil.writeBoolean( this.data, offset, b );
    }

    /**
     * Writes a single byte into internal data array at offset.
     * 
     * @param offset -- offset to write it into
     * @param b -- 8-bit byte value
     */
    protected void setByte( int offset, byte b )
    {
        this.data[offset] = b;
    }
    
    /**
     * Write 16-bit integer into internal data array at offset.
     * (Writes MSB first.)
     * 
     * @param offset -- offset to write it into
     * @param w --16-bit integer value
     */
    protected void setShort( int offset, short w )
    {
        ByteArrayUtil.writeShortBE( this.data, offset, w );
    }

    /**
     * Write 32-bit integer into internal data array at offset.
     * (Writes MSB first.)
     * 
     * @param offset -- offset to write it into
     * @param w -- 32-bit integer value
     */
    protected void setInt32( int offset, int w )
    {
        ByteArrayUtil.writeInt32BE( this.data, offset, w );
    }
    
    protected void setLong( int offset, long l )
    {
        ByteArrayUtil.writeLongBE( this.data, offset, l );
    }

    /**
     * Write array of bytes into the internal data array starting at offset
     * 
     * @param offset -- target offset to write to
     * @param data -- the source array of bytes
     */
    protected void setBytes( int offset, byte[] data )
    {
        ByteArrayUtil.writeBytes( this.data, offset, data );
    }
    
    /**
     * Write a varargs list of byte arrays 
     * into the internal array starting at offset
     * 
     * @param offset -- target offset to write to
     * @param arrays -- any number of byte[] instances to write in sequence
     */
    protected void setBytes( int offset, byte[]... arrays )
    {
        this.setBytes( offset, ByteArrayUtil.concat( arrays ) );
    }

    /**
     * Write a ByteArrayable object into the internal array starting at offset
     * 
     * @param offset -- target offset to write to
     * @param source -- ByteArrayable object to write
     */
    protected void setBytes( int offset, ByteArrayable source )
    {
        this.setBytes( offset, source.toBytes() );
    }
    
    /**
     * Write a varargs list of Objects (including primitive types
     * or ByteArrayables) into the internal data array starting at offset
     * 
     * @param offset -- target offset to write to
     * @param fields -- any number of byte[] instances to write in sequence
     */
    protected void setBytes( int offset, Object... fields )
    {
        ByteArrayUtil.writeObjectsBE( this.data, offset, fields );
    }
    

    /**
     * Returns a clone of the internal byte array.
     */
    public byte[] toBytes()
    {
        return (byte[]) this.data.clone();
    }

    /**
     * Creates a new byte array of size source.length - offset
     * and copies the rest of source, starting from offset.
     * 
     * @param source -- byte array to read from
     * @param offset -- offset to read from
     */
    public void fromBytes( byte[] source, int offset )
    {
        this.data = ByteArrayUtil.readBytesToEnd( source, offset );
    }
}
