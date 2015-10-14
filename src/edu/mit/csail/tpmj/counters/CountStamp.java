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

import edu.mit.csail.tpmj.util.ByteArrayable;

/**
 * This is the abstract interface for a "count-stamp" (as opposed to "time-stamp")
 * object.  A specific implementation of it could be, for example,
 * one which uses a TPMTransportLog.
 * 
 * @author lfgs
 */
public interface CountStamp extends ByteArrayable
{
    public static final int NO_ERROR = 0;
    public static final int UNSPECIFIED_ERROR = -1;

    /**
     * Returns the numeric value of the counter value in the stamp.
     * 
     * @return
     */
    public long getCount();

    /**
     * Returns the operation type (e.g., read or increment).
     * The constants used here can be implementation-specific
     * (e.g., for TPM implementations, this should probably use
     * the TPM ordinals for TPM_ReadCounter and TPM_IncrementCounter.
     * 
     * @return
     */
    public int opType();

    /**
     * In case, there is an error, returns an error code.
     * 
     * @return
     */
    public int getErrorCode();

    /**
     * Returns the counterID
     * Output type is implementation-specific.
     * 
     * @return
     */
    public ByteArrayable getCounterID();

    /**
     * Returns the nonce signed with the stamp.
     * Output type is implementation-specific.
     * 
     * @return
     */
    public ByteArrayable getNonce();
    
    /**
     * Verifies validity of stamp based on verification key.
     * Type of verification key is implementation-specific.
     * (e.g., it can be the RSA modulus or another structure
     * expressed in bytes) 
     * 
     * @param verificationKey
     * @return
     */
    public boolean verify( byte[] verificationKey );
}
