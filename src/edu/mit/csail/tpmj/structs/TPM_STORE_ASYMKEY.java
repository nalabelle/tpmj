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
package edu.mit.csail.tpmj.structs;

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;

/**
 * 10.6 TPM_STORE_ASYMKEY
 * <p>
 * The TPM_STORE_ASYMKEY structure provides the area to identify the confidential
 * information related to a key. This will include the private key factors for an asymmetric key.
 * The structure is designed so that encryption of a TPM_STORE_ASYMKEY structure
 * containing a 2048 bit RSA key can be done in one operation if the encrypting key is 2048
 * bits.
 * <p>
 * Using typical RSA notation the structure would include P, and when loading the key include
 * the unencrypted P*Q which would be used to recover the Q value.
 * To accommodate the future use of multiple prime RSA keys the specification of additional
 * prime factors is an optional capability.
 * This structure provides the basis of defining the protection of the private key.
 * Changes in this structure MUST be reflected in the TPM_MIGRATE_ASYMKEY structure
 * (section 10.8).
 * 
 * @author lfgs
 */
public class TPM_STORE_ASYMKEY extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_STORE_ASYMKEY { // pos len total
     *      TPM_PAYLOAD_TYPE payload; // 0 1 1
     *      TPM_SECRET usageAuth; // 1 20 21
     *      TPM_SECRET migrationAuth; // 21 20 41
     *      TPM_DIGEST pubDataDigest; // 41 20 61
     *      TPM_STORE_PRIVKEY privKey; // 61 132-151 193-214
     * } TPM_STORE_ASYMKEY;
     * 
     */

    private byte payload;
    private TPM_SECRET usageAuth = TPM_SECRET.NULL;
    private TPM_SECRET migrationAuth = TPM_SECRET.NULL;
    private TPM_DIGEST pubDataDigest = new TPM_DIGEST();
    private TPM_STORE_PRIVKEY privKey = new TPM_STORE_PRIVKEY();

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_STORE_ASYMKEY()
    {
        // do nothing
    }

    public TPM_STORE_ASYMKEY( byte payload, TPM_SECRET usageAuth,
        TPM_SECRET migrationAuth, TPM_DIGEST pubDataDigest,
        TPM_STORE_PRIVKEY privKey )
    {
        this.payload = payload;
        this.setUsageAuth( usageAuth );
        this.setMigrationAuth( migrationAuth );
        this.pubDataDigest = pubDataDigest;
        this.privKey = privKey;
    }

    public TPM_SECRET getMigrationAuth()
    {
        return migrationAuth;
    }

    public void setMigrationAuth( TPM_SECRET migrationAuth )
    {
        if ( migrationAuth == null )
        {
            this.migrationAuth = TPM_SECRET.NULL;
        }
        else
        {
            this.migrationAuth = migrationAuth;
        }
    }

    public byte getPayload()
    {
        return payload;
    }

    public void setPayload( byte payload )
    {
        this.payload = payload;
    }

    public TPM_STORE_PRIVKEY getPrivKey()
    {
        return privKey;
    }

    public void setPrivKey( TPM_STORE_PRIVKEY privKey )
    {
        this.privKey = privKey;
    }

    public TPM_DIGEST getPubDataDigest()
    {
        return pubDataDigest;
    }

    public void setPubDataDigest( TPM_DIGEST pubDataDigest )
    {
        this.pubDataDigest = pubDataDigest;
    }

    public TPM_SECRET getUsageAuth()
    {
        return usageAuth;
    }

    public void setUsageAuth( TPM_SECRET usageAuth )
    {
        if ( usageAuth == null )
        {
            this.usageAuth = TPM_SECRET.NULL;
        }
        else
        {
            this.usageAuth = usageAuth;
        }
    }

    @Override
    public byte[] toBytes()
    {
        /*
         * typedef struct tdTPM_STORE_ASYMKEY { // pos len total
         *      TPM_PAYLOAD_TYPE payload; // 0 1 1
         *      TPM_SECRET usageAuth; // 1 20 21
         *      TPM_SECRET migrationAuth; // 21 20 41
         *      TPM_DIGEST pubDataDigest; // 41 20 61
         *      TPM_STORE_PRIVKEY privKey; // 61 132-151 193-214
         * } TPM_STORE_ASYMKEY;
         * 
         */
        TPM_SECRET usageAuthToWrite = this.usageAuth;
        if ( usageAuthToWrite == null )
        {
            usageAuthToWrite = TPM_SECRET.NULL;
        }
        TPM_SECRET migAuthToWrite = this.migrationAuth;
        if ( migAuthToWrite == null )
        {
            migAuthToWrite = TPM_SECRET.NULL;
        }

        // FIXME: Think about actually setting the usageAuth and migrationAuth to all zeros if they are null        
        return ByteArrayUtil.buildBuf( this.payload, usageAuthToWrite,
            migAuthToWrite, this.pubDataDigest, this.privKey );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.payload = brw.readByte();
        this.usageAuth = new TPM_SECRET( brw.readBytes( TPM_SECRET.SIZE ) );
        this.migrationAuth = new TPM_SECRET( brw.readBytes( TPM_SECRET.SIZE ) );
        this.pubDataDigest = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ));
        this.privKey = new TPM_STORE_PRIVKEY();
        brw.readStruct( this.privKey );
    }

    public String toString()
    {
        return "TPM_STORE_ASMKEY: " + "payload type: " + this.payload + "\n"
            + "usageAuth: " + this.usageAuth + "\n"
            + "migrationAuth: " + this.migrationAuth + "\n"
            + "pubDataDigest: " + this.pubDataDigest + "\n"
            + "privKey: " + this.privKey + "\n";
    }
}
