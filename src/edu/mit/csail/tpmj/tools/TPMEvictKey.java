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
package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

/**
 * This tool can be used to evict all key handles from the TPM. 
 */
public class TPMEvictKey
{
    public static void usage()
    {
        System.out.println( "Usage: TPMEvictKey <keyHandle | \"all\"> " );
    }
    
    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
            System.exit( -1 );
        }
        
        SwitchParams params = new SwitchParams( args, "keyHandle" );
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );
        
        try
        {
            String keyHandleString = params.getString( "keyHandle" );
            if ( "all".equalsIgnoreCase( keyHandleString ) )
            {
                System.out.println( "Reading Key handles TPM 1.1 style" );
                TPM_KEY_HANDLE_LIST keyHandles = TPMGetCapabilityFuncs.getKeyHandles();
                System.out.println( keyHandles.toString() );

                int[] keyHandleInts = keyHandles.getHandles();
                for ( int keyHandle : keyHandleInts )
                {
                    evictKey( keyHandle );
                }
            }
            else
            {
                int keyHandle = params.getInt( "keyHandle" );
                evictKey( keyHandle );
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }

    /**
     * @param keyHandle
     */
    public static void evictKey( int keyHandle )
    {
        try
        {
            System.out.print( "Evicting keyHandle: 0x"
                + Integer.toHexString( keyHandle ) + " ... " );
            TPMStorageFuncs.TPM_EvictKey( keyHandle );
            System.out.println( "OK" );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

}
