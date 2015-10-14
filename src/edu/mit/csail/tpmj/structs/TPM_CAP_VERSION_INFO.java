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
 * 21.6 TPM_CAP_VERSION_INFO
 * (from TPM 1.2 Structures Spec)
 * This structure is in use both as input to a getcap and as an output to a getcap request.
 * When in use for input, the caller is asking if the TPM supports a certain version, or portion
 * of a version.
 * For instance the input for version could contain, 1.2.0.0, which means the question is does
 * the TPM support the main spec, version 1.2, regardless of manufacturer version. The TPM
 * would return TRUE if the TPM supported all 1.2 TPM main spec commands.
 * If the input was 1.2.3.1, the question is does the TPM support main spec version 1.2 AND
 * manufacturer firmware version 3.1. The TPM would return TRUE if the TPM supported the
 * main spec AND the proper firmware.
 * The spec letter allows the TPM to respond to 1.2a or 1.2b questions.
 * The vendor specific area allows the TPM vendor to provide support for vendor options. The
 * TPM vendor may define the area to the TPM vendor’s needs.
 * When in use for output, the TPM is returning the current version of the TPM
 *
 * @author lfgs
 */
public class TPM_CAP_VERSION_INFO extends SimpleTaggedTPMStruct
{
    public static final int VERSION_OFFSET = 2;
    public static final int SPECLEVEL_OFFSET = 6;
    public static final int ERRATAREV_OFFSET = 8;
    public static final int TPMVENDORID_OFFSET = 9;
    public static final int VENDORSPECIFICSIZE_OFFSET = 13;
    public static final int VENDORSPECIFIC_OFFSET = 15;

    // NOTE: It takes at least the same number of calls
    // that access the byte array directly whether we do it
    // with separate fields or use a TPMByteArrayStruct.
    // Using separate fields has the advantage that it is
    // more type-safe, more object-oriented, and
    // allows us to use Eclipse to generate the constructor,
    // and the getter/setter methods automatically.

    private TPM_VERSION version;
    private short specLevel;
    private byte errataRev; // TODO: TPM_VERSION_BYTE
    private int tpmVendorID;
    private byte[] vendorSpecific = new byte[0];

    public TPM_CAP_VERSION_INFO()
    {
        super();
    }

    public TPM_CAP_VERSION_INFO( byte[] source )
    {
        super( source );
    }

    public TPM_CAP_VERSION_INFO( TPM_VERSION version, short specLevel,
        byte errataRev, int tpmVendorID, byte[] vendorSpecific )
    {
        this.setTag( TPMConsts.TPM_TAG_CAP_VERSION_INFO );
        this.version = version;
        this.specLevel = specLevel;
        this.errataRev = errataRev;
        this.tpmVendorID = tpmVendorID;
        this.setVendorSpecific( vendorSpecific );
    }

    public byte getErrataRev()
    {
        return errataRev;
    }

    public void setErrataRev( byte errataRev )
    {
        this.errataRev = errataRev;
    }

    public int getSpecLevel()
    {
        return specLevel;
    }

    public void setSpecLevel( short specLevel )
    {
        this.specLevel = specLevel;
    }

    public int getTpmVendorID()
    {
        return tpmVendorID;
    }

    public void setTpmVendorID( int tpmVendorID )
    {
        this.tpmVendorID = tpmVendorID;
    }

    /**
     * Returns the vendor specific structure, or a zero-length byte[]
     * if there is none.
     * <p>
     * Note: Using zero-length arrays gives us more flexibility (e.g.,
     * we don't have to check the size or check for null when writing
     * (and in this case, when reading).
     * 
     * @return
     */
    public byte[] getVendorSpecific()
    {
        return vendorSpecific;
    }

    /**
     * Sets the vendor specific structure.
     * If vendorSpecific is null, then the internal structure is
     * set to be a zero-length byte[] to be consistent with getVendorSpecific.
     */
    public void setVendorSpecific( byte[] vendorSpecific )
    {
        if ( vendorSpecific == null )
        {
            this.vendorSpecific = new byte[0];
        }
        else
        {
            this.vendorSpecific = vendorSpecific;
        }
    }

    public int getVendorSpecificSize()
    {
        if ( this.vendorSpecific == null )
        {
            return 0;
        }
        else
        {
            return this.vendorSpecific.length;
        }
    }

    public TPM_VERSION getVersion()
    {
        return version;
    }

    public void setVersion( TPM_VERSION version )
    {
        this.version = version;
    }

    public byte[] toBytes()
    {
        short vendorSpecificSize = (vendorSpecific == null)
            ? 0
            : (short) this.vendorSpecific.length;
        return ByteArrayUtil.buildBuf( this.getTag(), this.version, this.specLevel,
            this.errataRev, this.tpmVendorID, vendorSpecificSize,
            this.vendorSpecific );
    }

    // TODO: Define a special exception for tag mismatch
    // TODO: Define tag value as a static final int in each structure class (?)
    // TODO: Define a superclass for tagged TPM structs (?)
    // TODO: Make tag pre-defined in class and unchangeable.

    public void fromBytes( byte[] source, int offset )
    {
        boolean okTag = this.readAndVerifyTag( source,
            offset + this.TAG_OFFSET, TPMConsts.TPM_TAG_CAP_VERSION_INFO );
        if ( !okTag )
        {
            // FIXME: Have some sort of TPMStruct deserialization exception
            System.err.println( "TPM_CAP_VERSION_INFO Tag Mismatch! Bytes: "
                + ByteArrayUtil.toPrintableHexString( ByteArrayUtil.readBytesToEnd( source, offset )));
            throw new IllegalArgumentException();
        }

        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset
            + this.VERSION_OFFSET );

        this.version = new TPM_VERSION();
        brw.readStruct( this.version );
        this.specLevel = brw.readShort();
        this.errataRev = brw.readByte();
        this.tpmVendorID = brw.readInt32();

        int vendorSpecificSize = brw.readUInt16();

        this.vendorSpecific = brw.readBytes( vendorSpecificSize );
    }
}
