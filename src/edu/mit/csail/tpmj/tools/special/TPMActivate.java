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
package edu.mit.csail.tpmj.tools.special;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

/**
 * This tool activates the TPM, with physical presence.
 */
public class TPMActivate
{

    public static void main( String[] args )
    {

        try
        {
            String[] noArgs = {};
            System.out.println( "Running TPMPhysicalEnable ..." );
            TPMPhysicalEnable.main( noArgs );

            // NOTE: Need to init the driver afterwards because TPMPhysicalEnable.main calls cleanup
            
            TPMUtilityFuncs.initTPMDriver();

            Stopwatch sw = new Stopwatch();
            sw.start();
            System.out.println( "calling TPM_PhysicalSetDeactivated(false) ... " );
            TPMAdminFuncs.physicalActivate();
            sw.stop();
            System.out.println( "Done in " + sw.getTime() + " ms." );
            System.out.println( "Your TPM is now activated." );

            System.out.println( "You must REBOOT your machine for settings to take effect." );
            
            TPMUtilityFuncs.cleanupTPMDriver();
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
    }

}
