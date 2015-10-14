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
import edu.mit.csail.tpmj.structs.TPM_TRANSPORT_PUBLIC;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;

public class TPM_EstablishTransport extends TPMKeyAuth1Command
{
    public static final int TRANSPUBLIC_OFFSET = 14;

    private TPM_TRANSPORT_PUBLIC transPublic;
    private byte[] secret;

    
    public TPM_EstablishTransport()
    {
        super( TPMConsts.TPM_ORD_EstablishTransport, TPMConsts.TPM_KH_TRANSPORT );
        // needed for instantiation of new instance before calling fromBytes
    }
    
    public TPM_EstablishTransport( int encHandle,
        TPM_TRANSPORT_PUBLIC transPublic, byte[] secret )
    {
        super( TPMConsts.TPM_ORD_EstablishTransport, encHandle );
        this.setTransPublic( transPublic );
        this.setSecret( secret );
        // TODO: this is inefficient
        int dataParamSize = TRANSPUBLIC_OFFSET + transPublic.toBytes().length
            + 4 + this.getSecretSize();
        this.setParamSize( this.computeParamSize( dataParamSize ) );
    }

    public byte[] getSecret()
    {
        return secret;
    }

    public int getSecretSize()
    {
        if ( this.secret == null )
        {
            return 0;
        }
        else
        {
            return this.secret.length;
        }
    }

    public void setSecret( byte[] secret )
    {
        if ( secret == null )
        {
            secret = new byte[0];
        }

        this.secret = secret;
    }

    public TPM_TRANSPORT_PUBLIC getTransPublic()
    {
        return transPublic;
    }

    public void setTransPublic( TPM_TRANSPORT_PUBLIC transPublic )
    {
        this.transPublic = transPublic;
    }

    
    
    @Override
    public void computeEncryptedPasswords( TPMAuthorizationSession authSession )
    {
        // no passwords
    }

    @Override
    public Object[] getInParamsForAuthDigest( )
    {
        Object[] inParams =
            { this.transPublic, this.getSecretSize(), this.secret };
        return inParams;
    }

    @Override
    public Class getReturnType()
    {
        return TPM_EstablishTransportOutput.class;
    }

    @Override
    public TPM_EstablishTransportOutput execute( TPMDriver tpmDriver )
        throws TPMException
    {
        return (TPM_EstablishTransportOutput) super.execute( tpmDriver );
    }

    /**
     * Overrides to restrict return type
     */
    @Override
    public TPM_EstablishTransportOutput execute(
        TPMAuthorizationSession authSession1, boolean continueAuthSession1 )
        throws TPMException
    {
        return (TPM_EstablishTransportOutput) super.execute( authSession1,
            continueAuthSession1 );
    }

    @Override
    public byte[] toBytes()
    {
        return this.createHeaderAndBody( this.getKeyHandle(), this.transPublic,
            this.getSecretSize(), this.secret );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.readHeader( source, offset );
        ByteArrayReadWriter brw = this.createBodyReadWriter( source, offset );

        this.setKeyHandle( brw.readInt32() );
        this.transPublic = new TPM_TRANSPORT_PUBLIC();
        brw.readStruct( this.transPublic );
        int secretSize = brw.readInt32();
        this.setSecret( brw.readBytes( secretSize ) );

        this.readAuthData( source, offset );
    }

}
