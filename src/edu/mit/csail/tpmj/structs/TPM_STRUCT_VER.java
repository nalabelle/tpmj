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

/**
 * Class for TPMStructVer structure.
 * (Note: this is only used in TPM 1.1.  For TPM 1.2, use TPM_VERSION
 * which is structurally the same, but defined separately in the spec.)
 * 
 * @author lfgs
 */
public class TPM_STRUCT_VER extends SimpleTPMStruct
{
    /**
     * A convenience constant for version 1.1.0.0,
     * which is used in several places in the spec.
     * Note that this constant is not named as such in the spec.
     */
    public static final TPM_STRUCT_VER TPM_1_1_VER = new TPM_STRUCT_VER(
        (byte) 1, (byte) 1, (byte) 0, (byte) 0 );

    public static final int STRUCT_SIZE = 4;
    public static final int MAJOR_OFFSET = 0;
    public static final int MINOR_OFFSET = 1;
    public static final int REVMAJOR_OFFSET = 2;
    public static final int REVMINOR_OFFSET = 3;

    private byte major;
    private byte minor;
    private byte revMajor;
    private byte revMinor;

    public TPM_STRUCT_VER()
    {
        super();
    }

    public TPM_STRUCT_VER( byte[] source )
    {
        super( source );
    }

    public TPM_STRUCT_VER( byte major, byte minor, byte revMajor, byte revMinor )
    {
        this.setMajor( major );
        this.setMinor( minor );
        this.setRevMajor( revMajor );
        this.setRevMinor( revMinor );
    }

    public byte[] toBytes()
    {
        byte[] a = new byte[STRUCT_SIZE];
        a[MAJOR_OFFSET] = this.getMajor();
        a[MINOR_OFFSET] = this.getMinor();
        a[REVMAJOR_OFFSET] = this.getRevMajor();
        a[REVMINOR_OFFSET] = this.getRevMinor();
        return a;
    }

    public void fromBytes( byte[] a, int offset )
    {
        this.setMajor( a[offset + MAJOR_OFFSET] );
        this.setMinor( a[offset + MINOR_OFFSET] );
        this.setRevMajor( a[offset + REVMAJOR_OFFSET] );
        this.setRevMinor( a[offset + REVMINOR_OFFSET] );
    }

    public void setMajor( byte major )
    {
        this.major = major;
    }

    public byte getMajor()
    {
        return major;
    }

    public void setMinor( byte minor )
    {
        this.minor = minor;
    }

    public byte getMinor()
    {
        return minor;
    }

    public void setRevMajor( byte revMajor )
    {
        this.revMajor = revMajor;
    }

    public byte getRevMajor()
    {
        return revMajor;
    }

    public void setRevMinor( byte revMinor )
    {
        this.revMinor = revMinor;
    }

    public byte getRevMinor()
    {
        return revMinor;
    }
}
