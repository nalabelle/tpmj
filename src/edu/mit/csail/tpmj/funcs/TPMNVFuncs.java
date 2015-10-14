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

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.drivers.linux.LinuxTPMDriver;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMNVFuncs extends TPMUtilityFuncs
{
    public static final int TPM_NV_INDEX_LOCK = 0xFFFFFFFF;
    public static final int TPM_NV_INDEX0 = 0x00000000;
    public static final int TPM_NV_INDEX_DIR = 0x10000001;
    public static final int TPM_NV_INDEX_EKCert = 0x0000F000; // The Endorsement credential   
    public static final int TPM_NV_INDEX_TPM_CC = 0x0000F001; // The TPM Conformance credential 
    public static final int TPM_NV_INDEX_PlatformCert = 0x0000F002; // The platform  credential   
    public static final int TPM_NV_INDEX_Platform_CC = 0x0000F003; // The Platform  conformance credential 
    public static final int TPM_NV_INDEX_TSS = 0x00011100; // Reserved  for TSS use 
    public static final int TPM_NV_INDEX_PC = 0x00011200; // Reserved  for PC Client use
    public static final int TPM_NV_INDEX_SERVER = 0x00011300; // reserved  for Server use 
    public static final int TPM_NV_INDEX_MOBILE = 0x00011400; // Reserved  for mobile use 
    public static final int TPM_NV_INDEX_PERIPHERAL = 0x00011500; // Reserved  for peripheral use 
    public static final int TPM_NV_INDEX_GPIO_xx = 0x00011600; // Reserved  for GPIO  pins  
    public static final int TPM_NV_INDEX_GROUP_RESV = 0x00010000; // Reserved  for TCG WG’s  

    public static TPM_DIGEST TPM_DirRead( int dirIndex ) throws TPMException
    {
        TPM_DirRead cmd = new TPM_DirRead( dirIndex );
        TPM_DirReadOutput output = cmd.execute( tpmDriver );
        return output.getDirContents();
    }

    public static void TPM_DirWriteAuth( int dirIndex, TPM_DIGEST newContents,
        TPM_SECRET ownerAuth ) throws TPMException
    {
        TPM_DirWriteAuth cmd = new TPM_DirWriteAuth( dirIndex, newContents );
        TPM_DirWriteAuthOutput output = null;
        if ( ownerAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_DirWriteAuthOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, ownerAuth );
        }
    }

    public static int[] getNVList() throws TPMException
    {
        TPM_GetCapability cmd = new TPM_GetCapability(
            TPMConsts.TPM_CAP_NV_LIST, null );

        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_GetCapabilityOutput output = cmd.execute( tpmDriver );

        byte[] resp = output.getResp();
        int[] intArr = new int[resp.length / 4];
        for ( int i = 0; i < resp.length / 4; i++ )
        {
            intArr[i] = ByteArrayUtil.readInt32BE( resp, i * 4 );
        }
        return intArr;
    }

    public static byte[] TPM_NV_ReadValue( int nvIndex, int offset,
        int dataSize, TPM_SECRET ownerAuth ) throws TPMException
    {
        TPM_NV_ReadValue cmd = new TPM_NV_ReadValue( nvIndex, offset, dataSize );
        TPM_NV_ReadValueOutput output = null;
        if ( ownerAuth == null )
        {
            cmd.setNoAuth();
            output = cmd.execute( tpmDriver );
        }
        else
        {
            output = (TPM_NV_ReadValueOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, ownerAuth );
        }
        byte[] data = output.getData();
        return data;
    }

    // Unit test

    private static void dumpNVRAMNoAuth( int index )
    {
        try
        {
            System.out.println( "Reading NVRAM index 0x"
                + Integer.toHexString( index ) + " with no authorization." );
            int i = 0;
            while ( i < 4096 )
            {
                byte[] data = TPM_NV_ReadValue( index, i, 16, null );
                System.out.println( "[" + i + "]:"
                    + ByteArrayUtil.toPrintableHexString( data ) );
                i += 16;
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

    }

    public static void main( String[] args )
    {
        SwitchParams params = new SwitchParams( args, "ownerAuth" );
        
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "ownerAuth" );

            System.out.println( "Trying to read DIR index 0 using TPM_DirRead" );
            TPM_DIGEST dirContents = TPM_DirRead( 0 );
            System.out.println( dirContents.toString() );

            System.out.println( "Incrementing and writing back to DIR index 0 using TPM_DirWriteAuth" );
            byte[] test = dirContents.toBytes();
            int val = ByteArrayUtil.readInt32BE( test, 0 );
            val++;
            ByteArrayUtil.writeInt32BE( test, 0, val );
            TPM_DIGEST newContents = new TPM_DIGEST( test );
            TPM_DirWriteAuth( 0, newContents, ownerAuth );
            System.out.println( "done." );

            System.out.println( "Trying to read DIR index 0 using TPM_DirRead" );
            dirContents = TPM_DirRead( 0 );
            System.out.println( dirContents.toString() );

            //            System.out.println( "Trying to write 0 to DIR index 0 using TPM_DirRead");
            //            newContents = new TPM_DIGEST();
            //            TPM_DirWriteAuth( 0, newContents, ownerAuth );
            //            System.out.println( "done." );
            //
            //            System.out.println( "Trying to read DIR index 0 using TPM_DirRead");
            //            dirContents = TPM_DirRead( 0 );
            //            System.out.println( dirContents.toString() );

            if ( !tpmDriver.isTPM11() )
            {

                try
                {
                    // This doesn't work. It generates a TPM_AUTHFAIL (return code 0x1) error.
                    //            System.out.println( "Using GetCapability to get list of NV ... " );
                    //            int[] nvList = getNVList();
                    //            for ( int i = 0; i < nvList.length; i++ )
                    //            {
                    //                System.out.println( "0x" + Integer.toHexString( nvList[i] ) );
                    //            }

                    
                    System.out.println( "Trying to read TPM_NV_INDEX_DIR with no authorization." );
                    int i = 0;
                    while ( i < 4096 )
                    {
                        byte[] data = TPM_NV_ReadValue( TPM_NV_INDEX_DIR, i,
                            16, null );
                        System.out.println( "[" + i + "]:"
                            + ByteArrayUtil.toPrintableHexString( data ) );
                        i += 16;
                    }
                }
                catch ( TPMException e )
                {
                    TPMToolsUtil.handleTPMException( e );
                }

                //          FIXME: This is causing a NullPointerException somewhere in VerifyAuthOutData (getting evenNonce)
                //          The reason is that for some strange reason, the TPM is returning a message with 0xC4 as the tag,
                //          which means no auth, and my code is not getting the auth data if it sees that tag, so the auth data is null.            
                try
                {
                    System.out.println( "Trying to read TPM_NV_INDEX_DIR with owner authorization." );
                    byte[] data = TPM_NV_ReadValue( TPM_NV_INDEX_DIR, 0, 20,
                        ownerAuth );
                    System.out.println( ByteArrayUtil.toPrintableHexString( data ) );
                }
                catch ( TPMException e )
                {
                    TPMToolsUtil.handleTPMException( e );
                }

                TPMNVFuncs.dumpNVRAMNoAuth( TPM_NV_INDEX_EKCert );
                TPMNVFuncs.dumpNVRAMNoAuth( TPM_NV_INDEX_TPM_CC );
                TPMNVFuncs.dumpNVRAMNoAuth( TPM_NV_INDEX_PlatformCert );
                TPMNVFuncs.dumpNVRAMNoAuth( TPM_NV_INDEX_Platform_CC );
            }

        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }

        TPMToolsUtil.cleanupTPMDriver();
    }

}
