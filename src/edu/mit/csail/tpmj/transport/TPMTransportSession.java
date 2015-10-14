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
package edu.mit.csail.tpmj.transport;

import java.util.ArrayList;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthSessionStateException;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.funcs.TPMOIAPSession;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPMTransportSession extends TPMAuthorizationSession
{
    private TPM_TRANSPORT_PUBLIC transPublic;

//    private TPMCommand lastExecutedCommand = null;

    private TPMTransportLog log;
    private TPM_TRANSPORT_LOG_IN inLog;
    private TPM_TRANSPORT_LOG_OUT outLog;

    /**
     * Creates a session object.
     * Doesn't connect yet.  Call startSession to connect.
     *  
     * @param tpmDriver
     */
    public TPMTransportSession( TPMDriver tpmDriver, int transAttributes,
        int algID, short encScheme )
    {
        super( tpmDriver );
        this.transPublic = new TPM_TRANSPORT_PUBLIC( transAttributes, algID,
            encScheme );
        this.log = new TPMTransportLog( tpmDriver.getTPMManufacturer() );
    }

    /**
     * Creates a session object.
     * Doesn't connect yet.  Call startSession to connect.
     *  
     * @param tpmDriver
     */
    public TPMTransportSession( TPMDriver tpmDriver, int transAttributes )
    {
        this( tpmDriver, transAttributes, TPMConsts.TPM_ALG_MGF1,
            TPMConsts.TPM_ES_NONE );
    }

    public TPMTransportLog getLog()
    {
        return log;
    }

    /**
     */
    private void addLogEntry( TPMCommand origCmd, TPMCommand transCmd,
        TPMOutputStruct transOut, TPMOutputStruct unencOut )
    {
        TPMTransportLogEntry logEntry = new TPMTransportLogEntry( origCmd,
            transCmd, transOut, unencOut );
        this.log.add( logEntry );
    }

    public boolean isEncrypted()
    {
        return (this.transPublic.getTransAttributes() & TPMConsts.TPM_TRANSPORT_ENCRYPT) != 0;
    }

    public boolean isExclusive()
    {
        return (this.transPublic.getTransAttributes() & TPMConsts.TPM_TRANSPORT_EXCLUSIVE) != 0;
    }

    public boolean isLogged()
    {
        return (this.transPublic.getTransAttributes() & TPMConsts.TPM_TRANSPORT_LOG) != 0;
    }

//    public byte[] encryptCommand( TPMCommand cmd )
//    {
//        this.lastExecutedCommand = cmd;
//
//        byte[] wrappedCmd = null;
//        if ( this.isEncrypted() )
//        {
//            // TODO: support encrypted transport sessions
//            throw new UnsupportedOperationException(
//                "Encrypted Transport Sessions are not yet supported by this version of TPM/J" );
//        }
//        else
//        {
//            wrappedCmd = cmd.toBytes();
//        }
//        return wrappedCmd;
//    }

    public int startSession( int encHandle, TPM_SECRET keyAuth,
        byte[] encSecret, TPM_SECRET plainSecret ) throws TPMException
    {
        if ( this.isActive() )
        {
            throw new TPMAuthSessionStateException(
                "Attempt to startSession on an active transport session." );
        }

        boolean useAuth = true;

        if ( (encHandle == TPMConsts.TPM_KH_TRANSPORT) || (keyAuth == null) )
        {
            useAuth = false;
            // keyAuth = TPM_SECRET.NULL;
        }

        // TODO: Call TPM_Reset() to be safe ?

        TPM_EstablishTransport cmd = new TPM_EstablishTransport( encHandle,
            transPublic, encSecret );
        if ( !useAuth )
        {
            cmd.setNoAuth();
        }

        TPM_EstablishTransportOutput output = (TPM_EstablishTransportOutput) TPMOIAPSession.executeOIAPSession(
            tpmDriver, cmd, keyAuth );

        // Add commands to log
        // NOTE: We also add to the origCommands and unEncOutputs lists to keep
        // them in sync with the commands and output lists
        this.addLogEntry( cmd, cmd, output, output );

        // Save transHandle (as authHandle) and transNonce (as authLastNonceEven)
        this.initialize( output.getTransHandle(), output.getTransNonce() );

        // Save info on the transport session

        // the shared secret is the plainSecret
        this.setSharedSecret( plainSecret );

        return this.getAuthHandle();
    }

    public TPMOutputStruct decryptOutput( byte[] encOutput, TPMCommand origCmd )
    {
        byte[] unEncCmd = null;
        if ( this.isEncrypted() )
        {
            // TODO: support encrypted transport sessions
            throw new UnsupportedOperationException(
                "Encrypted Transport Sessions are not yet supported by this version of TPM/J" );
        }
        else
        {
            unEncCmd = encOutput;
        }

        try
        {
            // FIXME: decryptOutput can only be called while the lastExecutedCommand is still correct 
            TPMOutputStruct out = (TPMOutputStruct) origCmd.getReturnType().newInstance();
            out.fromBytes( unEncCmd, 0 );
            return out;
        }
        catch ( Exception e )
        {
            return new ByteArrayTPMOutputStruct( unEncCmd );
        }
    }

    /**
     * Convenience method for doing an executeTransport on cmd
     * and returning the unwrapped output (instead of the TPM_ExecuteTransportOutput).
     * 
     * @param cmd
     * @return
     * @throws TPMException
     */
    public TPMOutputStruct wrapAndExecuteCmd( TPMCommand cmd )
        throws TPMException
    {
        // Old code.  Won't work anymore because of this.decryptOutput
//        TPM_ExecuteTransportOutput execOut = this.executeTransport( cmd, true );
//        byte[] encOutput = execOut.getWrappedCmd();
//        TPMOutputStruct unEncOut = this.decryptOutput( encOutput );
//        return unEncOut;

//        Debug.println( "In TPMTransportSession.wrapAndExecuteCmd ..." );

        // TODO: Merge this code with that in executeTransport.  It is the same except it returns unencOut instead of execOut
        TPM_ExecuteTransport execCmd = new TPM_ExecuteTransport( cmd, this );
        
        // NOTE: executeAuth1Cmd will invoke execCmd.computeEncryptedPasswords() which will encrypt the wrapped cmd, cmd

//        Debug.println( "Created execCmd: " + execCmd );
//        Debug.println( "Executing ..." );

        TPM_ExecuteTransportOutput execOut = (TPM_ExecuteTransportOutput) super.executeAuth1Cmd(
            execCmd, true );

//        Debug.println( "Got execOut: " + execOut );
//        Debug.println( "Done." );
        
        TPMOutputStruct unencOut = execCmd.decryptOutput( execOut, this );
        
//        Debug.println( "Got unencOut: " + unencOut );
        
        // NOTE: We add execCmd to the log AFTER it is executed so it will have all the auth data in it.
        this.addLogEntry( cmd, execCmd, execOut, unencOut );

        return unencOut;
    }

    public TPM_ExecuteTransportOutput executeTransport( TPMCommand cmd,
        boolean continueAuthSession ) throws TPMException
    {
//        Debug.println( "In TPMTransportSession.executeTransport ..." );
        
        TPM_ExecuteTransport execCmd = new TPM_ExecuteTransport( cmd, this );
        
        // NOTE: executeAuth1Cmd will invoke execCmd.computeEncryptedPasswords() which will encrypt the wrapped cmd, cmd

//        Debug.println( "Created execCmd: " + execCmd );
//        Debug.println( "Executing ..." );
        
        TPM_ExecuteTransportOutput execOut = (TPM_ExecuteTransportOutput) super.executeAuth1Cmd(
            execCmd, continueAuthSession );

//        Debug.println( "Got execOut: " + execOut );
//        Debug.println( "Done." );
        
        TPMOutputStruct unencOut = execCmd.decryptOutput( execOut, this );
        
//        Debug.println( "Got unencOut: " + unencOut );
        
        // NOTE: We add execCmd to the log AFTER it is executed so it will have all the auth data in it.
        this.addLogEntry( cmd, execCmd, execOut, unencOut );

//        Debug.println( "Added Log entry: " + unencOut );
        
        return execOut;
    }

    public TPM_ReleaseTransportSignedOutput releaseTransportSigned(
        int keyHandle, TPM_NONCE antiReplay, TPM_SECRET keyAuth )
        throws TPMException
    {
        TPM_ReleaseTransportSigned cmd = new TPM_ReleaseTransportSigned(
            keyHandle, antiReplay );

        TPMOIAPSession keySession = new TPMOIAPSession( tpmDriver );
        keySession.startSession();
        keySession.setSharedSecret( keyAuth );

        TPM_ReleaseTransportSignedOutput output = (TPM_ReleaseTransportSignedOutput) cmd.execute(
            keySession, false, this, false );

        // Add commands to log
        // NOTE: We also add to the origCommands and unEncOutputs lists to keep
        // them in sync with the commands and output lists
        this.addLogEntry( cmd, cmd, output, output );

        return output;
    }

//    @Override
//    protected byte[] computeInParamDigest( TPMAuth1Command cmd )
//    {
//        // TODO: transfer logic from TPM_ExecuteTransport to here
//        if ( cmd instanceof TPM_ExecuteTransport )
//        {
////            Debug.println( "inside computeInParamDigest for TPM_ExecuteTransport ..." );
//            TPM_ExecuteTransport execCmd = (TPM_ExecuteTransport) cmd;
//
//            byte[] origCmdBytes = this.lastExecutedCommand.toBytes();
//
//            byte[] bytesToDigest = computeWrappedCmdParamBytesToDigest( origCmdBytes );
//            TPM_DIGEST wrappedCmdDigest = CryptoUtil.computeTPM_DIGEST( bytesToDigest );
//
//            Object[] inParams =
//                { execCmd.getWrappedCmdSize(), wrappedCmdDigest };
//
//            // NOTE: The below code doesn't pass the authentication.  The above code seems to work.
//            //            Object[] inParams =
//            //                { execCmd.getWrappedCmdSize(), bytesToDigest };
//
//            byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( execCmd.getOrdinal() );
//
////            Debug.println( "ordinal: "
////                + ByteArrayUtil.toPrintableHexString( ordinalBytes ) );
////            Debug.println( "inParams: " );
////            for ( Object o : inParams )
////            {
////                Debug.println( o.getClass().getSimpleName() + ": "
////                    + o.toString() );
////            }
//
//            byte[] inParamsBytes = ByteArrayUtil.concatObjectsBE( inParams );
//            byte[] text = ByteArrayUtil.concat( ordinalBytes, inParamsBytes );
//
////            Debug.println( "concat: "
////                + ByteArrayUtil.toPrintableHexString( text ) );
//
//            return CryptoUtil.computeSHA1Hash( text );
//
//        }
//        else
//        {
//            return super.computeInParamDigest( cmd );
//        }
//    }

    /**
     * @param origCmdBytes
     * @return
     */
    public static byte[] computeWrappedCmdParamBytesToDigest(
        byte[] origCmdBytes )
    {
        // NOTE: according to the TPM specs, the HMAC is calculated using
        // the SHA-1 of the ordinal and data part of the wrapped command
        // (without the key handles and authorization trailer)

        // TODO: FIXME: Maybe do this smarter by defining a TPMWrappedCommand class

        short wrappedCmdTag = ByteArrayUtil.readShortBE( origCmdBytes,
            TPMCommand.TAG_OFFSET );
        int wrappedOrdinal = ByteArrayUtil.readInt32BE( origCmdBytes,
            TPMCommand.ORDINAL_OFFSET );
        byte[] wrappedData = null;
        if ( wrappedCmdTag == TPMConsts.TPM_TAG_RQU_COMMAND )
        {
            wrappedData = ByteArrayUtil.readBytes( origCmdBytes,
                TPMCommand.BODY_OFFSET, origCmdBytes.length
                    - TPMCommand.BODY_OFFSET );
        }
        else if ( wrappedCmdTag == TPMConsts.TPM_TAG_RQU_AUTH1_COMMAND )
        {
            // FIXME: !!! For now, this will only work on commands WITHOUT keyHandles ...
            wrappedData = ByteArrayUtil.readBytes( origCmdBytes,
                TPMCommand.BODY_OFFSET, origCmdBytes.length
                    - TPMCommand.BODY_OFFSET - TPMAuthInData.STRUCT_SIZE );
        }
        else
        {
            // FIXME: !!! For now, this will only work on commands with only 1 keyHandle ...
            wrappedData = ByteArrayUtil.readBytes( origCmdBytes,
                TPMCommand.BODY_OFFSET + 4, origCmdBytes.length
                    - TPMCommand.BODY_OFFSET - 4
                    - (TPMAuthInData.STRUCT_SIZE * 2) );
        }

        byte[] bytesToDigest = ByteArrayUtil.concatObjectsBE( wrappedOrdinal,
            wrappedData );
        
//        Debug.println( "wrappedCmdParamBytesToDigest: " + ByteArrayUtil.toPrintableHexString( bytesToDigest ) );
        
        return bytesToDigest;
    }

    @Override
    protected byte[] computeOutParamDigest( TPMCommand originatingCmd,
        TPMAuth1CommandOutput output )
    {
        if ( output instanceof TPM_ExecuteTransportOutput )
        {
            // FIXME: Move this to TPM_ExecuteTransportOutput
            
//            Debug.println( "inside computeOutParamDigest for TPM_ExecuteTransport ..." );

            TPM_ExecuteTransportOutput excOut = (TPM_ExecuteTransportOutput) output;

            byte[] returnCode = ByteArrayUtil.toBytesInt32BE( excOut.getReturnCode() );
            byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( originatingCmd.getOrdinal() );

            byte[] excOutBytes = excOut.getInternalByteArray();
            int paramOffset = excOut.BODY_OFFSET;
            int length = excOut.WRAPPEDCMD_OFFSET - paramOffset;
            byte[] excOutTransOutData = ByteArrayUtil.readBytes( excOutBytes,
                paramOffset, length );
            byte[] wrappedOutData = excOut.getWrappedCmd();

            TPMCommand plainWrappedCmd = ((TPM_ExecuteTransport) originatingCmd).getPlainWrappedCmd();
            TPMOutputStruct unwrappedOut = this.decryptOutput( wrappedOutData, plainWrappedCmd );
            int wrappedOrdinal = plainWrappedCmd.getOrdinal();

            byte[] bytesToDigest = computeUnwrappedOutputParamBytesToDigest(
                wrappedOrdinal, unwrappedOut );
            TPM_DIGEST wrappedOutDigest = CryptoUtil.computeTPM_DIGEST( bytesToDigest );

            byte[] outParams = ByteArrayUtil.concatObjectsBE(
                excOutTransOutData, wrappedOutDigest );

            return CryptoUtil.computeSHA1Hash( returnCode, ordinalBytes,
                outParams );
        }
        else
        {
            return super.computeOutParamDigest( originatingCmd, output );
        }
    }

    /**
     * @param unwrappedOut
     * @return
     */
    public static byte[] computeUnwrappedOutputParamBytesToDigest(
        int wrappedOrdinal, TPMOutputStruct unwrappedOut )
    {
        // from getOutParamsForAuthDigest
        int wrappedRC = unwrappedOut.getReturnCode();

        byte[] unwrappedData = unwrappedOut.toBytes();
        byte[] dataW = null;

        short wrappedOutTag = ByteArrayUtil.readShortBE( unwrappedData,
            TPMCommand.TAG_OFFSET );

        // FIXME: !!! For now, these will only work on commands WITHOUT keyHandles ...
        if ( wrappedOutTag == TPMConsts.TPM_TAG_RSP_COMMAND )
        {
            dataW = ByteArrayUtil.readBytes( unwrappedData,
                TPMOutputStruct.BODY_OFFSET, unwrappedData.length
                    - TPMOutputStruct.BODY_OFFSET );
        }
        else if ( wrappedOutTag == TPMConsts.TPM_TAG_RSP_AUTH1_COMMAND )
        {
            dataW = ByteArrayUtil.readBytes( unwrappedData,
                TPMCommand.BODY_OFFSET, unwrappedData.length
                    - TPMOutputStruct.BODY_OFFSET - TPMAuthOutData.STRUCT_SIZE );
        }
        else
        {
            dataW = ByteArrayUtil.readBytes( unwrappedData,
                TPMCommand.BODY_OFFSET, unwrappedData.length
                    - TPMOutputStruct.BODY_OFFSET
                    - (TPMAuthOutData.STRUCT_SIZE * 2) );
        }

        byte[] bytesToDigest = ByteArrayUtil.concatObjectsBE( wrappedRC,
            wrappedOrdinal, dataW );
        
//        Debug.println( "unwrappedOutParamBytesToDigest: " + ByteArrayUtil.toPrintableHexString( bytesToDigest ) );
        
        return bytesToDigest;
    }

}
