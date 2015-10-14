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

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_GetCapability extends TPMCommand
{
    public static final int CAPAREA_OFFSET = 10;
    public static final int SUBCAPSIZE_OFFSET = 14;
    public static final int SUBCAP_OFFSET = 18;

    private int capArea;
    private byte[] subCap = new byte[0];
    
    public TPM_GetCapability( int capArea, byte[] subCap )
    {
        super( TPMConsts.TPM_TAG_RQU_COMMAND, TPMConsts.TPM_ORD_GetCapability );

        this.setCapArea( capArea );
        
        // Note: this.setSubCap sets paramSize
        this.setSubCap( subCap );
    }

    public TPM_GetCapability( int capArea )
    {
        this( capArea, null );
    }
    
    public int getCapArea()
    {
        return capArea;
    }

    public void setCapArea( int capArea )
    {
        this.capArea = capArea;
    }

    public byte[] getSubCap()
    {
        return subCap;
    }

    public int getSubCapSize()
    {
        return this.subCap.length;
    }

    /**
     * Sets subCap array and automatically computes and
     * sets paramSize.
     * 
     * @param subCap
     */
    public void setSubCap( byte[] subCap )
    {
        if ( subCap == null )
        {
            subCap = new byte[0];
        }
        
        this.subCap = subCap;

        int subCapSize = this.getSubCapSize();
        this.setParamSize( SUBCAP_OFFSET + subCapSize );
    }

    // Return Output Struct
    
    public Class getReturnType()
    {
        return TPM_GetCapabilityOutput.class;
    }

    @Override
    public TPM_GetCapabilityOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_GetCapabilityOutput) super.execute(tpmDriver);
    }

    @Override
    public byte[] toBytes()
    {
        int subCapSize = this.getSubCapSize();
        return this.createHeaderAndBody( this.capArea, subCapSize, this.subCap );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        this.setCapArea( brw.readInt32() );
        int subCapSize = brw.readInt32();
        this.setSubCap( brw.readBytes( subCapSize ) );
    }
    
}
