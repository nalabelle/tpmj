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
package edu.mit.csail.tpmj.counters;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.commands.TPMCommand;
import edu.mit.csail.tpmj.commands.TPM_IncrementCounter;
import edu.mit.csail.tpmj.commands.TPM_ReadCounter;
import edu.mit.csail.tpmj.commands.TPM_ReadOrIncCounterCommand;
import edu.mit.csail.tpmj.commands.TPM_ReadOrIncCounterOutput;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.transport.TPMTransportLog;
import edu.mit.csail.tpmj.transport.TPMTransportLogEntry;
import edu.mit.csail.tpmj.util.ByteArrayable;

/**
 * This is a specific implementation using a TPMTransportLog.
 * 
 * @author lfgs
 */
public class TPMCountStamp implements CountStamp
{
    private TPMTransportLog transLog;

    public TPMCountStamp()
    {
        // needed for instantiating
    }

    public TPMCountStamp( byte[] source, int offset )
    {
        this.fromBytes( source, offset );
    }

    public TPMCountStamp( TPMTransportLog transLog )
    {
        super();
        this.transLog = transLog;
    }

    public TPMTransportLog getTransLog()
    {
        return transLog;
    }

    /**
     * Returns the numeric value of the counter value in the stamp.
     * 
     * @return
     */
    public long getCount()
    {
        TPMTransportLogEntry logEntry = transLog.get( 1 );
        TPM_ReadOrIncCounterOutput incOut = (TPM_ReadOrIncCounterOutput) logEntry.getUnencOut();
        TPM_COUNTER_VALUE counterValue = incOut.getCount();
        // fix for sign extension
        long count = counterValue.getCounter() & 0xFFFFFFFFL;
        return count;
    }

    /**
     * Returns the operation type (TPM_ORD_ReadCounter or TPM_ORD_IncrementCounter).
     * 
     * @return
     */
    public int opType()
    {
        TPMTransportLogEntry logEntry = transLog.get( 1 );
        TPMCommand opCmd = logEntry.getOrigCmd();
        return opCmd.getOrdinal();
    }

    /**
     * Returns the first non-zero return code in the transport log.
     * (For execute transport, we check the error code of the
     * TPM_ExecuteTransport command first, and then that of the
     * wrapped command.)
     */
    public int getErrorCode()
    {
        int errorCode = 0;
        int i = 0;

        do
        {
            TPMTransportLogEntry logEntry = transLog.get( i );
            errorCode = logEntry.getTransOut().getReturnCode();
            if ( errorCode == 0 )
            {
                errorCode = logEntry.getUnencOut().getReturnCode();
            }
            i++;
        }
        while ( (i < transLog.size()) && (errorCode == 0) );

        return errorCode;
    }

    /**
     * Returns the counterID
     * Output type is implementation-specific.
     * 
     * @return
     */
    public TPMCounterID getCounterID()
    {
        TPMTransportLogEntry logEntry = transLog.get( 1 );

        TPMCommand cmd = logEntry.getOrigCmd();
        int countID = 0;
        if ( cmd instanceof TPM_ReadOrIncCounterCommand )
        {
            countID = ((TPM_ReadOrIncCounterCommand) cmd).getCountID();
        }
        else
        {
            return null;
        }

        TPM_ReadOrIncCounterOutput incOut = (TPM_ReadOrIncCounterOutput) logEntry.getUnencOut();
        if ( incOut.getReturnCode() != 0 )
        {
            return null;
        }
        TPM_COUNTER_VALUE counterValue = incOut.getCount();
        byte[] label = counterValue.getLabel();

        return new TPMCounterID( countID, label );
    }

    /**
     * Returns the nonce signed with the stamp.
     * Output type is implementation-specific.
     * 
     * @return
     */
    public TPM_NONCE getNonce()
    {
        return transLog.getAntiReplayNonce();
    }

    /**
     * Verifies validity of stamp based on verification key
     * given the modulus as a raw array of bytes 
     * 
     * @param verificationKey -- modulus bytes
     * @return
     */
    public boolean verify( byte[] verificationKey )
    {
        // For counter operations, keyMap is unnecessary because
        // no keys are used in any wrapped commands
        return this.transLog.verify( verificationKey, null );
    }
    
    public byte[] toBytes()
    {
        return this.transLog.toBytes();
    }

    public void fromBytes( byte[] source, int offset )
    {
        this.transLog = new TPMTransportLog( source, offset );
    }

    public String toString()
    {
        String ordName = "";
        switch ( this.opType() )
        {
            case TPMConsts.TPM_ORD_ReadCounter:
                ordName = "(TPM_ORD_ReadCounter)";
                break;
            case TPMConsts.TPM_ORD_IncrementCounter:
                ordName = "(TPM_ORD_IncrementCounter)";
                break;
        }
        
        return "TPMCountStamp:\n" 
            + "ID = " + this.getCounterID()
            + "\ncount = " + this.getCount()
            + "\nopType = 0x" + this.opType() + " " + ordName
            + "\nerror code = 0x" + this.getErrorCode() 
            + "\nnonce = " + this.getNonce();
    }
    
}
