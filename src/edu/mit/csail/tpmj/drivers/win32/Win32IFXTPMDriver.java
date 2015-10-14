/*
 * Copyright (c) 2006-2007, Massachusetts Institute of Technology (MIT)
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
package edu.mit.csail.tpmj.drivers.win32;

import edu.mit.csail.tpmj.drivers.TDDLConsts;
import edu.mit.csail.tpmj.drivers.TDDLException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.TPMErrorReturnCodeException;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMIOStruct;
import edu.mit.csail.tpmj.structs.TPM_STRUCT_VER;
import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class Win32IFXTPMDriver extends Win32TDDLDriver
{
    
    public static native int _TDDL_Open();
    public static native int _TDDL_Close();
    public static native int _TDDL_TransmitData( byte[] input, byte[] output );

    static
    {
        System.loadLibrary( "IFXTPMJNIProxy" );
    }

    @Override
    public synchronized int TDDL_Close() throws TDDLException
    {
        int ret = _TDDL_Close();

        if ( ret != TDDLConsts.TDDL_SUCCESS )
        {
            throw new TDDLException( "Error on TDDL_Close()", ret );
        }
        // Debug.println( "TDDL_Close() called successfully.");
        return ret;
    }
    @Override
    public synchronized int TDDL_Open() throws TDDLException
    {
        int ret = _TDDL_Open();
        if ( ret != TDDLConsts.TDDL_SUCCESS )
        {
            throw new TDDLException( "Error on TDDL_Open()", ret );
        }
        // Debug.println( "TDDL_Open() called successfully.");
        return ret;
        
    }
    @Override
    public synchronized byte[] TDDL_TransmitData( byte[] input ) throws TDDLException
    {
      byte[] output = new byte[TPMConsts.TPM_MAX_BUFF_SIZE];
      int ret = _TDDL_TransmitData( input, output );
      if ( ret != TDDLConsts.TDDL_SUCCESS )
      {
          throw new TDDLException( "Error on TDDL_TransmitData()", ret );
      }
      else
      {
          // NOTE: This is redundant with BasicTPMDriver.truncateArrayToParamSize(),
          // but keeping this here so it can give a TDDLException
          
          int size = ByteArrayUtil.readInt32BE( output, TPMIOStruct.PARAMSIZE_OFFSET );
          if ( (size < 6) || (size > TPMConsts.TPM_MAX_BUFF_SIZE) )
          {
              throw new TDDLException( "Error: TDDL did not return error, output array has wrong size", ret );
          }
          else
          {
              if ( size >= output.length )
              {
                  // just output directly
                  return output;
              }
              else
              {
                  // Logical size of structure is less 
                  // than allocated space.  (This should be the common case.)
                  // Return a new truncated array.
                  
                  byte[] outArr = new byte[size];
                  System.arraycopy( output, 0, outArr, 0, size );
                  return outArr;
              }
          }
      }
    }

//    public ByteArrayTPMOutputStruct transmit( TPMInputStruct input )
//        throws TPMException
//    {
//        byte[] inputBytes = input.toBytes();
//        byte[] outputBytes = new byte[TPMConsts.TPM_MAX_BUFF_SIZE];
//        int ret = _TDDL_TransmitData( inputBytes, outputBytes );
//        ByteArrayTPMOutputStruct output = new ByteArrayTPMOutputStruct(
//            outputBytes );
//        if ( ret != 0 )
//        {
//            throw new TPMErrorReturnCodeException( input, output,
//                "Native call to TPM driver returned error code: " + ret );
//        }
//        else
//        {
//            return output;
//        }
//    }





}
