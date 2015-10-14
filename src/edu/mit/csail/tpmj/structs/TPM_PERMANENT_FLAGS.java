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

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_PERMANENT_FLAGS extends SimpleTaggedTPMStruct
{
    private boolean disable;
    private boolean ownership;
    private boolean deactivated;
    private boolean readPubek;
    private boolean disableOwnerClear;
    private boolean allowMaintenance;
    private boolean physicalPresenceLifetimeLock;
    private boolean physicalPresenceHWEnable;
    private boolean physicalPresenceCMDEnable;
    private boolean CEKPUsed;
    private boolean TPMpost;
    private boolean TPMpostLock;
    private boolean FIPS;
    private boolean operator;
    private boolean enableRevokeEK;
    private boolean nvLocked;
    private boolean readSRKPub;
    private boolean tpmEstablished;
    // FIXME: Apparently, the TPM doesn't return this (neither Broadcom or STMicro)
//    private boolean maintenanceDone;

    
    
    
    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.buildBuf( this.getTag(), this.disable, this.ownership,
            this.deactivated, this.readPubek, this.disableOwnerClear,
            this.allowMaintenance, this.physicalPresenceLifetimeLock,
            this.physicalPresenceHWEnable, this.physicalPresenceCMDEnable,
            this.CEKPUsed, this.TPMpost, this.TPMpostLock, this.FIPS,
            this.operator, this.enableRevokeEK, this.nvLocked, this.readSRKPub,
            this.tpmEstablished );
            //, this.maintenanceDone );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.setTag( brw.readShort() );
        this.disable = brw.readBoolean();
        this.ownership = brw.readBoolean();
        this.deactivated = brw.readBoolean();
        this.readPubek = brw.readBoolean();
        this.disableOwnerClear = brw.readBoolean();
        this.allowMaintenance = brw.readBoolean();
        this.physicalPresenceLifetimeLock = brw.readBoolean();
        this.physicalPresenceHWEnable = brw.readBoolean();
        this.physicalPresenceCMDEnable = brw.readBoolean();
        this.CEKPUsed = brw.readBoolean();
        this.TPMpost = brw.readBoolean();
        this.TPMpostLock = brw.readBoolean();
        this.FIPS = brw.readBoolean();
        this.operator = brw.readBoolean();
        this.enableRevokeEK = brw.readBoolean();
        this.nvLocked = brw.readBoolean();
        this.readSRKPub = brw.readBoolean();
        this.tpmEstablished = brw.readBoolean();
        // this.maintenanceDone = brw.readBoolean();
    }

    @Override
    public String toString()
    {
        return "TPM_PERMANENT_FLAGS:\n" 
            + "disable: " + this.disable + "\n"
            + "ownership: " + this.ownership + "\n" 
            + "deactivated: " + this.deactivated + "\n" 
            + "readPubek: " + this.readPubek + "\n"
            + "disableOwnerClear: " + this.disableOwnerClear + "\n"      
            + "allowMaintenance: " + this.allowMaintenance + "\n"
            + "physicalPresenceLifetimeLock: "
            + this.physicalPresenceLifetimeLock + "\n"
            + "physicalPresenceHWEnable: " + this.physicalPresenceHWEnable + "\n" 
            + "physicalPresenceCMDEnable: " + this.physicalPresenceCMDEnable + "\n" 
            + "CEKPUsed: " + this.CEKPUsed + "\n" 
            + "TPMpost: " + this.TPMpost + "\n"
            + "TPMpostLock: " + this.TPMpostLock + "\n" 
            + "FIPS: " + this.FIPS + "\n" 
            + "operator: " + this.operator + "\n"
            + "enableRevokeEK: " + this.enableRevokeEK + "\n" 
            + "nvLocked: " + this.nvLocked + "\n" 
            + "readSRKPub: " + this.readSRKPub + "\n"
            + "tpmEstablished: " + this.tpmEstablished + "\n";
            // + "maintenanceDone: " + this.maintenanceDone + "\n";
    }

    public boolean isAllowMaintenance()
    {
        return allowMaintenance;
    }

    public boolean isCEKPUsed()
    {
        return CEKPUsed;
    }

    public boolean isDeactivated()
    {
        return deactivated;
    }

    public boolean isDisable()
    {
        return disable;
    }

    public boolean isDisableOwnerClear()
    {
        return disableOwnerClear;
    }

    public boolean isEnableRevokeEK()
    {
        return enableRevokeEK;
    }

    public boolean isFIPS()
    {
        return FIPS;
    }

    public boolean isNvLocked()
    {
        return nvLocked;
    }

    public boolean isOperator()
    {
        return operator;
    }

    public boolean isOwnership()
    {
        return ownership;
    }

    public boolean isPhysicalPresenceCMDEnable()
    {
        return physicalPresenceCMDEnable;
    }

    public boolean isPhysicalPresenceHWEnable()
    {
        return physicalPresenceHWEnable;
    }

    public boolean isPhysicalPresenceLifetimeLock()
    {
        return physicalPresenceLifetimeLock;
    }

    public boolean isReadPubek()
    {
        return readPubek;
    }

    public boolean isReadSRKPub()
    {
        return readSRKPub;
    }

    public boolean isTpmEstablished()
    {
        return tpmEstablished;
    }

    public boolean isTPMpost()
    {
        return TPMpost;
    }

    public boolean isTPMpostLock()
    {
        return TPMpostLock;
    }

}
