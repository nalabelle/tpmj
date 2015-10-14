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
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;

public class DebuggingTPMDriverWrapper implements TPMDriver
{
    public TPMDriver tpmDriver;

    static
    {
        Debug.setThisClassDebugOn( true );
    }

    public DebuggingTPMDriverWrapper( TPMDriver tpmDriver )
    {
        super();
        this.tpmDriver = tpmDriver;
    }

    public byte[] transmitBytes( byte[] inputBytes ) throws TPMIOException
    {
        Debug.println( "TPMDriver.transmit: input= ", inputBytes );

        byte[] outputBytes = this.tpmDriver.transmitBytes( inputBytes );

        Debug.println( "TPMDriver.transmit: output= ", outputBytes );

        return outputBytes;
    }

    public TPMOutputStruct transmit( TPMInputStruct input ) throws TPMException
    {
        Debug.println( "TPMDriver.transmit: input= ", input );

        byte[] inputBytes = input.toBytes();
        int paramSize = ByteArrayUtil.readInt32BE( inputBytes,
            TPMInputStruct.PARAMSIZE_OFFSET );
        if ( paramSize != inputBytes.length )
        {
            throw new TPMException( "ParamSize " + paramSize
                + " doesn't match actual byte array length "
                + inputBytes.length );
        }

        //Debug.println( "paramSize: " + paramSize  );
        TPMOutputStruct output = this.tpmDriver.transmit( input );

        Debug.println( "TPMDriver.transmit: output= ", output );

        return output;
    }

    public void init()
    {
        this.tpmDriver.init();
    }

    public void cleanup()
    {
        this.tpmDriver.cleanup();
    }
    
    
    public int getTPMManufacturer()
    {
        return this.tpmDriver.getTPMManufacturer();
    }

    public TPM_STRUCT_VER getTPMVersion()
    {
        return this.tpmDriver.getTPMVersion();
    }

    public boolean isTPM11()
    {
        return tpmDriver.isTPM11();
    }

    public boolean isTPM12()
    {
        return tpmDriver.isTPM12();
    }

}
