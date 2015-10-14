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
package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;
import edu.mit.csail.tpmj.util.stats.Stopwatch;

public class TPMInfo
{
    public static void main( String[] args )
    {
        SwitchParams params = new SwitchParams( args, "ownerPwd" );
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "ownerPwd" );

        getManufacturer();
        System.out.println( "----\n" );

        getVersion11Style();
        System.out.println( "----\n" );

        getVersion12Style();
        System.out.println( "----\n" );

        TPM_PERMANENT_FLAGS permFlags = getTPMFlags( tpmDriver );
        System.out.println( "----\n" );

        if ( tpmDriver.isTPM11() )
        {
            readPubEK( tpmDriver );
        }
        else
        {
            readPubEK12Style( tpmDriver, ownerAuth );
        }
        System.out.println( "----\n" );

        readPCRs();
        System.out.println( "----\n" );

        readKeyHandles11Style();
        System.out.println( "----\n" );

        readHandles( "KEY", TPMConsts.TPM_RT_KEY );
        System.out.println( "----\n" );

        readHandles( "CONTEXT", TPMConsts.TPM_RT_CONTEXT );
        System.out.println( "----\n" );

        readHandles( "AUTH SESSION", TPMConsts.TPM_RT_AUTH );
        System.out.println( "----\n" );

        readHandles( "TRANSPORT SESSION", TPMConsts.TPM_RT_TRANS );
        System.out.println( "----\n" );

        readCounters();
        System.out.println( "----\n" );

        TPMToolsUtil.cleanupTPMDriver();
    }

    public static void readHandles( String type, int typeConst )
    {
        System.out.println( "Reading " + type + " handles (TPM 1.2 style) ..." );
        try
        {
            TPM_KEY_HANDLE_LIST handlesList;
            handlesList = TPMGetCapabilityFuncs.getHandles( typeConst );
            int[] handles = handlesList.getHandles();
            String ret = "(" + handles.length + " handles):\n";
            for ( int i = 0; i < handles.length; i++ )
            {
                ret = ret + "0x" + Integer.toHexString( handles[i] ) + "\n";
            }
            System.out.println( ret );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

    }

    public static void readCounters()
    {
        System.out.println( "Reading monotonic counters (TPM 1.2 only) ..." );
        try
        {
            TPM_KEY_HANDLE_LIST handlesList;
            handlesList = TPMGetCapabilityFuncs.getHandles( TPMConsts.TPM_RT_COUNTER );
            int[] handles = handlesList.getHandles();
            System.out.println( "(" + handles.length + " counters):\n" );
            for ( int i = 0; i < handles.length; i++ )
            {
                TPM_COUNTER_VALUE counterValue = TPMCounterFuncs.TPM_ReadCounter( handles[i] );
                int countVal = counterValue.getCounter();
                System.out.println( "0x" + Integer.toHexString( handles[i] )
                    + ": " + counterValue );
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

    }

    /**
     * 
     */
    public static void readKeyHandles11Style()
    {
        try
        {
            System.out.println( "Reading Key handles TPM 1.1 style" );
            TPM_KEY_HANDLE_LIST keyHandles = TPMGetCapabilityFuncs.getKeyHandles();
            System.out.println( keyHandles.toString() );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

    /**
     * 
     */
    public static void readPCRs()
    {
        try
        {
            // Get number of PCRs.
            System.out.println( "\nGetting number of PCRS: " );
            int numPcrs = TPMGetCapabilityFuncs.getNumPcrs();
            System.out.println( "numPcrs = " + numPcrs );

            // Read PCRS
            System.out.println( "Reading PCRs" );
            for ( int i = 0; i < numPcrs; i++ )
            {
                TPM_PCRVALUE pcrValue = TPMPcrFuncs.TPM_PCRRead( i );
                System.out.println( "PCR " + i + ": " + pcrValue );
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

    /**
     * @param tpmDriver
     */
    public static TPM_PERMANENT_FLAGS getTPMFlags( TPMDriver tpmDriver )
    {
        TPM_PERMANENT_FLAGS permFlags = null;
        System.out.println( "Getting TPM Flags (TPM 1.2 only) ... " );
        if ( !tpmDriver.isTPM11() )
        {
            System.out.println( "Getting TPM Permanent Flags ... " );
            try
            {
                permFlags = TPMGetCapabilityFuncs.getPermanentFlags();
            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
            }
            System.out.println( "Returned: " + permFlags );

            System.out.println( "Getting TPM Volatile Flags ... " );
            TPM_STCLEAR_FLAGS volFlags = null;
            try
            {
                volFlags = TPMGetCapabilityFuncs.getVolatileFlags();
            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
            }
            System.out.println( "Returned: " + volFlags );
        }
        return permFlags;
    }

    /**
     * @param tpmDriver
     */
    public static void readPubEK( TPMDriver tpmDriver )
    {
        System.out.println( "Reading Public Endorsement Key using TPM_ReadPubek ..." );
        System.out.println( "(using all-zeros as nonce)" );
        try
        {
            TPM_PUBKEY pubKey = TPMAdminFuncs.TPM_ReadPubek( TPM_NONCE.NULL );
            System.out.println( "Public Endorsement Key:\n" + pubKey );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

    /**
     * @param tpmDriver
     * @param ownerAuth 
     */
    public static void readPubEK12Style( TPMDriver tpmDriver,
        TPM_SECRET ownerAuth )
    {
        TPM_PUBKEY pubKey = null;
        System.out.println( "Reading Public Endorsement Key using TPM_OwnerReadInternalPub (TPM 1.2 only) ..." );
        try
        {
            pubKey = TPMAdminFuncs.TPM_OwnerReadInternalPub(
                TPMConsts.TPM_KH_EK, ownerAuth );
            System.out.println( "Public Endorsement Key:\n" + pubKey );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.out.println( "Error: " + e );
        }

        if ( pubKey == null )
        {
            System.out.println();
            readPubEK( tpmDriver );
        }
    }

    /**
     * 
     */
    public static void getVersion12Style()
    {
        System.out.println( "Getting version via TPM 1.2 way ... " );
        TPM_CAP_VERSION_INFO versionInfo12 = null;
        try
        {
            versionInfo12 = TPMGetCapabilityFuncs.getVersion12Style();
            System.out.println( "Returned: " + versionInfo12 );
            if ( versionInfo12 != null )
            {
                System.out.println( "tag: 0x"
                    + Integer.toHexString( versionInfo12.getTag() ) );
                System.out.println( "version: " + versionInfo12.getVersion() );
                System.out.println( "specLevel: 0x"
                    + Integer.toHexString( versionInfo12.getSpecLevel() ) );
                System.out.println( "errataRev: 0x"
                    + Integer.toHexString( versionInfo12.getErrataRev() ) );
                System.out.println( "tpmVendorID: 0x"
                    + Integer.toHexString( versionInfo12.getTpmVendorID() ) );
                System.out.println( "vendorSpecificSize: 0x"
                    + Integer.toHexString( versionInfo12.getVendorSpecificSize() ) );
                System.out.println( "vendorSpecific: "
                    + ByteArrayUtil.toPrintableHexString( versionInfo12.getVendorSpecific() ) );
                System.out.println();
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

    /**
     * 
     */
    public static void getVersion11Style()
    {
        System.out.println( "Getting version via TPM 1.1 way ... " );
        TPM_STRUCT_VER structVer = null;
        try
        {
            structVer = TPMGetCapabilityFuncs.getVersion11Style();
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        System.out.println( "Returned: " + structVer );
        System.out.println();
    }

    /**
     * 
     */
    public static void getManufacturer()
    {
        System.out.println( "\n*****" );
        System.out.println( "Getting manufacturer ID ... " );
        try
        {
            int manuf = TPMGetCapabilityFuncs.getManufacturer();

            System.out.println( "TPM VENDOR ID = 0x"
                + Integer.toHexString( manuf )
                + " ("
                + ByteArrayUtil.toASCIIString( ByteArrayUtil.toBytesInt32BE( manuf ) )
                + ")" );

        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
    }

}
