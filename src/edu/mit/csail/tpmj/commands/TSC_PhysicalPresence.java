/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2007
 */
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.TPMJDisallowedException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

/**
 * Avoid using this class directly; use TPMAdminFuncs.assertPhysicalPresence instead.
 * This class is used for asserting physical presence on the Intel Mac.
 * This should not work on other machines.
 * For safety, the "A1" flags, which refer to lifetime settings are disallowed
 * by TPM/J, and will return a TPM/J-specific error code.
 * 
 * @author lfgs
 */
public class TSC_PhysicalPresence extends TPMCommand
{
    public static final short A1mask = TPMConsts.TPM_PHYSICAL_PRESENCE_LIFETIME_LOCK
        | TPMConsts.TPM_PHYSICAL_PRESENCE_HW_ENABLE
        | TPMConsts.TPM_PHYSICAL_PRESENCE_CMD_ENABLE
        | TPMConsts.TPM_PHYSICAL_PRESENCE_HW_DISABLE
        | TPMConsts.TPM_PHYSICAL_PRESENCE_CMD_DISABLE;
    
    

    private short physicalPresence;

    public TSC_PhysicalPresence( short physicalPresence )
    {
        super( TPMConsts.TPM_TAG_RQU_COMMAND, 12,
            TPMConsts.TSC_ORD_PhysicalPresence );
        this.setPhysicalPresence( physicalPresence );
    }

    public short getPhysicalPresence()
    {
        return physicalPresence;
    }

    public void setPhysicalPresence( short physicalPresence )
    {
        if ( this.hasLifetimeSettings( physicalPresence ) )
        {
            throw new IllegalArgumentException( "TPM/J does not allow TSC_PhysicalPresence to attempt to set lifetime settings" );
        }
        this.physicalPresence = physicalPresence;
    }
    
    public static boolean hasLifetimeSettings( short physicalPresence )
    {
        return ( physicalPresence & A1mask ) != 0;
    }
    
    public boolean hasLifetimeSettings()
    {
        return hasLifetimeSettings( this.getPhysicalPresence() );
    }

    // Return Output Struct
    /**
     * Returns ByteArrayTPMOutputStruct.class
     */
    public Class getReturnType()
    {
        return ByteArrayTPMOutputStruct.class;
    }

    @Override
    public ByteArrayTPMOutputStruct execute( TPMDriver tpmDriver )
        throws TPMException
    {
        if ( this.hasLifetimeSettings() )
        {
            throw new TPMJDisallowedException( this );
        }
        return (ByteArrayTPMOutputStruct) super.execute( tpmDriver );
    }

    /**
     * Just writes the header (tag, paramSize, and ordinal).
     */
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.physicalPresence );
    }

    /**
     * Just reads the header.
     */
    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setPhysicalPresence( brw.readShort() );
    }

}
