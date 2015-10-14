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
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_OSAP extends TPMCommand
{
    public static final int ENTITYTYPE_OFFSET = 10;
    public static final int ENTITYVALUE_OFFSET = 12;
    public static final int NONCEODDOSAP_OFFSET = 16;

    private short entityType;
    private int entityValue;
    private TPM_NONCE nonceOddOSAP;

    public TPM_OSAP( short entityType, int entityValue, TPM_NONCE nonceOddOSAP )
    {
        super( TPMConsts.TPM_TAG_RQU_COMMAND, 36, TPMConsts.TPM_ORD_OSAP );
        this.setEntityType( entityType );
        this.setEntityValue( entityValue );
        this.setNonceOddOSAP( nonceOddOSAP );
    }

    public short getEntityType()
    {
        return entityType;
    }

    public void setEntityType( short entityType )
    {
        this.entityType = entityType;
    }

    public int getEntityValue()
    {
        return entityValue;
    }

    public void setEntityValue( int entityValue )
    {
        this.entityValue = entityValue;
    }

    public TPM_NONCE getNonceOddOSAP()
    {
        return nonceOddOSAP;
    }

    public void setNonceOddOSAP( TPM_NONCE nonceOddOSAP )
    {
        this.nonceOddOSAP = nonceOddOSAP;
    }

    public Class getReturnType()
    {
        return TPM_OSAPOutput.class;
    }

    @Override
    public TPM_OSAPOutput execute( TPMDriver tpmDriver ) throws TPMException
    {
        return (TPM_OSAPOutput) super.execute( tpmDriver );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.entityType, this.entityValue, this.nonceOddOSAP );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.entityType = brw.readShort();
        this.entityValue = brw.readInt32();
        this.nonceOddOSAP = new TPM_NONCE();
        brw.readStruct( this.nonceOddOSAP );
    }

}
