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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006
 */
package edu.mit.csail.tpmj.drivers.win32;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.BasicTPMDriver;
import edu.mit.csail.tpmj.drivers.TDDLConsts;
import edu.mit.csail.tpmj.drivers.TDDLException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.stats.Stopwatch;


public abstract class Win32TDDLDriver extends BasicTPMDriver
{
    /**
     * Defaults to IFXTPM (Infineon's IFXTPM.DLL)
     */
    public static final String DEFAULT_DLL = "IFXTPM";
    protected String dllName;

    public Win32TDDLDriver()
    {
        this( DEFAULT_DLL );
    }

    public Win32TDDLDriver( String dllName )
    {
        this.dllName = dllName;
    }

    @Override
    public synchronized void init()
    {
//        System.out.println( "Testing Windows TDDL Driver ... " );

        try
        {
            // NOTE: Tested this in Vista, and apparently,
            // the right thing to do is to open it once and
            // don't close it until the end.  Otherwise,
            // authorization sessions don't work.
            
            // This seems to be OK with IFXTPM in XP, so keeping it this way.
            this.TDDL_Open();
            
            // FIXME: Right now, there is nothing that closes
            
            // this.TDDL_Close();
//            System.out.println( "Windows TDDL Driver successfully tested." );
        }
        catch ( TDDLException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.init();
    }

    public synchronized void cleanup()
    {

        try
        {
            this.TDDL_Close();
        }
        catch ( TDDLException e )
        {
            e.printStackTrace();
        }
    }
    
    
    public abstract int TDDL_Open() throws TDDLException;

    public abstract int TDDL_Close() throws TDDLException;

    /**
     * Transmits data via the native TDDL, and returns a new array containing the output.
     * Any errors 
     * 
     * @param input
     * @param output
     * @return
     * @throws TPMIOException
     */
    public abstract byte[] TDDL_TransmitData( byte[] input )
        throws TDDLException;
    
    

    public synchronized byte[] transmitBytes( byte[] inputBytes )
        throws TPMIOException
    {
        byte[] outputBytes;

//        Stopwatch sw = new Stopwatch();
//        System.out.println( "Calling TDDL_TransmitData ... " );
//        sw.start();
//
//        Debug.println( " Calling TDDL_Open() ... " );

        // NOTE: Tested this in Vista, and apparently, opening and closing
        // for each Submit causes an error for commands that require
        // authorization.  (Authorization sessions probably need to be
        // done in the same context.)
        // This seems to be OK with IFXTPM in XP, so keeping it this way.
        
//        this.TDDL_Open();

//        Debug.println( "TDDL_Open() done ... " + sw.getTime() );

        //                Debug.println( " Calling TDDL_TransmitData() ... " );
        outputBytes = this.TDDL_TransmitData( inputBytes );
        outputBytes = BasicTPMDriver.truncateArrayToParamSize( outputBytes );

//        Debug.println( "TDDL_TransmitData() done ... " + sw.getTime() )

//        Debug.println( " Calling TDDL_Close() ... " );
        
        // NOTE: Tested this in Vista, and apparently, opening and closing
        // for each Submit causes an error for commands that require
        // authorization.
//        this.TDDL_Close();

//        Debug.println( "TDDL_Close() done ... " + sw.getTime() );
//
//        sw.stop();
//        Debug.println( "Done. " + sw.getTime() + " ms" );

        return outputBytes;
    }
}
