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
package edu.mit.csail.tpmj.counters;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_ExecuteTransportOutput;
import edu.mit.csail.tpmj.commands.TPM_IncrementCounter;
import edu.mit.csail.tpmj.commands.TPM_IncrementCounterOutput;
import edu.mit.csail.tpmj.commands.TPM_ReadCounter;
import edu.mit.csail.tpmj.commands.TPM_ReadCounterOutput;
import edu.mit.csail.tpmj.commands.TPM_ReleaseTransportSignedOutput;
import edu.mit.csail.tpmj.funcs.TPMOIAPSession;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_RESULT;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.transport.TPMTransportLog;
import edu.mit.csail.tpmj.transport.TPMTransportSession;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class CountStampFuncs extends TPMUtilityFuncs
{

    public static TPMTransportLog signedIncrementCounter( int countID,
        TPM_SECRET counterAuth, TPM_NONCE antiReplay, int keyHandle,
        TPM_SECRET keyAuth ) throws TPMException
    {
        TPMTransportLog transLog;
        TPMOIAPSession oiapSession = new TPMOIAPSession( tpmDriver );
        oiapSession.startSession();
        oiapSession.setSharedSecret( counterAuth );
        Debug.println( "Started OIAP session 0x"
            + Integer.toHexString( oiapSession.getAuthHandle() ) );

        TPMTransportSession transSession = new TPMTransportSession( tpmDriver,
            TPMConsts.TPM_TRANSPORT_LOG );

        int transHandle = transSession.startSession(
            TPMConsts.TPM_KH_TRANSPORT, null, TPM_SECRET.NULL.toBytes(),
            TPM_SECRET.NULL );

        Debug.println( "Started Transport Session 0x"
            + Integer.toHexString( transHandle ) );

        // FIXME: At present, we assume counterAuth is null
        TPM_IncrementCounter incCmd = new TPM_IncrementCounter( countID );

        TPM_NONCE newNonceOdd = CryptoUtil.generateRandomNonce();
        incCmd.computeAuthInData1( oiapSession, newNonceOdd, false );

        Debug.println( "incCmd with authData = ", incCmd );

        try
        {
            TPM_ExecuteTransportOutput output = transSession.executeTransport(
                incCmd, true );
            Debug.println( "Output: ", output );

            TPM_IncrementCounterOutput incOut = (TPM_IncrementCounterOutput) transSession.decryptOutput(
                output.getWrappedCmd(), incCmd );

            // FIXME: It's possible for the output to be an error.  Develop a general way of handling this.
            if ( incOut.getReturnCode() != 0 )
            {
                Debug.println( "Wrapped command returned error code: ",
                    incOut.getReturnCode() + " ",
                    TPM_RESULT.getErrorName( incOut.getReturnCode() ) );

                // throw new TPMErrorReturnCodeException( incCmd, incOut, "Wrapped Command resulted in error return code." );
            }
            else
            {
                TPM_COUNTER_VALUE counterValue = incOut.getCount();
                Debug.println( "Counter Value: ", counterValue );
            }
        }
        catch ( TPMException e )
        {
            Debug.println( "Error during executeTransport: ", e );
            TPMToolsUtil.handleTPMException( e );
        }

        TPM_ReleaseTransportSignedOutput signOut = transSession.releaseTransportSigned(
            keyHandle, antiReplay, keyAuth );

        Debug.println( "Output: ", signOut );

        transLog = transSession.getLog();
        return transLog;
    }

    /**
     * @param countID
     * @param keyHandle
     * @param keyAuth
     * @param tpmDriver
     * @return
     * @throws TPMException
     */
    public static TPMTransportLog signedReadCounter( int countID,
        TPM_NONCE antiReplay, int keyHandle, TPM_SECRET keyAuth )
        throws TPMException
    {
        TPMTransportLog transLog;
        TPMTransportSession transSession = new TPMTransportSession( tpmDriver,
            TPMConsts.TPM_TRANSPORT_LOG );

        int transHandle = transSession.startSession(
            TPMConsts.TPM_KH_TRANSPORT, null, TPM_SECRET.NULL.toBytes(),
            TPM_SECRET.NULL );

        Debug.println( "Started Transport Session 0x"
            + Integer.toHexString( transHandle ) );

        TPM_ReadCounter readCounterCmd = new TPM_ReadCounter( countID );

        try
        {
            TPM_ExecuteTransportOutput output = transSession.executeTransport(
                readCounterCmd, true );
            Debug.println( "Output: ", output );

            TPM_ReadCounterOutput readOut = (TPM_ReadCounterOutput) transSession.decryptOutput(
                output.getWrappedCmd(), readCounterCmd );
            TPM_COUNTER_VALUE counterValue = readOut.getCount();
            Debug.println( "Counter Value: ", counterValue );

        }
        catch ( TPMException e )
        {
            System.out.println( "Error during executeTransport: " + e );
            TPMToolsUtil.handleTPMException( e );
        }

        TPM_ReleaseTransportSignedOutput signOut = transSession.releaseTransportSigned(
            keyHandle, antiReplay, keyAuth );

        Debug.println( "Output: ", signOut );

        transLog = transSession.getLog();
        return transLog;
    }

    /**
     * @param dataBytes
     * @param opType
     * @param countID
     * @param counterAuth
     * @param keyHandle
     * @param keyAuth
     * @return
     * @throws TPMException
     */
    public static CountStamp createCountStamp( byte[] dataBytes, int opType,
        int countID, TPM_SECRET counterAuth, int keyHandle, TPM_SECRET keyAuth )
        throws TPMException
    {
        TPM_NONCE antiReplay = CryptoUtil.computeTPM_DIGEST( dataBytes );

        System.out.println( "Hash of data (to be used as nonce): " + antiReplay );

        TPMTransportLog transLog = null;
        if ( opType == TPMConsts.TPM_ORD_ReadCounter )
        {
            transLog = signedReadCounter( countID, antiReplay, keyHandle,
                keyAuth );
        }
        else if ( opType == TPMConsts.TPM_ORD_IncrementCounter )
        {
            transLog = signedIncrementCounter( countID, counterAuth,
                antiReplay, keyHandle, keyAuth );
        }

        CountStamp countStamp = new TPMCountStamp( transLog );
        return countStamp;
    }

    public static boolean verifyCountStamp( byte[] dataBytes,
        TPMCountStamp countStamp, byte[] modulus )
    {
        TPM_NONCE dataHashFromStamp = countStamp.getNonce();
        TPM_NONCE dataHashFromData = CryptoUtil.computeTPM_DIGEST( dataBytes );
        return (dataHashFromData.equals( dataHashFromStamp ) && countStamp.verify( modulus ));
    }
}
