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
package edu.mit.csail.tpmj.drivers;

import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.TPMIOException;
import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_STRUCT_VER;

public interface TPMDriver
{
    public static final int TPM_MANUFACTURER_UNKNOWN = -1;
    public static final int TPM_MANUFACTURER_INFINEON = 0x49465800; // "IFX\0"
    public static final int TPM_MANUFACTURER_BROADCOM = 0x4252434d; // "BRCM"
    
    /**
     * Initialize the TPM (including getting and caching the manufacturer ID
     * and other TPM-related info, if necessary).
     * 
     * @return
     */
    public void init();

    // TODO: Define TDDL-like functions, including GetCapability

    /**
     * Returns the version of the TPM.  
     * (Because a TPM 1.2 chip would always return 1.1.0.0 
     * if the version 1.1 way of getting the version is used,
     * implementations of this method should first try the version 1.2 way of getting
     * the version, and then try the 1.1 way.)
     */
    public TPM_STRUCT_VER getTPMVersion();
    
    public boolean isTPM11();
    
    public boolean isTPM12();
    
    
    /**
     * Returns the TPM manufacturer ID (as returned by GetCapability).
     * 
     * @return
     */
    public int getTPMManufacturer();
    
    
    
    /**
     * This is one step above the low-level interface to the TPM.
     * It includes wrapping/unwrapping into TPMIOStruct objects.
     * Transmits command to TPM and throws TPMException on error.
     * If a TPMErrorReturnCodeException is thrown, the 
     * return code and output struct returned by the TPM can be retrieved from
     * the TPMErrorReturnCodeException object.
     * 
     * @param input
     * @return
     * @throws TPMException: TPMIOException on I/O error, TPMErrorReturnCodeException if output return code is not 0.
     */
    public TPMOutputStruct transmit( TPMInputStruct input ) throws TPMException;
    
    /**
     * This is the low-level interface to the TPM.
     * Typically, this would involve a call to the TDDL-level
     * device driver.
     * 
     * @param inputBytes 
     * @return
     * @throws TPMIOException
     */
    public byte[] transmitBytes( byte[] inputBytes ) throws TPMIOException;
    
    /**
     * Cleanup the TPM driver.  This should be called at the very end of the program.
     * 
     * @return
     */
    public void cleanup();
    
}
