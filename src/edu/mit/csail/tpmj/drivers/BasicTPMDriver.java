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

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMErrorReturnCodeException;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.TPMIOException;
import edu.mit.csail.tpmj.TPMNullOutputException;
import edu.mit.csail.tpmj.commands.TPM_GetCapability;
import edu.mit.csail.tpmj.commands.TPM_GetCapabilityOutput;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMIOStruct;
import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.structs.TPM_CAP_VERSION_INFO;
import edu.mit.csail.tpmj.structs.TPM_STRUCT_VER;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;

public abstract class BasicTPMDriver implements TPMDriver
{
    protected int numRetries = 1;
    protected int retryDelay = 2000;

    // cached values
    private int tpmManufacturer = 0;
    private TPM_STRUCT_VER tpmVersion = null;

    static
    {
        // TODO: Unset Debug for BasicTPMDriver.  This is set on by default to show the TPM version upon init
        // Debug.setThisClassDebugOn( true );
    }

    /**
     * Default implementation includes resetting
     * and regetting of info such as manufacturer and version.
     */
    public synchronized void init()
    {
        Debug.println( "Initializing TPMDriver ", this.getClass() );
        // force regetting of info from TPM
        this.tpmManufacturer = 0;
        this.tpmVersion = null;
        // get info from TPM
        int manuf = this.getTPMManufacturer();
        Debug.println( "Manufacturer ID: 0x" + Integer.toHexString( manuf ) );
        TPM_STRUCT_VER ver = this.getTPMVersion();
        Debug.println( "TPM Version: " + ver );
    }

    /**
     * Returns the TPM Manufacturer, or zero, if there was an error.
     * This is initially called in init().
     * Default implementation checks if cached value is non-zero
     * and uses it, otherwise, it creates 
     * and executes a new TPM_GetCapability command.
     */
    public synchronized int getTPMManufacturer()
    {
        if ( this.tpmManufacturer != 0 )
        {
            return this.tpmManufacturer;
        }

        try
        {
            // NOTE: This is the same as TPMGetCapabilityFuncs.getManufacturer()
            // but is inlined here so there are no dependencies
            byte[] subCap = ByteArrayUtil.toBytesInt32BE( TPMConsts.TPM_CAP_PROP_MANUFACTURER );
            TPM_GetCapability cmd = new TPM_GetCapability(
                TPMConsts.TPM_CAP_PROPERTY, subCap );
            // Note: this can throw a TPMException, 
            // in which case, output is never returned
            TPM_GetCapabilityOutput output = cmd.execute( this );
            byte[] resp = output.getResp();
            this.tpmManufacturer = ByteArrayUtil.readInt32BE( resp, 0 );
            return this.tpmManufacturer;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            // FIXME: create a constant for error
            this.tpmManufacturer = 0;
            return 0;
        }
    }

    /**
     * Returns the version of the TPM.  
     * (First tries the version 1.2 way of getting
     * the version, and then tries the 1.1 way.)
     */
    public synchronized TPM_STRUCT_VER getTPMVersion()
    {
        if ( this.tpmVersion != null )
        {
            return this.tpmVersion;
        }

        TPM_STRUCT_VER ret = null;
        try
        {
            ret = this.getVersion12Style().getVersion();
        }
        catch ( Exception e )
        {
            try
            {
                ret = this.getVersion11Style();
            }
            catch ( TPMException e11 )
            {
                return null;
            }
        }
        this.tpmVersion = ret;
        return this.tpmVersion;
    }

    public synchronized boolean isTPM11()
    {
        TPM_STRUCT_VER version = this.getTPMVersion();
        return (version.getMajor() == 1) && (version.getMinor() == 1);
    }

    public synchronized boolean isTPM12()
    {
        TPM_STRUCT_VER version = this.getTPMVersion();
        return (version.getMajor() == 1) && (version.getMinor() == 2);
    }

    private synchronized TPM_STRUCT_VER getVersion11Style() throws TPMException
    {
        // HACK: This is the same as in TPMGetCapabilityFuncs but repeated here to avoid dependencies

        TPM_GetCapability cmd = new TPM_GetCapability(
            TPMConsts.TCPA_CAP_VERSION );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( this );

        // if we reach here, then output should not be null
        // and there should be no errors.
        byte[] verBytes = output.getResp();
        TPM_STRUCT_VER structVer = new TPM_STRUCT_VER( verBytes );
        return structVer;
    }

    private synchronized TPM_CAP_VERSION_INFO getVersion12Style()
        throws TPMException
    {
        // HACK: This is the same as in TPMGetCapabilityFuncs but repeated here to avoid dependencies

        TPM_GetCapability cmd = new TPM_GetCapability(
            TPMConsts.TPM_CAP_VERSION_VAL );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( this );

        // if we reach here, then output should not be null
        // and there should be no errors.
        byte[] verBytes = output.getResp();
        TPM_CAP_VERSION_INFO ret = new TPM_CAP_VERSION_INFO( verBytes );
        return ret;
    }

    public synchronized int getNumRetries()
    {
        return numRetries;
    }

    public synchronized void setNumRetries( int numRetries )
    {
        this.numRetries = numRetries;
    }

    public synchronized int getRetryDelay()
    {
        return retryDelay;
    }

    public synchronized void setRetryDelay( int retryDelay )
    {
        this.retryDelay = retryDelay;
    }

    /**
     * This is a convenience method that 
     * takes in a raw output buffer (typically a 4096-byte array),
     * and returns a truncated array according to the size specified at
     * the paramSize offset.  This should be used in transmitBytes
     * to ensure that transmitBytes returns the right-sized array.
     * Note: if the size at paramSize offset is wrong (less than 6 bytes,
     * or greater than the size of the input buffer, then it just returns
     * the input buffer itself).
     * 
     * @param rawArray
     * @return
     */
    public static byte[] truncateArrayToParamSize( byte[] rawBuffer )
    {
        if ( ( rawBuffer == null ) || ( rawBuffer.length < 6 ) ) 
        {
            // In case of error, or rawBuffer too short to read paramSize, return as-is.
            
            return rawBuffer;
        }
        int size = ByteArrayUtil.readInt32BE( rawBuffer,
            TPMIOStruct.PARAMSIZE_OFFSET );
        if ( (size < 6) || (size >= rawBuffer.length) )
        {
            // In case of error, or same length, just return as-is
            return rawBuffer;
        }
        else
        {
            // Logical size of structure is less 
            // than allocated space.  (This should be the common case.)
            // Return a new truncated array.

            byte[] outArr = new byte[size];
            System.arraycopy( rawBuffer, 0, outArr, 0, size );
            return outArr;
        }
    }

    /**
     * This method calls this.transmitBytes, and retries if there is an error. 
     */
    public synchronized ByteArrayTPMOutputStruct transmit( TPMInputStruct input )
        throws TPMException
    {
        //        Debug.println( "BasicTPMDriver.transmit: input= " + input );

        int retries = 0;
        boolean ioOk = false;
        ByteArrayTPMOutputStruct output = null;

        // Serialize input
        byte[] inputBytes = input.toBytes();

        do
        {
            try
            {
                byte[] outputBuf = this.transmitBytes( inputBytes );
                output = new ByteArrayTPMOutputStruct( outputBuf );
                // if we get here, there was no io error.
                ioOk = true;
            }
            catch ( TPMIOException ioe )
            {
                if ( retries < this.numRetries )
                {
                    retries++;
                    System.err.println( "BasicTPMDriver: TPMIOException encountered. (" + ioe + ")\n"
                        + "Waiting "
                        + (this.retryDelay / 1000.0) + " s, then retrying ... " );
                    try
                    {
                        Thread.sleep( this.retryDelay );
                    }
                    catch ( InterruptedException e )
                    {
                        e.printStackTrace();
                    }
                    // continue and try again ...
                }
                else
                {
                    throw ioe;
                }
            }
        }
        while ( !ioOk );

        //        Debug.println( "BasicTPMDriver.transmit: output= " + output );
        if ( output != null )
        {
            //            Debug.println( "Return code: " + output.getReturnCode() );
        }

        if ( output == null )
        {
            // NOTE: I think this should rarely or never happen.
            throw new TPMNullOutputException( input );
        }
        else if ( output.isError() )
        {
            throw new TPMErrorReturnCodeException( input, output );
        }
        else
        {
            return output;
        }
    }

}
