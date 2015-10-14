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
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

/**
 * Use this class as a superclass for authorization sessions.
 * Note that this class keeps a saved copy of the "sharedSecret",
 * which is created once and used several times in many
 * kinds of authorization sessions.
 * In the case of TPM_OIAP sessions, the sharedSecret can
 * change in between commands.  In this case, the user
 * can call this.setSharedSecret() to change (and save)
 * the sharedSecret.
 * 
 * @author lfgs
 */
public abstract class TPMAuthorizationSession
{
    protected TPMDriver tpmDriver;

    private boolean active = false;

    // The following are the data that needs to be saved
    // (according to the spec, Sec. 13.2.1 of the TPM 1.2 Design Principles Spec)
    protected int authHandle;
    protected TPM_NONCE authLastNonceEven;
    protected TPM_NONCE nonceOdd;

    // FIXME: Figure out the right way of replacing sharedSecret with all zeros secret if sharedSecret is null
    private TPM_SECRET sharedSecret;

    public TPMAuthorizationSession( TPMDriver tpmDriver )
    {
        this.tpmDriver = tpmDriver;
    }

    public TPMDriver getTpmDriver()
    {
        return tpmDriver;
    }

    public void setTpmDriver( TPMDriver tpmDriver )
    {
        this.tpmDriver = tpmDriver;
    }

    public TPM_SECRET getSharedSecret()
    {
        return sharedSecret;
    }

    /**
     * Set the saved sharedSecret of the session.
     * This can be used internally by subclasses, or
     * externally in the case of TPM_OIAP sessions
     * where the secret can change between different commands.
     */
    public void setSharedSecret( TPM_SECRET sharedSecret )
    {
        // FIXME: Figure out the right way of replacing sharedSecret with all zeros secret if sharedSecret is null
        this.sharedSecret = sharedSecret;
    }

    public boolean isActive()
    {
        return active;
    }

    public int getAuthHandle()
    {
        return authHandle;
    }

    public TPM_NONCE getAuthLastNonceEven()
    {
        return authLastNonceEven;
    }

    protected void setAuthLastNonceEven( TPM_NONCE authLastNonceEven )
    {
        this.authLastNonceEven = authLastNonceEven;
    }

    public TPM_NONCE getNonceOdd()
    {
        return nonceOdd;
    }

    public void setNonceOdd( TPM_NONCE nonceOdd )
    {
        this.nonceOdd = nonceOdd;
    }

    public void setActive( boolean active )
    {
        this.active = active;
    }

    protected void setAuthHandle( int authHandle )
    {
        this.authHandle = authHandle;
    }

    /**
     * Call this method after the session starting command has been sent to the
     * and the TPM has returned the authHandle
     * and authLastNonceEven.  This sets active to true.
     * <p>
     * NOTE: This should be called in a startSession method,
     * but it can also be called idependently in cases where
     * startSession cannot be used (e.g., when starting a session
     * inside a transport session).
     * 
     * @param authHandle
     * @param authLastNonceEven
     */
    public void initialize( int authHandle, TPM_NONCE authLastNonceEven )
    {
        this.authHandle = authHandle;
        this.authLastNonceEven = authLastNonceEven;

        Debug.println( "AuthHandle= ", this.authHandle, "\nauthLastNonceEven= ", this.authLastNonceEven );

        // if we reached this point, then there should be no errors.
        this.setActive( true );
    }

    //    /**
    //     * Generate the next AuthInData given a command.
    //     * This is called by execute, but can also be called externally
    //     * when using Auth2 commands (which would require calling
    //     * this for two separate authorization sessions).
    //     * Note that calling this method changes the state of the session.
    //     * The AuthInData field of the command itself is not touched,
    //     * so the output of this method should be explicitly written into the
    //     * command after this is called.
    //     * <p>
    //     * Note: this generates a random nonceOdd.
    //     * 
    //     * @param cmd -- Command to execute (with empty AuthInData structure field)
    //     * @param continueAuthSession -- true if continuing, false if this is the last command
    //     * @return
    //     */
    //    public TPMAuthInData generateAuthInData( TPMAuth1Command cmd,
    //        boolean continueAuthSession )
    //    {
    //        // TODO: Check for active state (?)
    //        return this.generateAuthInData(
    //            CryptoUtil.generateRandomNonce(), cmd, continueAuthSession );
    //    }

    //    /**
    //     * Version of generateAuthInData that
    //     * allows control over nonceOdd that is used.
    //     * 
    //     * @param newNonceOdd
    //     * @param cmd
    //     * @param continueAuthSession
    //     * @return
    //     */
    //    public TPMAuthInData generateAuthInData( TPM_NONCE newNonceOdd,
    //        TPMAuth1Command cmd, boolean continueAuthSession )
    //    {
    //        Debug.println( "in generateAuthInData:" );
    //
    //        this.nonceOdd = newNonceOdd;
    //
    //        byte[] inParamDigest = this.computeInParamDigest( cmd );
    //        // Note that this.computeInAuthSetupParams uses this.nonceOdd,
    //        // so it's important to set it above.
    //        byte[] inAuthSetupParams = this.computeInAuthSetupParams( continueAuthSession );
    //
    //        Debug.println( "inParamDigest: "
    //            + ByteArrayUtil.toPrintableHexString( inParamDigest ) );
    //        Debug.println( "inAuthSetupParams: "
    //            + ByteArrayUtil.toPrintableHexString( inAuthSetupParams ) );
    //
    ////        // NOTE: if secret is null, use all-zeros secret
    ////        TPM_SECRET secret = this.sharedSecret;
    ////        if ( secret == null )
    ////        {
    ////            secret = TPM_SECRET.NULL; // 
    ////        }
    //        
    //        TPM_DIGEST inAuth = CryptoUtil.computeHMAC_TPM_DIGEST(
    //            this.sharedSecret, inParamDigest, inAuthSetupParams );
    //
    //        TPMAuthInData authInData = new TPMAuthInData(
    //            this.authHandle, this.nonceOdd, continueAuthSession, inAuth );
    //        return authInData;
    //    }

    //    protected byte[] computeInParamDigest( TPMAuth1Command cmd )
    //    {
    //        Debug.println( "in computeInParamDigest:" );
    //
    //        byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( cmd.getOrdinal() );
    //        Object[] inParams = cmd.getInParamsForAuthDigest(this);
    //
    //        Debug.println( "ordinal: "
    //            + ByteArrayUtil.toPrintableHexString( ordinalBytes ) );
    //        Debug.println( "inParams: " );
    //        for ( Object o : inParams )
    //        {
    //            Debug.println( o.getClass().getSimpleName() + o.toString() );
    //        }
    //
    //        byte[] inParamsBytes = ByteArrayUtil.concatObjectsBE( inParams );
    //        byte[] text = ByteArrayUtil.concat( ordinalBytes, inParamsBytes );
    //
    //        Debug.println( "concat: " + ByteArrayUtil.toPrintableHexString( text ) );
    //
    //        return CryptoUtil.computeSHA1Hash( text );
    //    }

    public byte[] computeInAuthSetupParams( boolean continueAuthSession )
    {
        // Note: the OIAP description in the Design Principles spec
        // say that authHandle is included in inAuthSetupParams.
        // The OSAP description doesn't include inAuthSetupParams.
        // Looking at the source code of tpm-3.0.3, the authHandle is not
        // included.

        // byte[] authHandleBytes = ByteArrayUtil.toBytesInt32BE( this.authHandle );
        byte[] authLastNonceEvenBytes = this.authLastNonceEven.toBytes();
        byte[] nonceOddBytes = this.nonceOdd.toBytes();
        byte[] continueAuthSessionBytes =
            { (byte) (continueAuthSession ? 1 : 0) };
        return ByteArrayUtil.concat( authLastNonceEvenBytes, nonceOddBytes,
            continueAuthSessionBytes );
    }

    /**
     * Takes the output of a command and verifies the authorization
     * output data using the current state of the session.
     * Note that calling this saves the nonceEven.
     * 
     * @param ordinal -- the ordinal of the original command
     * @param output -- the whole output object
     * @param authOutData -- the authOutData to use, either getAuthOutData1() or getAuthOutData2() of output
     * @return
     */
    public boolean verifyAuthOutData( TPMCommand originatingCmd,
        TPMAuth1CommandOutput output, TPMAuthOutData authOutData )
    {
        // TODO: Check for active state (?)

        // apparently, in some cases (e.g., when reading the NVRAM with authorization
        // before it is locked), the TPM returns a no-authorization response (0xc4 tag)
        // Even if the operation may have succeeded, 
        // we will consider this a failure, so that the user may be alerted
        // and will know that he should call the operation with noAuth to begin with.

        if ( authOutData == null )
        {
            return false;
        }

        // Save the nonceEven here
        this.authLastNonceEven = authOutData.getNonceEven();

        // Compute HMAC
        byte[] outParamDigest = this.computeOutParamDigest( originatingCmd, output );
        byte[] outAuthSetupParams = this.computeOutAuthSetupParams( authOutData );
        TPM_DIGEST hmac = CryptoUtil.computeHMAC_TPM_DIGEST( this.sharedSecret,
            outParamDigest, outAuthSetupParams );
        TPM_DIGEST resAuth = authOutData.getResAuth();

        Debug.println( "resAuth (from TPM): ", resAuth );
        Debug.println( "hmac (computed): ", hmac );

        return hmac.equals( resAuth );
    }

    protected byte[] computeOutParamDigest( TPMCommand originatingCmd, // int ordinal,
        TPMAuth1CommandOutput output )
    {
        byte[] returnCode = ByteArrayUtil.toBytesInt32BE( output.getReturnCode() );
        byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( originatingCmd.getOrdinal() );
        byte[] outParams = output.getOutParamsForAuthDigest();
        return CryptoUtil.computeSHA1Hash( returnCode, ordinalBytes, outParams );
    }

    protected byte[] computeOutAuthSetupParams( TPMAuthOutData authOutData )
    {
        // Note: the OIAP description in the Design Principles spec
        // say that authHandle is included in outAuthSetupParams.
        // The OSAP description doesn't include outAuthSetupParams.
        // Looking at the source code of tpm-3.0.3, the authHandle is not
        // included.  Not including it actually works.

        // byte[] authHandleBytes = ByteArrayUtil.toBytesInt32BE( this.authHandle );
        byte[] nonceEvenBytes = authOutData.getNonceEven().toBytes();
        byte[] nonceOddBytes = this.nonceOdd.toBytes();
        byte[] continueAuthSessionBytes =
            { authOutData.getContinueAuthSessionByte() };
        byte[][] total =
            { nonceEvenBytes, nonceOddBytes, continueAuthSessionBytes };
        return ByteArrayUtil.concat( total );
    }

    /**
     * Execute a command within this authorization session.
     * Authorization and verification are automatically handled
     * by this method.  Errors will throw a TPMException.
     * 
     * @param cmd -- Command to execute (with empty AuthInData structure field)
     * @param continueAuthSession -- true if continuing, false if this is the last command
     * @return
     * @throws TPMException
     */
    public TPMOutputStruct executeAuth1Cmd( TPMAuth1Command cmd,
        boolean continueAuthSession ) throws TPMException
    {
        return TPMAuthorizationSession.executeAuth1Cmd( cmd, this,
            continueAuthSession );
    }

    public TPM_ENCAUTH encryptAuthWithEvenNonce( TPM_AUTHDATA auth )
    {
        byte[] concat = ByteArrayUtil.concat( this.getSharedSecret(),
            this.getAuthLastNonceEven() );
        TPM_DIGEST digest = CryptoUtil.computeTPM_DIGEST( concat );
        byte[] digestBytes = digest.toBytes();

        byte[] encData = CryptoUtil.xor( auth.toBytes(), digestBytes );
        return new TPM_ENCAUTH( encData );
    }

    public TPM_ENCAUTH encryptAuthWithOddNonce( TPM_AUTHDATA auth )
    {
        byte[] concat = ByteArrayUtil.concat( this.getSharedSecret(),
            this.getNonceOdd() );
        TPM_DIGEST digest = CryptoUtil.computeTPM_DIGEST( concat );
        byte[] digestBytes = digest.toBytes();

        byte[] encData = CryptoUtil.xor( auth.toBytes(), digestBytes );
        return new TPM_ENCAUTH( encData );
    }

    /**
     * Static method for invoking a command within a given authorization session.
     * Authorization and verification are automatically handled
     * by this method.  Errors will throw a TPMException.
     * 
     * @param cmd -- Command to execute (with empty AuthInData structure field)
     * @param authSession -- the TPMAuthorizationSession instance to use (must be active)
     * @param continueAuthSession -- true if continuing, false if this is the last command
     * @return
     * @throws TPMException
     */
    public static TPMOutputStruct executeAuth1Cmd( TPMAuth1Command cmd,
        TPMAuthorizationSession authSession, boolean continueAuthSession )
        throws TPMException
    {
        return cmd.execute( authSession, continueAuthSession );
    }

    //  { // old code
    //        if ( cmd.isNoAuth() )
    //        {
    //            Debug.println( "Got noAuth command.  Executing without using session ..." );
    //            return cmd.execute( authSession.getTpmDriver() );
    //        }
    //
    //        // Note: generateAuthInData generates and saves a new nonceOdd
    //        TPMAuthInData authInData = authSession.generateAuthInData(
    //            cmd, continueAuthSession );
    //        Debug.println( "generated AuthInData1: " + authInData );
    //        cmd.setAuthInData1( authInData );
    //
    //        // Note: at this point, cmd already includes the authInData
    //        // so any exceptions will include it
    //        TPMAuth1CommandOutput output = cmd.execute( authSession.getTpmDriver() );
    //
    //        Debug.println( "Verifiying output ..." );
    //
    //        boolean ok = authSession.verifyAuthOutData( cmd.getOrdinal(), output,
    //            output.getAuthOutData1() );
    //        
    //        // Note: at this point, cmd already includes the authOutData
    //        // so any exceptions will include it
    //
    //        // Note: verifyAuthOutData saves the nonceEven as authLastNonceEven
    //        if ( !ok )
    //        {
    //            throw new TPMAuthOutDataMismatchException( cmd, output );
    //            // TODO: Terminate handle?
    //        }
    //        else
    //        {
    //            if ( !continueAuthSession )
    //            {
    //                authSession.setActive( false );
    //                // TODO: Terminate handle? (I think the TPM automatically
    //                // terminates a handle if it sees continueAuthSession
    //                // false.
    //            }
    //            return output;
    //        }
    //  }

    /**
     * TODO: This is not tested.
     * Static method for invoking a command within a given authorization session.
     * Authorization and verification are automatically handled
     * by this method.  Errors will throw a TPMException.
     * 
     * @param cmd -- Command to execute (with empty AuthInData structure field)
     * @param authSession1 -- the 1st TPMAuthorizationSession instance to use (must be active)
     * @param continueAuthSession1 -- true if continuing 1st session, false if this is the last command
     * @param authSession2 -- the 2nd TPMAuthorizationSession instance to use (must be active)
     * @param continueAuthSession2 -- true if continuing 2nd session, false if this is the last command
     * @return
     * @throws TPMException
     */
    public static TPMOutputStruct executeAuth2Cmd( TPMAuth2Command cmd,
        TPMAuthorizationSession authSession1, boolean continueAuthSession1,
        TPMAuthorizationSession authSession2, boolean continueAuthSession2 )
        throws TPMException
    {
        return (TPMAuth2CommandOutput) cmd.execute( authSession1,
            continueAuthSession1, authSession2, continueAuthSession2 );
    }

}
