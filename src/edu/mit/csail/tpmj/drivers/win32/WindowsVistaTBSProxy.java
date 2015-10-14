/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
 * Parts, copyright (c) 2007, Thomas Müller, xnos Internet Services (xnos.org)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of MIT nor xnos nor the names of its contributors may be used 
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
 * Original author:  Thomas Müller, xnos Internet Services (xnos.org), 2007
 * Modified to extend Win32TDDLDriver by: Luis F. G. Sarmenta, Massachusetts Institute of Technology (MIT)
 */ 
package edu.mit.csail.tpmj.drivers.win32;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMErrorReturnCodeException;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.TPMNullOutputException;
import edu.mit.csail.tpmj.drivers.BasicTPMDriver;
import edu.mit.csail.tpmj.drivers.TDDLConsts;
import edu.mit.csail.tpmj.drivers.TDDLException;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.util.Debug;

public class WindowsVistaTBSProxy extends Win32TDDLDriver
{
    static
    {
        // JNI Proxy dll
        System.loadLibrary( "TBSProxy" );
        
        // TBS Return Codes
    }
    
    //native methods
    private native long Tbs_getReturnCode();
    private native void Tbsip_Context_Create();
    private native void Tbsip_Context_Close();
    private native void Tbsip_Cancel_Commands();
    private native byte[] Tbsip_Physical_Presence_Command(byte[] input, int input_length);
    private native byte[] Tbsip_Submit_Command(byte[] input, int input_length);
    
    public static final int BUFFER_SIZE = TPMConsts.TPM_MAX_BUFF_SIZE;

    public WindowsVistaTBSProxy()
    {
        //this( DEFAULT_DEVICE_PATH );
    }
    
    @Override
    public synchronized int TDDL_Close() throws TDDLException
    {
        // Close Connection
        Debug.println( "Close TBS Context" );
        Tbsip_Context_Close();
        int retCode = (int) Tbs_getReturnCode();
        Debug.println( "Context_Close_Return_Code: " + retCode );
        
        if ( retCode != TDDLConsts.TDDL_SUCCESS )
        {
            throw new TDDLException( "Error on Tbsip_Context_Create()", retCode );
        }
        
        return retCode;
    }
    
    @Override
    public synchronized int TDDL_Open() throws TDDLException
    {
        // Connect to TBS 
        Debug.println( "Create TBS Context" );        
        Tbsip_Context_Create();
        int retCode = (int) Tbs_getReturnCode();
        Debug.println( "Context_Create_Return_Code: " + retCode );
        
        if ( retCode != TDDLConsts.TDDL_SUCCESS )
        {
            throw new TDDLException( "Error on Tbsip_Context_Create()", retCode );
        }
        
        return retCode;
    }
    
    @Override
    public synchronized byte[] TDDL_TransmitData( byte[] input ) throws TDDLException
    {
        // Submit Command 
        Debug.println( "Submit Command" );
        byte[] outputBytes = Tbsip_Submit_Command(input, input.length);
        int retCode = (int) Tbs_getReturnCode();
        Debug.println( "Submit_Command_Return_Code: " + retCode );

        if ( retCode != TDDLConsts.TDDL_SUCCESS )
        {
            throw new TDDLException( "Error on Tbsip_Context_Create()", retCode );
        }
        
        return outputBytes;
    }

    
//  OLD Code removed by lfgs, now done Win32TDDLDriver.transmitBytes, using TDDL_* defined here
//    
//    public byte[] transmitBytes( byte[] inputBytes )
//    {
//        // FIXME:  This should throw TPMIOException in case of error return codes.
//        // FIXME:  Better yet, just use transmitBytes in Win32TDDLDriver, and just define TDDL_* methods
//        
//        //Debug.setDebugOn( this.getClass(), true );
//        
//        byte[] outputBytes = null;
//        long retCode = 0;
//        
//        // Connect to TBS 
//        Debug.println( "Create TBS Context" );        
//        Tbsip_Context_Create();
//        retCode = Tbs_getReturnCode();
//        Debug.println( "Context_Create_Return_Code: " + retCode );
//        
//        
//        
//        // Submit Command 
//        Debug.println( "Submit Command" );
//        outputBytes = Tbsip_Submit_Command(inputBytes, inputBytes.length);
//        Debug.println( "Submit_Command_Return_Code: " + Tbs_getReturnCode() );
//        
//        // Close Connection
//        Debug.println( "Close TBS Context" );
//        Tbsip_Context_Close();
//        Debug.println( "Context_Close_Return_Code: " + Tbs_getReturnCode() );
//        
//        return outputBytes;
//    }

//    OLD Code removed by lfgs, now done in BasicTPMDriver
//    
//    public synchronized ByteArrayTPMOutputStruct transmit( TPMInputStruct input )
//        throws TPMException
//    {
//        //Debug.setDebugOn( this.getClass(), true );
//        Debug.println( "WindowsVistaTBSProxy.transmit: input= " + input );
//
//        ByteArrayTPMOutputStruct output = null;
//
//        output = _transmit( input );
//
//        Debug.println( "WindowsVistaTBSProxy.transmit: output= " + output );
//        if ( output != null )
//        {
//            Debug.println( "Return code: " + output.getReturnCode() );
//        }
//
//        if ( output == null )
//        {
//            // NOTE: I think this should rarely or never happen.
//            throw new TPMNullOutputException( input );
//        }
//        else if ( output.isError() )
//        {
//            throw new TPMErrorReturnCodeException( input, output );
//        }
//        else
//        {
//            return output;
//        }
//    }
}
