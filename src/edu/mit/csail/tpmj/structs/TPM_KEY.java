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
import edu.mit.csail.tpmj.util.CryptoUtil;

/**
 * This is the class used for both TPM_KEY and TPM_KEY12 structures.
 * We do not use a separate TPM_KEY12 class because it will probably
 * be common practice to read a byte array using fromBytes into a TPM_KEY structure
 * without first knowing whether it is a TPM_KEY or TPM_KEY12.
 *   
 * 
 * @author lfgs
 *
 */
public class TPM_KEY extends SimpleTaggedTPMStruct
{
    /* From the TPM Structures specification:
     typedef struct tdTPM_KEY{
     TPM_STRUCT_VER ver;
     TPM_KEY_USAGE keyUsage;
     TPM_KEY_FLAGS keyFlags;
     TPM_AUTH_DATA_USAGE authDataUsage;
     TPM_KEY_PARMS algorithmParms;
     UINT32 PCRInfoSize;
     BYTE* PCRInfo;
     TPM_STORE_PUBKEY pubKey;
     UINT32 encDataSize;
     [size_is(encDataSize)] BYTE* encData;
     TPM_KEY;     

     typedef struct tdTPM_KEY12{
     TPM_STRUCTURE_TAG tag;
     UINT16 fill;
     TPM_KEY_USAGE keyUsage;
     TPM_KEY_FLAGS keyFlags;
     TPM_AUTH_DATA_USAGE authDataUsage;
     TPM_KEY_PARMS algorithmParms;
     UINT32 PCRInfoSize;
     BYTE* pcrInfoBytes;
     TPM_STORE_PUBKEY pubKey;
     UINT32 encDataSize;
     [size_is(encDataSize)] BYTE* encDataBytes;
     } TPM_KEY12;
     */

    private short fill;
    private short keyUsage; // TPM_KEY_USAGE 
    private int keyFlags;
    private byte authDataUsage;
    private TPM_KEY_PARMS algorithmParms = new TPM_KEY_PARMS();
    private byte[] pcrInfoBytes = new byte[0];
    private TPM_STORE_PUBKEY pubKey = new TPM_STORE_PUBKEY();
    private byte[] encDataBytes = new byte[0];

    /**
     * Constructor using tag.  For TPM_KEY12 structure, use TPMConsts.TPM_TAG_KEY12.
     * For 1.1 style TPM_KEY, use 0x0101 (not TPMConsts.TPM_TAG_KEY),
     * or use the constructor with a TPM_STRUCT_VER
     * 
     * @param tag
     * @param keyUsage
     * @param keyFlags
     * @param authDataUsage
     * @param algorithmParms
     * @param pcrInfoBytes
     * @param pubKey
     * @param encDataBytes
     */
    public TPM_KEY( short tag, short keyUsage, int keyFlags, byte authDataUsage,
        TPM_KEY_PARMS algorithmParms, byte[] pcrInfoBytes,
        TPM_STORE_PUBKEY pubKey, byte[] encDataBytes )
    {
        super( tag );

        //        // FIXME: This is tied to TPMUtilityFuncs
        //        if ( TPMUtilityFuncs.getTPMDriver().isTPM11() )
        //        {
        //            this.setTag( (short) 0x0101 );
        //        }

        this.fill = 0x0000;
        this.keyUsage = keyUsage;
        this.keyFlags = keyFlags;
        this.authDataUsage = authDataUsage;
        this.setAlgorithmParms( algorithmParms );
        this.setPcrInfoBytes( pcrInfoBytes );
        this.pubKey = pubKey;
        this.setEncDataBytes( encDataBytes );
    }

    /**
     * Empty constructor for use with readStruct, or for
     * building key by using setter methods (initializes tag to 1.1 style TPM_KEY).
     */
    public TPM_KEY()
    {
        super( (short) 0x0101 ); // initialize as TPM_KEY (1.1)
        // FIXME: Make initializing fields to default non-null values an idiom for all structs
    }

    /**
     * Constructor for 1.1-stle TPM_KEY, using TPM_STRUCT_VER.
     * Typical use is to use TPM_STRUCT_VER.TPM_1_1_VER
     * 
     * @param ver
     * @param keyUsage
     * @param keyFlags
     * @param authDataUsage
     * @param algorithmParms
     * @param pcrInfoBytes
     * @param pubKey
     * @param encDataBytes
     */
    public TPM_KEY( TPM_STRUCT_VER ver, short keyUsage, int keyFlags, byte authDataUsage,
        TPM_KEY_PARMS algorithmParms, byte[] pcrInfoBytes,
        TPM_STORE_PUBKEY pubKey, byte[] encDataBytes )
    {
        this( (short) 0x0101, keyUsage, keyFlags, authDataUsage, algorithmParms, pcrInfoBytes, pubKey, encDataBytes );
        this.setVer( ver );
    }
    
    /**
     * Construct a TPM_KEY instance from a keyBlob.
     * 
     * @param keyBlob
     */
    public TPM_KEY( byte[] keyBlob )
    {
        this.fromBytes( keyBlob, 0 );
    }

    /**
     * Returns true if this.getTag() is 0x0101,
     * which is the first two bytes of the TPM_STRUCT_VER structure in
     * TPM_KEY structures from TPM version 1.1.
     * 
     * @return
     */
    public boolean isTPM_KEY11()
    {
        return this.getTag() == 0x0101;
    }

    /**
     * Returns true if the tag is TPM_TAG_KEY12
     * 
     * @return
     */
    public boolean isTPM_KEY12()
    {
        return this.getTag() == TPMConsts.TPM_TAG_KEY12;
    }
    
    public TPM_STRUCT_VER getVer()
    {
        byte major = (byte) ( (this.getTag() >> 8) & 0xff );
        byte minor = (byte) ( this.getTag() & 0xff );
        byte revMajor = (byte) ( (this.getFill() >> 8) & 0xff );
        byte revMinor = (byte) ( this.getFill() & 0xff );
        return new TPM_STRUCT_VER( major, minor, revMajor, revMinor );
    }
    
    public void setVer( TPM_STRUCT_VER ver )
    {
        byte major = ver.getMajor();
        byte minor = ver.getMinor();
        byte revMajor = ver.getRevMajor();
        byte revMinor = ver.getRevMinor();
        short tag = (short) ((major << 8) | minor);
        short fill = (short) ((revMajor << 8) | revMinor);
        this.setTag( tag );
        this.setFill( fill );
    }

    public TPM_KEY_PARMS getAlgorithmParms()
    {
        return algorithmParms;
    }

    public void setAlgorithmParms( TPM_KEY_PARMS algorithmParms )
    {
        this.algorithmParms = algorithmParms;
    }

    public byte getAuthDataUsage()
    {
        return authDataUsage;
    }

    public void setAuthDataUsage( byte authDataUsage )
    {
        this.authDataUsage = authDataUsage;
    }

    public byte[] getEncDataBytes()
    {
        return encDataBytes;
    }

    public void setEncDataBytes( byte[] encData )
    {
        this.encDataBytes = encData;
    }

    public short getFill()
    {
        return fill;
    }

    public void setFill( short fill )
    {
        this.fill = fill;
    }

    public int getKeyFlags()
    {
        return keyFlags;
    }

    public void setKeyFlags( int keyFlags )
    {
        this.keyFlags = keyFlags;
    }

    public short getKeyUsage()
    {
        return keyUsage;
    }

    public void setKeyUsage( short keyUsage )
    {
        this.keyUsage = keyUsage;
    }

    public byte[] getPcrInfoBytes()
    {
        return pcrInfoBytes;
    }

    public void setPcrInfoBytes( byte[] info )
    {
        pcrInfoBytes = info;
    }

    public TPM_STORE_PUBKEY getPubKey()
    {
        return pubKey;
    }

    public void setPubKey( TPM_STORE_PUBKEY pubKey )
    {
        this.pubKey = pubKey;
    }

    public int getEncDataSize()
    {
        return this.encDataBytes.length;
    }

    public int getPCRInfoSize()
    {
        return this.pcrInfoBytes.length;
    }

    public byte[] getPubDataBytes()
    {
        byte[] concat = ByteArrayUtil.buildBuf( this.getTag(), this.fill,
            this.keyUsage, this.keyFlags, this.authDataUsage,
            this.algorithmParms, this.getPCRInfoSize(), this.pcrInfoBytes,
            this.pubKey );
        return concat;
    }

    public TPM_DIGEST getPubDataDigest()
    {
        byte[] concat = this.getPubDataBytes();
        return CryptoUtil.computeTPM_DIGEST( concat );
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.fill, this.keyUsage,
            this.keyFlags, this.authDataUsage, this.algorithmParms,
            this.getPCRInfoSize(), this.pcrInfoBytes, this.pubKey,
            this.getEncDataSize(), this.encDataBytes );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.setFill( brw.readShort() );
        this.keyUsage = brw.readShort();
        this.keyFlags = brw.readInt32();
        this.authDataUsage = brw.readByte();
        this.algorithmParms = new TPM_KEY_PARMS();
        brw.readStruct( this.algorithmParms );
        int pcrInfoSize = brw.readInt32();
        this.setPcrInfoBytes( brw.readBytes( pcrInfoSize ) );
        this.pubKey = new TPM_STORE_PUBKEY();
        brw.readStruct( this.pubKey );
        int encDataSize = brw.readInt32();
        this.setEncDataBytes( brw.readBytes( encDataSize ) );
    }

    @Override
    public String toString()
    {
        String ret = "Tag: 0x" + Integer.toHexString( this.getTag() ) + "\n"
            + "Fill: 0x" + Integer.toHexString( this.fill ) + "\n"
            + "Key Usage: 0x" + Integer.toHexString( this.keyUsage ) + "\n"
            + "Key Flags: 0x" + Integer.toHexString( this.keyFlags ) + "\n"
            + "AuthDataUsage: 0x" + Integer.toHexString( this.authDataUsage )
            + "\n" + "algorithmParms: " + this.algorithmParms + "\n"
            + "PCRInfo (" + this.getPCRInfoSize() + " bytes): "
            + ByteArrayUtil.toHexString( this.pcrInfoBytes ) + "\n"
            + "pubKey: " + this.pubKey + "\n" + "encData ("
            + this.getEncDataSize() + " bytes): "
            + ByteArrayUtil.toPrintableHexString( this.encDataBytes ) + "\n";
        return ret;
    }

}
