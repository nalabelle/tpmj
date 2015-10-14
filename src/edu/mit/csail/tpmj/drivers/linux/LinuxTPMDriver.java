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
package edu.mit.csail.tpmj.drivers.linux;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMIOException;
import edu.mit.csail.tpmj.drivers.BasicTPMDriver;
import edu.mit.csail.tpmj.structs.*;

import java.io.*;

public class LinuxTPMDriver extends BasicTPMDriver
{
    public static final String DEFAULT_DEVICE_PATH = "/dev/tpm0";

    private String devicePath;
    public LinuxTPMDriver()
    {
        this( DEFAULT_DEVICE_PATH );
    }
    
    public LinuxTPMDriver( String devicePath )
    {
        this.devicePath = devicePath;
    }

    /**
     * This is the low-level interface to the TPM.
     * Typically, this would involve a call to the TDDL-level
     * device driver.
     * 
     * @param inputBytes 
     * @return
     * @throws TPMIOException
     */
    public byte[] transmitBytes( byte[] inputBytes ) throws TPMIOException
    {
        byte[] outputBuf = new byte[TPMConsts.TPM_MAX_BUFF_SIZE];
        
        RandomAccessFile raf = null;
        FileOutputStream os = null;
        FileInputStream is = null;

        try
        {
            // NOTE: we need to take the following steps below to 
            // make the os and is point to the same open file descriptor.
            // If we not, then we get problems.
            // Specifically, if we open os and is at the same time,
            // is refuses to open because the "device is busy".
            // If we open is after we close os, we are not able to read
            // any data out.
            // If we try to open is from the getFD of os, it says it's
            // a bad file descriptor (probably because it was only opened for writing).
            raf = new RandomAccessFile( this.devicePath, "rw" );
            FileDescriptor fd = raf.getFD();
            os = new FileOutputStream( fd );
            is = new FileInputStream( fd );

            // write
            os.write( inputBytes );
            os.flush();

            //          Note: the following does NOT work.
            //          Apparently, if you call read but do not read everything available,
            //          then the data disappears, and is not available the next time I call read.            
            //          TODO: Try using a BufferedInputStream to see if it makes a difference.
            //                      
            //            // read
            //            this.readAll( is, outputBuf, 0, 6 );
            //            // TODO: check that tag is correct, if not, throw exception
            //            
            //            int paramSize = ByteArrayUtil.readInt32( outputBuf, 
            //                                            TPMOutputStruct.PARAMSIZE_OFFSET );
            //            if ( paramSize > outputBuf.length )
            //            {
            //                // there must be a problem 
            //                // TODO: Throw a more specific type of exception?
            //                throw new TPMIOException( "TPM returned paramSize too big.");
            //            }
            //            this.readAll( is, outputBuf, 6, paramSize - 6 );

            // Just do what tpm-3.0.3 does, which is attempt to read
            // the whole TPM_MAX_BUFF_SIZE (i.e., 4096)
            this.readAll( is, outputBuf, 0, outputBuf.length );

            is.close();
            is = null;
            os.close();
            os = null;
            raf.close();
            raf = null;
        }
        catch ( Exception e )
        {
            throw new TPMIOException( inputBytes, e );
        }
        finally
        {
            if ( is != null )
            {
                try
                {
                    is.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
            if ( os != null )
            {
                try
                {
                    os.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
            if ( raf != null )
            {
                try
                {
                    raf.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        
        outputBuf = BasicTPMDriver.truncateArrayToParamSize( outputBuf );
        
        return outputBuf;
    }

    /**
     * Reads and ignores all currently available characters on
     * the TPM device. 
     * <p>
     * NOTE: Currently, this is unused and seems unnecessary.
     */
    public void flushTPMOutput()
    {
        try
        {
            FileOutputStream os = new FileOutputStream( this.devicePath );
            FileInputStream is = new FileInputStream( this.devicePath );
            while ( is.available() > 0 )
            {
                is.read();
            }
            is.close();
            os.close();
        }
        catch ( IOException e )
        {
            // We shouldn't get an IOException here unless the file closed or something.
            e.printStackTrace();
        }
    }

    private void readAll( InputStream is, byte[] buf, int off, int len )
        throws IOException
    {
        // Note: it doesn't seem to help if you retry anyway.
        this.readAll( is, buf, off, len, 0, 0 );
    }

    private void readAll( InputStream is, byte[] buf, int off, int len,
        int retries, int delay ) throws IOException
    {
        // TODO: Add Debugging mechanism
        // System.out.println( "readAll: buf.length=" + buf.length + ", off=" + off + ", len=" + len );

        int totalBytesRead = 0;
        int curOff = off;
        int bytesLeft = len;

        do
        {
            int bytesToRead = Math.min( bytesLeft, buf.length - curOff );

            // System.out.println( "Calling read: buf.length=" + buf.length + ", curOff=" + curOff + ", bytestoRead=" + bytesToRead );

            int bytesRead = is.read( buf, curOff, bytesToRead );
            if ( bytesRead > 0 )
            {
                curOff += bytesRead;
                totalBytesRead += bytesRead;
                bytesLeft -= bytesRead;
            }
            else
            {
                // Some kind of error or the device is not ready.
                // System.out.println( "TPM did not give response.  Waiting " + delay + " ms." );
                try
                {
                    Thread.sleep( delay );
                }
                catch ( InterruptedException e )
                {
                    // do nothing
                }
                retries--;
            }
        }
        while ( (bytesLeft > 0) && (curOff < buf.length) && (retries >= 0) );
    }

    public void cleanup()
    {
        // Do nothing ...
        
    }
    
    
}
