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
package edu.mit.csail.tpmj.funcs;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.TPMDriverFactory;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 * This is the superclass for a set of classes
 * that provide static convenience functions
 * for executing TPM commands.
 * <p>
 * The static convenience functions are
 * generally grouped according to their grouping
 * in the TPM commands spec.  In addition to primitive
 * commands provided by the TPM itself, additional
 * functions that use these 
 * commands in specialized ways can be defined here.
 * <p>
 * The purpose of this class is to allow the
 * utility function classes to inherit the static
 * tpmDriver pointer and the execute method.
 * 
 * @author lfgs
 */
public class TPMUtilityFuncs
{
    protected static TPMDriver tpmDriver = null;

    public static TPMDriver initTPMDriver()
    {
        tpmDriver = TPMDriverFactory.getTPMDriver();
        return tpmDriver;
    }

    public static void cleanupTPMDriver()
    {
        if ( tpmDriver != null )
        {
            tpmDriver.cleanup();
            tpmDriver = null;
            TPMDriverFactory.setTPMDriver( null );
        }
    }

    public static TPMDriver getTPMDriver()
    {
        if ( tpmDriver == null )
        {
            initTPMDriver();
        }
        return tpmDriver;
    }

    public static void setTPMDriver( TPMDriver tpmDriver )
    {
        //        TPMDriverFactory.setTPMDriver( tpmDriver );
        TPMUtilityFuncs.tpmDriver = tpmDriver;
    }

    /**
     * Executes the given command using the same TPMdriver used
     * by other TPMUtilityFuncs static functions.
     * Note: use of this command is not recommended
     * because it does not take advantage of the type-safety
     * provided by the covariant return types of specific commands.
     * 
     * @param cmd
     * @return
     * @throws TPMException
     */
    @Deprecated
    public static TPMOutputStruct execute( TPMCommand cmd ) throws TPMException
    {
        return cmd.execute( tpmDriver );
    }

    @Deprecated
    public static void TPM_Reset() throws TPMException
    {
        if ( tpmDriver.isTPM11() )
        {
            TPM_Reset resetCmd = new TPM_Reset();
            resetCmd.execute( tpmDriver );
        }
        else
        {
            TPM_KEY_HANDLE_LIST handlesList = TPMGetCapabilityFuncs.getHandles( TPMConsts.TPM_RT_AUTH );
            int[] handleInts = handlesList.getHandles();
            for ( int handle : handleInts )
            {
                TPMAdminFuncs.TPM_FlushSpecific( handle, TPMConsts.TPM_RT_AUTH );
            }
        }
    }
}
