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
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public abstract class TPMAuth1Command extends TPMCommand
{
    protected TPMAuthInData authInData1;

    /**
     * Creates a TPMCommand with a 
     * and tag TPMConsts.TPM_TAG_RQU_AUTH1_COMMAND
     * and the given ordinal.
     */
    public TPMAuth1Command( int ordinal )
    {
        super( TPMConsts.TPM_TAG_RQU_AUTH1_COMMAND, ordinal );
    }

    /**
     * Call this right after construction in cases where the key allows for no authorization.
     * This sets the tag to TPMConsts.TPM_TAG_RQU_COMMAND and does not add
     * the auth data at the end of the command.  If the paramSize
     * has already been set (i.e., is non-zero), then it subtracts TPMAuthInData.STRUCT_SIZE from
     * params.
     */
    public void setNoAuth()
    {
        this.setTag( TPMConsts.TPM_TAG_RQU_COMMAND );

        // FIXME: Think about how to do this without having to set the paramSize here.
        int oldParamSize = this.getParamSize();
        if ( oldParamSize != 0 )
        {
            this.setParamSize( this.getParamSize() - TPMAuthInData.STRUCT_SIZE );
        }

    }

    public boolean isNoAuth()
    {
        return (this.getTag() == TPMConsts.TPM_TAG_RQU_COMMAND);
    }

    public boolean isAuth1()
    {
        return (this.getTag() == TPMConsts.TPM_TAG_RQU_AUTH1_COMMAND);
    }

    /**
     * Computes (but doesn't set) the correct total size,
     * given the size of the input parameters <b>excluding</b>
     * the auth parameters. (In this case, it adds TPMAuthInData.STRUCT_SIZE.)
     * 
     * @param dataParamSize -- size of input parameters, including tag, but not including authentication structs
     */
    protected int computeParamSize( int dataParamSize )
    {
        if ( this.isNoAuth() )
        {
            return dataParamSize;
        }
        else
        {
            // NOTE: We're not checking for other values of the tag here.
            return dataParamSize + TPMAuthInData.STRUCT_SIZE;
        }
    }

    protected byte[] concatInParams( Object... inParams )
    {
        return ByteArrayUtil.buildBuf( inParams );
    }

    /**
     * This is called in computeAuthInData1 to allow any passwords
     * to be encrypted according to the nonces and shared secret in
     * the authSession.  (When calling this alone, this should be called after
     * setting the nonceOdd on the session since some commands use
     * the nonceOdd to encrypt passwords.)
     * 
     * @param authSession
     */
    public abstract void computeEncryptedPasswords(
        TPMAuthorizationSession authSession );

    /**
     * This should return an array of Object
     * listing all the input params to
     * be included in inParamDigest, as specific by the spec
     * (marked by S in the HMAC column of the input parameters),
     * <b>not including</b> for the the ordinal.
     * Note that thanks to Java 5.0's autoboxing,
     * you can (and should) include primitive types in the array.
     * in the same way that they can be included in the varargs
     * list as usually done when writing toBytes.
     * <p>
     * Note: the ordinal is not included here to keep the definition
     * of this method symmetric with 
     * TPMAuth1CommandOutput.getOutParamsForAuthDigest() which
     * does not include the return code and the ordinal.
     * <p>
     * Note: Override this method to encrypt any encAuthData fields 
     * before returning them here.  (Note: that they must also
     * be serialized in encrypted form separately in toBytes.)
     * 
     * @return
     */
    public abstract Object[] getInParamsForAuthDigest();

    protected byte[] computeInParamDigest()
    {
        Debug.println( "in computeInParamDigest:" );

        byte[] ordinalBytes = ByteArrayUtil.toBytesInt32BE( this.getOrdinal() );
        Object[] inParams = this.getInParamsForAuthDigest();

        if ( Debug.isDebugOn() )
        {
            Debug.println( "ordinal: ", ordinalBytes );
            Debug.println( "inParams: " );
            for ( Object o : inParams )
            {
                Debug.println( o.getClass().getSimpleName(), o );
            }
        }

        byte[] inParamsBytes = ByteArrayUtil.concatObjectsBE( inParams );
        byte[] text = ByteArrayUtil.concat( ordinalBytes, inParamsBytes );

        Debug.println( "concat: ", text );

        return CryptoUtil.computeSHA1Hash( text );
    }

    public void computeAuthInData1( TPMAuthorizationSession authSession,
        TPM_NONCE newNonceOdd, boolean continueAuthSession )
    {
        Debug.println( "in TPMAuth1Command.computeAuthInData1:" );

        authSession.setNonceOdd( newNonceOdd );

        // NOTE: password computation may use nonceOdd, so it's important
        // to set it above.
        this.computeEncryptedPasswords( authSession );

        byte[] inParamDigest = this.computeInParamDigest();

        // Note that computeInAuthSetupParams uses nonceOdd,
        // so it's important to set it above.
        byte[] inAuthSetupParams = authSession.computeInAuthSetupParams( continueAuthSession );

        Debug.println( "inParamDigest: ", inParamDigest );
        Debug.println( "inAuthSetupParams: ", inAuthSetupParams );

        //        // NOTE: if secret is null, use all-zeros secret
        //        TPM_SECRET secret = this.sharedSecret;
        //        if ( secret == null )
        //        {
        //            secret = TPM_SECRET.NULL; // 
        //        }

        TPM_DIGEST inAuth = CryptoUtil.computeHMAC_TPM_DIGEST(
            authSession.getSharedSecret(), inParamDigest, inAuthSetupParams );

        TPMAuthInData authInData = new TPMAuthInData(
            authSession.getAuthHandle(), authSession.getNonceOdd(),
            continueAuthSession, inAuth );

        this.setAuthInData1( authInData );
    }

    public TPMAuthInData getAuthInData1()
    {
        return authInData1;
    }

    public void setAuthInData1( TPMAuthInData authInData1 )
    {
        this.authInData1 = authInData1;
    }

    @Override
    /**
     * Overrides superclass to restrict return type to TPMAuth1CommandOutput.
     * Subclasses of this class must make sure that their own return
     * types are subclasses of TPMAuth1CommandOutput.
     */
    public TPMAuth1CommandOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPMAuth1CommandOutput) super.execute( tpmDriver );
    }

    /**
     * Executes this command using the given authorization session.
     * Subclasses of this class must override this method and
     * make sure that their own return
     * types are subclasses of TPMAuth1CommandOutput.
     */
    public TPMAuth1CommandOutput execute( TPMAuthorizationSession authSession,
        boolean continueAuthSession ) throws TPMException
    {
        //        return (TPMAuth1CommandOutput) TPMAuthorizationSession.executeAuth1Cmd(
        //            this, authSession, continueAuthSession );

        if ( this.isNoAuth() )
        {
            Debug.println( "Got noAuth command.  Executing without using session ..." );
            return this.execute( authSession.getTpmDriver() );
        }

        TPM_NONCE newNonceOdd = CryptoUtil.generateRandomNonce();

        // NOTE: computeAuthInData calls computeEncryptedPasswords
        this.computeAuthInData1( authSession, newNonceOdd, continueAuthSession );
        // Note: computeAuthInData1 saves new nonceOdd in the authSession

        Debug.println( "generated AuthInData1: ", this.getAuthInData1() );

        Debug.println( "Executing cmd: ", this );

        // Note: at this point, cmd already includes the authInData
        // so any exceptions will include it
        TPMAuth1CommandOutput output = this.execute( authSession.getTpmDriver() );

        Debug.println( "Verifiying output ..." );

        // TODO: Think about moving verification to this class instead of authSession

        boolean ok = authSession.verifyAuthOutData( this, output,
            output.getAuthOutData1() );

        // Note: at this point, cmd already includes the authOutData
        // so any exceptions will include it

        // Note: verifyAuthOutData saves the nonceEven as authLastNonceEven
        if ( !ok )
        {
            throw new TPMAuthOutDataMismatchException( this, output );
            // TODO: Terminate handle?
        }
        else
        {
            if ( !continueAuthSession )
            {
                authSession.setActive( false );
                // TODO: Terminate handle? (I think the TPM automatically
                // terminates a handle if it sees continueAuthSession
                // false.
            }
            return output;
        }
    }

    //    // Commenting out.  This is not necessary 
    //    /**
    //     * Called in execute( TPMAuthorizationSession ) to prepare the 
    //     * AuthInData.  This can be overridden in subclasses to do
    //     * things such as the generation of encAuthData.
    //     * Default implementation does nothing if this.isNoAuth().
    //     * <p>
    //     * Note: leaving this private for now since it is not necessary
    //     * to override this just to compute encAuthData.
    //     * That can be done in getInParamsForDigest.
    //     * 
    //     * @param authSession
    //     * @param continueAuthSession
    //     * @throws TPMAuthSessionStateException
    //     */
    //    private void prepareAuthInData( TPMAuthorizationSession authSession,
    //        boolean continueAuthSession ) throws TPMAuthSessionStateException
    //    {
    //        if ( this.isNoAuth() )
    //        {
    //            return;
    //        }
    //
    //        if ( !authSession.isActive() )
    //        {
    //            throw new TPMAuthSessionStateException( this,
    //                "Attempt to execute command in inactive authorization session." );
    //        }
    //
    //        // Note: generateAuthInData generates and saves a new nonceOdd
    //        TPMAuthInData authInData = authSession.generateAuthInData( this,
    //            continueAuthSession );
    //
    //        Debug.println( "generatedAuthInData: " + authInData );
    //
    //        this.setAuthInData1( authInData );
    //    }
    //
    //    /**
    //     * Verifies the AuthOutData.  Default implementation is to call
    //     * authSession.verifyAuthOutData, but this can be overridden in
    //     * subclasses to perform extra processing, if necessary.
    //     * Always returns true if this.isNoAuth().
    //     * <p>
    //     * Note: Leaving this private for now since it doesn't
    //     * seem necessary to override this.
    //     * 
    //     * @param authSession
    //     * @param output
    //     * @return
    //     */
    //    protected boolean verifyAuthOutData( TPMAuthorizationSession authSession,
    //        TPMAuth1CommandOutput output )
    //    {
    //        if ( this.isNoAuth() )
    //        {
    //            return true;
    //        }
    //        else
    //        {
    //            return authSession.verifyAuthOutData( this.getOrdinal(), output,
    //                output.getAuthOutData1() );
    //        }
    //    }

    protected int getAuthData1Offset()
    {
        if ( this.isNoAuth() )
        {
            return this.getParamSize();
        }
        else
        {
            // NOTE: We're not checking for other values of the tag here.
            return this.getParamSize() - TPMAuthInData.STRUCT_SIZE;
        }
    }

    /**
     * Given source and the offset <b>of the start of the command
     * structure</b>, automatically computes the offset of the
     * authorization data (by subtracting from paramSize)
     * and reads the auth data from there.
     * 
     * @param source -- byte[] to read from
     * @param headerOffset -- offset in source of the start of the command
     */
    protected void readAuthData( byte[] source, int headerOffset )
    {
        if ( !this.isNoAuth() )
        {
            this.authInData1 = new TPMAuthInData();
            ByteArrayUtil.readStruct( source, this.getAuthData1Offset(),
                this.authInData1 );
        }
    }

    @Override
    /**
     * If not isNoAuth(), adds this.getAuthInData1() into offset this.getAuthData1Offset.
     */
    protected void writeHeaderAndBody( byte[] dest, int offset,
        Object... bodyFields )
    {
        super.writeHeaderAndBody( dest, offset, bodyFields );
        if ( !this.isNoAuth() )
        {
            ByteArrayUtil.writeObjectsBE( dest, offset
                + this.getAuthData1Offset(), this.getAuthInData1() );
        }
    }
}
