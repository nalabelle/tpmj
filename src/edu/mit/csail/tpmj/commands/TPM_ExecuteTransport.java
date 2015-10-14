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
package edu.mit.csail.tpmj.commands;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.structs.ByteArrayTPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPMAuthInData;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.transport.TPMTransportSession;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPM_ExecuteTransport extends TPMAuth1Command
{
    private TPMCommand plainWrappedCmd;
    private byte[] wrappedCmd;

    public TPM_ExecuteTransport()
    {
        super( TPMConsts.TPM_ORD_ExecuteTransport );
        // needed for instantiation of new instance before calling fromBytes
    }


    /**
     * This creates a new TPM_ExecuteTransport command given the unencrypted 
     * command to be wrapped, and the Transport session this command will
     * be executed in.  This calls encryptCommand with the transport session.
     * 
     * @param plainWrappedCmd
     * @param transSession
     */
    public TPM_ExecuteTransport( TPMCommand plainWrappedCmd, TPMTransportSession transSession )
    {
        super( TPMConsts.TPM_ORD_ExecuteTransport );
        this.plainWrappedCmd = plainWrappedCmd;
        
        this.encryptCommand( transSession );
        this.setParamSize( this.computeParamSize( 10 ) + 4
            + this.getWrappedCmdSize() );
    }
    
    /**
     * This method calls the transport session to encrypt the wrapped command.
     * 
     * @param transSession
     * @return
     */
    protected void encryptCommand( TPMTransportSession transSession )
    {
        byte[] wrappedBytes = null;
        
        if ( transSession.isEncrypted() )
        {
            // TODO: support encrypted transport sessions
            throw new UnsupportedOperationException(
                "Encrypted Transport Sessions are not yet supported by this version of TPM/J" );
        }
        else
        {
            wrappedBytes = this.getPlainWrappedCmd().toBytes();
        }
        this.setWrappedCmd( wrappedBytes );
    }
    
    /**
     * @param transSession
     * @return
     */
    public TPMOutputStruct decryptOutput( TPM_ExecuteTransportOutput execOut, TPMTransportSession transSession )
    {
        byte[] encOutput = execOut.getWrappedCmd();

        byte[] unEncCmd = null;
        if ( transSession.isEncrypted() )
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
            TPMOutputStruct out = (TPMOutputStruct) this.getPlainWrappedCmd().getReturnType().newInstance();
            out.fromBytes( unEncCmd, 0 );
            return out;
        }
        catch ( Exception e )
        {
            return new ByteArrayTPMOutputStruct( unEncCmd );
        }
    }

    public TPMCommand getPlainWrappedCmd()
    {
        return plainWrappedCmd;
    }


    public void setPlainWrappedCmd( TPMCommand plainWrappedCmd )
    {
        this.plainWrappedCmd = plainWrappedCmd;
    }


    public byte[] getWrappedCmd()
    {
        return wrappedCmd;
    }

    public int getWrappedCmdSize()
    {
        return ( this.wrappedCmd == null ) ? 0 : this.wrappedCmd.length;
    }

    /**
     * This sets the encrypted wrappedCmd, and also sets param size properly.
     * This is called in the constructor and in encryptCommand()
     *  
     * @param wrappedCmd
     */
    public void setWrappedCmd( byte[] wrappedCmd )
    {
        if ( wrappedCmd == null )
        {
            this.wrappedCmd = new byte[0];
        }
        else
        {
            this.wrappedCmd = wrappedCmd;
        }
        
        this.setParamSize( this.computeParamSize( 10 ) + 4
            + this.getWrappedCmdSize() );
    }

    @Override
    public Class getReturnType()
    {
        return TPM_ExecuteTransportOutput.class;
    }

    @Override
    public TPM_ExecuteTransportOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_ExecuteTransportOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_ExecuteTransportOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_ExecuteTransportOutput) super.execute( authSession1,
            continueAuthSession1 );
    }
    
    
    
    /**
     * Encrypts the plainWrappedCmd using the transport session.
     */
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        if ( !(authSession instanceof TPMTransportSession) )
        {
            throw new IllegalArgumentException( "TPM_ExecuteTransport must be used with a transport session." );
        }
        
        this.encryptCommand( (TPMTransportSession) authSession );
    }


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
    protected byte[] computeInParamDigest()
    {
        Debug.println( "in TPMExecuteTransport.computeAuthInData1:" );
        
        byte[] origCmdBytes = this.plainWrappedCmd.toBytes();

        byte[] bytesToDigest = this.computeWrappedCmdParamBytesToDigest( origCmdBytes );
        TPM_DIGEST wrappedCmdDigest = CryptoUtil.computeTPM_DIGEST( bytesToDigest );

        Object[] inParams =
            { this.getWrappedCmdSize(), wrappedCmdDigest };

        // NOTE: The below code doesn't pass the authentication.  The above code seems to work.
        //            Object[] inParams =
        //                { this.getWrappedCmdSize(), bytesToDigest };

        byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( this.getOrdinal() );

//        Debug.println( "ordinal: "
//            + ByteArrayUtil.toPrintableHexString( ordinalBytes ) );
//        Debug.println( "inParams: " );
//        for ( Object o : inParams )
//        {
//            Debug.println( o.getClass().getSimpleName() + ": "
//                + o.toString() );
//        }

        byte[] inParamsBytes = ByteArrayUtil.concatObjectsBE( inParams );
        byte[] text = ByteArrayUtil.concat( ordinalBytes, inParamsBytes );

//        Debug.println( "concat: "
//            + ByteArrayUtil.toPrintableHexString( text ) );

        return CryptoUtil.computeSHA1Hash( text );
    }


    
    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.getWrappedCmdSize(), this.wrappedCmd );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );
        int wrappedCmdSize = brw.readInt32();
        this.setWrappedCmd( brw.readBytes( wrappedCmdSize ) );

        this.readAuthData( source, offset );
    }

    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        throw new UnsupportedOperationException( "TPM_ExecuteTranport.getInParamsForAuthDigest must never be called directly.  TPMTransportSession handles it as a special case in computeInParamDigest." ); 
        // return null;
    }


    @Override
    public String toString()
    {
        return super.toString() + "\n"
            + "wrappedCmd = " + ByteArrayUtil.toPrintableHexString( this.wrappedCmd ) + "\n"
            + "(original = " + this.plainWrappedCmd + ")";
    }

    
}
