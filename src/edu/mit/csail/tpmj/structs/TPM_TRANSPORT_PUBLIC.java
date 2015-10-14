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

public class TPM_TRANSPORT_PUBLIC extends SimpleTaggedTPMStruct
{
    private int transAttributes;
    private int algID;
    private short encScheme;

    public TPM_TRANSPORT_PUBLIC()
    {
        super( TPMConsts.TPM_TAG_TRANSPORT_PUBLIC );
    }
    
    public TPM_TRANSPORT_PUBLIC( int transAttributes, int algID, short encScheme )
    {
        super( TPMConsts.TPM_TAG_TRANSPORT_PUBLIC );
        this.transAttributes = transAttributes;
        this.algID = algID;
        this.encScheme = encScheme;
    }

    public int getAlgID()
    {
        return algID;
    }

    public void setAlgID( int algID )
    {
        this.algID = algID;
    }

    public short getEncScheme()
    {
        return encScheme;
    }

    public void setEncScheme( short encScheme )
    {
        this.encScheme = encScheme;
    }

    public int getTransAttributes()
    {
        return transAttributes;
    }

    public void setTransAttributes( int transAttributes )
    {
        this.transAttributes = transAttributes;
    }


    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.transAttributes,
            this.algID, this.encScheme );
    }    
    
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.setTransAttributes( brw.readInt32() );
        this.setAlgID( brw.readInt32() );
        this.setEncScheme( brw.readShort() );
    }

    @Override
    public String toString()
    {
        return "TPM_TRANSPORT_PUBLIC:\n" 
            + "transAttributes: " + this.transAttributes + "\n"
            + "algID: " + this.algID + "\n" 
            + "encScheme: " + this.encScheme + "\n";
    }

}
