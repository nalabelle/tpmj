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

public class TPM_COUNTER_VALUE extends SimpleTaggedTPMStruct
{
    private byte[] label = new byte[4];
    private int counter;

    public TPM_COUNTER_VALUE()
    {
        super( TPMConsts.TPM_TAG_COUNTER_VALUE );
    }

    public TPM_COUNTER_VALUE( byte[] label, int counter )
    {
        super( TPMConsts.TPM_TAG_COUNTER_VALUE );
        this.setLabel( label );
        this.counter = counter;
    }

    public int getCounter()
    {
        return counter;
    }

    public void setCounter( int counter )
    {
        this.counter = counter;
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
                "TPM_COUNTER_VALUE.label must have length 4" );
        }
        this.label = label;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.label, this.counter );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.setLabel( brw.readBytes( 4 ) );
        this.setCounter( brw.readInt32() );
    }

    @Override
    public String toString()
    {
        String labelString = "";
        
        if ( label != null )
        {
            char[] labelBytes = new char[4];
            for ( int i = 0; (i < 4) && (i < label.length); i++ )
            {
                labelBytes[i] = (char) label[i];
            }

            labelString = " (" + String.copyValueOf( labelBytes ) + ")";
        }

        return "TPM_COUNTER_VALUE: " + "label: 0x"
            + ByteArrayUtil.toHexString( this.label ) 
            + labelString
            + ", counter value: 0x" + Integer.toHexString( this.counter );
    }

}
