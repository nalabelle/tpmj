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
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayStruct;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

/**
 * A TPMCommand is associated by the data structure that will
 * be transmitted to the TPM.  Each TPM command should be implemented
 * by extending this superclass, and defining constructors and
 * setter methods corresponding to ways that one can setup input
 * parameters to the command.
 * 
 * @author lfgs
 */
public abstract class TPMCommand extends SimpleTaggedTPMStruct implements TPMInputStruct
{
    public static final int BODY_OFFSET = 10;

    private int paramSize;
    private int ordinal;

    /**
     * Constructs a TPMCommand with tag and ordinal.
     * Note that this.setParamSize() MUST be called 
     * in the subclass's constructor some time
     * after calling this constructor.
     * 
     * @param tag
     * @param ordinal
     */
    public TPMCommand( short tag, int ordinal )
    {
        super( tag );
        this.setOrdinal( ordinal );
    }
    
    /**
     * Constructs a TPMCommand with tag, paramSize, and ordinal.
     * 
     * @param tag
     * @param ordinal
     */
    public TPMCommand( short tag, int paramSize, int ordinal )
    {
        this( tag, ordinal );
        this.setParamSize( paramSize );
    }

    public int getParamSize()
    {
        return paramSize;
    }

    public void setParamSize( int paramSize )
    {
        this.paramSize = paramSize;
    }

    public int getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal( int ordinal )
    {
        this.ordinal = ordinal;
    }
    
    /**
     * Read the paramSize from the given offset.
     * (Note: the offset here is the absolute offset of the paramSize
     * itself in source.)
     * 
     * @param source
     * @param offset
     */
    public void readParamSize( byte[] source, int offset )
    {
        this.paramSize = ByteArrayUtil.readInt32BE( source, offset );
    }
    
    /**
     * Read the ordinal from the given offset.
     * (Note: the offset here is the absolute offset of the ordinal
     * itself in source.)
     * 
     * @param source
     * @param offset
     */
    public void readOrdinal( byte[] source, int offset )
    {
        this.ordinal = ByteArrayUtil.readInt32BE( source, offset );
    }

    /**
     * Read the tag, paramSize, and ordinal starting from offset.
     * 
     * @param source
     * @param offset
     */
    protected void readHeader( byte[] source, int offset )
    {
        this.readTag( source, offset + TAG_OFFSET );
        this.readParamSize( source, offset + PARAMSIZE_OFFSET );
        this.readOrdinal( source, offset + ORDINAL_OFFSET );
    }
    
    /**
     * Write the tag, paramSize and ordinal to the destination and offset
     * @param source
     * @param offset
     */
    protected void writeHeader( byte[] dest, int offset )
    {
        ByteArrayUtil.writeObjectsBE( dest, offset, this.getTag(), this.getParamSize(), this.getOrdinal() );
    }
    
    /**
     * This is used in createHeaderAndBody to write out bytes,
     * including the header, any body fields, and any footer
     * fields such as the authorization data.  Override this
     * method in subclases, to make any changes to header
     * or footer.
     * 
     * @param dest
     * @param offset
     * @param bodyFields
     */
    protected void writeHeaderAndBody( byte[] dest, int offset, Object... bodyFields )
    {
        this.writeHeader( dest, offset );
        ByteArrayUtil.writeObjectsBE( dest, offset + BODY_OFFSET, bodyFields );
    }
    
    /**
     * Given the "body" fields of the command in a varargs list,
     * returns a new byte array including the header and footer 
     * (authorization data, if applicable). 
     * Subclasses of TPMCommand can call this in
     * their definition of <code>toBytes()</code>
     * in order to write out their fields.
     * Subclasses should override writeHeaderAndBody to add
     * things like footers, etc.
     * 
     * @param bodyFields
     * @return
     * 
     * @see writeHeaderAndBody
     */
    protected byte[] createHeaderAndBody( Object... bodyFields )
    {
        byte[] buf = new byte[this.getParamSize()];
        this.writeHeaderAndBody( buf, 0, bodyFields );
        return buf;
    }
    
    protected ByteArrayReadWriter createBodyReadWriter( byte[] source, int structStartOffset )
    {
        return new ByteArrayReadWriter( source, structStartOffset + this.BODY_OFFSET );
    }

    public abstract Class getReturnType();

    /**
     * Transmits the command input struct to the tpmDriver,
     * and returns the output as an instance of the class
     * specified by <code>this.getReturnType()</code>.
     * <p>
     * Subclasses should override this method and make
     * the return type of the method the same as the class
     * specified by <code>getReturnType()</code>.
     * The body of the overriding version of execute
     * can just be
     * <code>
     * return (MyOutputStructType) super.execute( tpmDriver );
     * </code>
     * (Note: This is a new feature in Java 5.0 
     * called covariant return types.)
     *  
     * @param tpmDriver
     * @return
     * @throws TPMException
     */
    public TPMOutputStruct execute( TPMDriver tpmDriver ) throws TPMException
    {
        TPMOutputStruct ret = null;
        try
        {
            ret = (TPMOutputStruct) this.getReturnType().newInstance();
        }
        catch ( Exception e )
        {
            throw new TPMException( e );
        }
        // Note: this may throw an exception, in which case,
        // ret is never returned.
        TPMOutputStruct rawOutput = tpmDriver.transmit( this );

        if ( rawOutput instanceof ByteArrayTPMOutputStruct )
        {
            // NOTE: we check rawOutput for ByteArrayTPMOutputStruct here instead of
            // just ByteArrayStruct because otherwise, we
            // don't have access to getInternalByteArray below.
            
            if ( ret instanceof ByteArrayStruct )
            {
                // if ret type is a ByteArrayStruct anyway, use recast
                // so we don't waste time creating a copy of the bytes
                ((ByteArrayStruct) ret).recast( (ByteArrayStruct) rawOutput );
            }
            else
            {
                // if ret is not a ByteArrayStruct,
                // then use fromBytes.  Note that in this case,
                // The original bytes cannot be changed,
                // but it doesn't matter because the original bytes
                // are not exposed outside of this method anyway.
                ret.fromBytes( ((ByteArrayTPMOutputStruct) rawOutput).getInternalByteArray(), 0 );
            }
        }
        else
        {
            ret.fromBytes( rawOutput.toBytes(), 0 );
        }

        return ret;
    }
}
