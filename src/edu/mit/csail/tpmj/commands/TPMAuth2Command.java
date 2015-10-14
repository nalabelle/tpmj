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
import edu.mit.csail.tpmj.funcs.TPMAuthOutDataMismatchException;
import edu.mit.csail.tpmj.funcs.TPMAuthSessionStateException;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.structs.TPMAuthInData;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

/**
 * @author lfgs
 */
public abstract class TPMAuth2Command extends TPMAuth1Command
{
    private TPMAuthInData authInData2;

    /**
     * Creates a TPMCommand with a 
     * and tag TPMConsts.TPM_TAG_RQU_AUTH2_COMMAND
     * and the given ordinal.
     */
    public TPMAuth2Command( int ordinal )
    {
        super( ordinal );
        this.setTag( TPMConsts.TPM_TAG_RQU_AUTH2_COMMAND );
    }

    
    @Override
    /**
     * Throws an UnsupportedOperationException
     */
    public void setNoAuth()
    {
        throw new UnsupportedOperationException( "TPMAuth2Command cannot be set to NoAuth" );
    }

    /**
     * Sets the paramSize field to the correct total size,
     * given the size of the input parameters <b>excluding</b>
     * the auth parameters.  (In this case, it adds 2 * TPMAuthInData.STRUCT_SIZE.)
     * 
     * @param dataParamSize -- size of input parameters, including tag, but not including authentication structs
     */
    @Override
    public int computeParamSize( int dataParamSize )
    {
        return dataParamSize + (2 * TPMAuthInData.STRUCT_SIZE);
    }

    public TPMAuthInData getAuthInData2()
    {
        return authInData2;
    }

    public void setAuthInData2( TPMAuthInData authInData2 )
    {
        this.authInData2 = authInData2;
    }

    /**
     * Note: this is overridden since the AuthData1Offset is now
     * calculated as this.getParamSize() - 2 * TPMAuthInData.STRUCT_SIZE
     */
    @Override
    protected int getAuthData1Offset()
    {
        if ( this.isNoAuth() )
        {
            return this.getParamSize();
        }
        else
        {
            return this.getParamSize() - (2 * TPMAuthInData.STRUCT_SIZE);
        }
    }

    protected int getAuthData2Offset()
    {
        if ( this.isNoAuth() )
        {
            return this.getParamSize();
        }
        else
        {
            return this.getParamSize() - TPMAuthInData.STRUCT_SIZE;
        }
    }
    
    
    /**
     * Given source and the offset <b>of the start of the command
     * structure</b>, automatically computes the offset of the
     * authorization data (by subtracting from paramSize)
     * and reads the auth data from there.
     * 
     * @param source
     * @param headerOffset
     */
    @Override
    protected void readAuthData( byte[] source, int headerOffset )
    {
        // read the 1st auth data
        this.authInData1 = new TPMAuthInData();
        ByteArrayUtil.readStruct( source, this.getAuthData1Offset(), this.authInData1 );

        // read the 2nd auth data
        this.authInData2 = new TPMAuthInData();
        ByteArrayUtil.readStruct( source, this.getAuthData2Offset(), this.authInData2 );
    }

    /**
     * This is called in computeAuthInData1and2 to allow any passwords
     * to be encrypted according to the nonces and shared secrets in
     * the authSessions.  (When calling this alone, this should be called after
     * setting the nonceOdds on the sessions since some commands use
     * the nonceOdds to encrypt passwords.)
     * 
     * @param authSession
     */
    public abstract void computeEncryptedPasswords( TPMAuthorizationSession authSession1, TPMAuthorizationSession authSession2 );


    
    
    @Override
    /**
     * Overrides superclass to restrict return type to TPMAuth2CommandOutput.
     * Subclasses of this class must make sure that their own return
     * types are subclasses of TPMAuth2CommandOutput.
     */
    public TPMAuth2CommandOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPMAuth2CommandOutput) super.execute( tpmDriver );
    }

    @Override
    /**
     * Overrides superclass to restrict return type and to
     * also throw an UnsupportedOperationException
     */
    public TPMAuth2CommandOutput execute( TPMAuthorizationSession authSession, boolean continueAuthSession ) throws TPMException
    {
        throw new UnsupportedOperationException( "A TPMAuth2Command cannot be executed with only 1 auth session." );
    }
    
    
    public void computeAuthInData1and2( TPMAuthorizationSession authSession1,
        TPM_NONCE newNonceOdd1, boolean continueAuthSession1, 
        TPMAuthorizationSession authSession2, TPM_NONCE newNonceOdd2,
        boolean continueAuthSession2 )
    {
        Debug.println( "in TPMAuth2Command.computeAuthInData1and2:" );

        authSession1.setNonceOdd( newNonceOdd1 );
        authSession2.setNonceOdd( newNonceOdd2 );

        // NOTE: password computation may use nonceOdd, so it's important
        // to set it above.
        this.computeEncryptedPasswords( authSession1, authSession2 );
        
        byte[] inParamDigest = this.computeInParamDigest();
        
        // Note that computeInAuthSetupParams uses nonceOdd,
        // so it's important to set it above.

        // Set up authData1
        byte[] inAuthSetupParams1 = authSession1.computeInAuthSetupParams( continueAuthSession1 );

        Debug.println( "inParamDigest1: ", inParamDigest );
        Debug.println( "inAuthSetupParams1: ", inAuthSetupParams1 );

        TPM_DIGEST inAuth1 = CryptoUtil.computeHMAC_TPM_DIGEST(
            authSession1.getSharedSecret(), inParamDigest, inAuthSetupParams1 );
        TPMAuthInData authInData1 = new TPMAuthInData( authSession1.getAuthHandle(),
            authSession1.getNonceOdd(), continueAuthSession1, inAuth1 );
        this.setAuthInData1( authInData1 );

        // Set up authData2
        byte[] inAuthSetupParams2 = authSession2.computeInAuthSetupParams( continueAuthSession2 );

        Debug.println( "inParamDigest2: ", inParamDigest );
        Debug.println( "inAuthSetupParams2: ", inAuthSetupParams2 );

        TPM_DIGEST inAuth2 = CryptoUtil.computeHMAC_TPM_DIGEST(
            authSession2.getSharedSecret(), inParamDigest, inAuthSetupParams2 );
        TPMAuthInData authInData2 = new TPMAuthInData( authSession2.getAuthHandle(),
            authSession2.getNonceOdd(), continueAuthSession2, inAuth2 );
        this.setAuthInData2( authInData2 );
    }


    /**
     * Executes this command using the given authorization session.
     * Subclasses of this class must override this method and
     * make sure that their own return
     * types are subclasses of TPMAuth1CommandOutput.
     */
    public TPMAuth2CommandOutput execute( TPMAuthorizationSession authSession1,
        boolean continueAuthSession1, TPMAuthorizationSession authSession2,
        boolean continueAuthSession2 ) throws TPMException
    {
//        return (TPMAuth2CommandOutput) TPMAuthorizationSession.executeAuth2Cmd(
//            this, authSession1, continueAuthSession1, authSession2,
//            continueAuthSession2 );
        
            if ( authSession1.getTpmDriver() != authSession2.getTpmDriver() )
            {
                throw new IllegalArgumentException( "Both authSessions should be using the same TPMDriver instance.");
            }

            if ( this.isNoAuth() )
            {
                Debug.println( "Got noAuth command.  Executing without using session ..." );
                return this.execute( authSession1.getTpmDriver() );
            }
            
            if ( this.isAuth1() )
            {
                Debug.println( "Got Auth1 command.  Executing using only authSession1 ... " );
                return this.execute( authSession1, continueAuthSession1 );
            }

            if ( !authSession1.isActive() )
            {
                throw new TPMAuthSessionStateException(
                    this, "authSession 1 is inactive" );
            }
            if ( !authSession2.isActive() )
            {
                throw new TPMAuthSessionStateException(
                    this, "authSession 2 is inactive" );
            }

            TPM_NONCE newNonceOdd1 = CryptoUtil.generateRandomNonce();
            TPM_NONCE newNonceOdd2 = CryptoUtil.generateRandomNonce();
            
            
            this.computeAuthInData1and2( authSession1, newNonceOdd1, continueAuthSession1,
                authSession2, newNonceOdd2, continueAuthSession2 );
            
            Debug.println( "generated AuthInData1: ", this.getAuthInData1() );
            Debug.println( "generated AuthInData2: ", this.getAuthInData2() );

            // Note: at this point, cmd already includes the authInData
            // so any exceptions will include it
            TPMAuth2CommandOutput output = this.execute( authSession1.getTpmDriver() );
            
            Debug.println( "Verifiying output ..." );

            // Note: at this point, cmd already includes the authOutData
            // so any exceptions will include it

            // Also note that if we reach here, we know there was a zero return code
            
            String errMsg = "Mismatched HMACs for: ";
            
            boolean auth1Matched = authSession1.verifyAuthOutData(
                this, output, output.getAuthOutData1() );
            
            // FIXME: For some reason, this works on the TPM 1.2 chips, but
            // not on the 1.1.  In both cases, the TPM succeeds,
            // but in the TPM 1.1 case, the software reports a mismatch
            boolean auth2Matched = authSession2.verifyAuthOutData(
                this, output, output.getAuthOutData2() );
            
            if ( !continueAuthSession1 )
            {
                authSession1.setActive( false );
                // TODO: Terminate handle? (I think the TPM automatically
                // terminates a handle if it sees continueAuthSession
                // false.
            }
            
            // Note: verifyAuthOutData saves the nonceEven as authLastNonceEven
            
            // NOTE: I changed this condition to && instead of || since
            // I had a problem with Infineon's TPM1.1b and TPM_ChangeAuth.
            // There, the first HMAC would match, but the second wouldn't.
            // I noticed that IBM's tpm-3.0.3 code only checked the first one.
            
            if ( !auth1Matched && !auth2Matched )
            {
                throw new TPMAuthOutDataMismatchException( this, output, errMsg );
                // TODO: Terminate handle?
            }
            else
            {
                return output;
            }
        
    }

    @Override
    protected void writeHeaderAndBody( byte[] dest, int offset, Object... bodyFields )
    {
        super.writeHeaderAndBody( dest, offset, bodyFields );

        // the following is already done in super
//        this.writeBytes( dest, offset + this.getAuthData1Offset(), this.getAuthInData1() );
        ByteArrayUtil.writeObjectsBE( dest, offset + this.getAuthData2Offset(), this.getAuthInData2() );
    }
    
    
}
