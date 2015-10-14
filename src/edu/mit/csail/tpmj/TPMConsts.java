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
package edu.mit.csail.tpmj;

import java.math.BigInteger;

public interface TPMConsts
{
    // Note: these constants were taken from tpm.h of tpm-3.0.3 by IBM

    // Note: some of the comments here are taken or adapted from 
    // the TPM 1.2 specifications.
    // http://www.trustedcomputinggroup.org/specs/TPM

    public static final int ERR_MASK = 0x80000000; // mask to define error state 

    public static final int ERR_BAD_RESP = 0x80001000; // response from TPM not formatted correctly 
    public static final int ERR_HMAC_FAIL = 0x80001001; // HMAC authorization verification failed 
    public static final int ERR_NULL_ARG = 0x80001002; // An argument was NULL that shouldn't be 
    public static final int ERR_BAD_ARG = 0x80001003; // An argument had an invalid value 
    public static final int ERR_CRYPT_ERR = 0x80001004; // An error occurred in an OpenSSL library call 
    public static final int ERR_IO = 0x80001005; // An I/O Error occured 
    public static final int ERR_MEM_ERR = 0x80001006; // A memory allocation error occurred 

    // TPM properties
    public static final int TPM_MAX_BUFF_SIZE = 4096; // from tpm-3.0.3 by IBM
    //    public static final int TPM_HASH_SIZE = 20;
    //    public static final int TPM_NONCE_SIZE = 20;
    public static final int TPM_RSA_KEY_SIZE_BITS = 2048;

    // Crypto-related constants

    /**
     * From Design Principles Spec 1.2, rev 85, line 639, p. 19: 
     * <p>
     * 4. The RSA public exponent MUST be e, where e = 2^16 + 1.
     */
    public static final int TPMDefaultPublicExponent = 65537;
    public static final BigInteger TPMDefaultPublicExponentBigInt = new BigInteger(
        "65537" );

    // Command Tags
    // from TPM Main part 2 TPM Structures Spec. version 1.2, Sect. 6 (pg. 45)
    public static final short TPM_TAG_RQU_COMMAND = 0x00C1; // A command with no authentication.
    public static final short TPM_TAG_RQU_AUTH1_COMMAND = 0x00C2; // An authenticated command with one authentication handle
    public static final short TPM_TAG_RQU_AUTH2_COMMAND = 0x00C3; // An authenticated command with two authentication handles
    public static final short TPM_TAG_RSP_COMMAND = 0x00C4; // A response from a command with no authentication
    public static final short TPM_TAG_RSP_AUTH1_COMMAND = 0x00C5; // An authenticated response with one authentication handle
    public static final short TPM_TAG_RSP_AUTH2_COMMAND = 0x00C6; // An authenticated response with two authentication handles        

    // Ordinals
    public static final int TPM_ORD_ActivateIdentity = 0x0000007A;
    public static final int TPM_ORD_AuthorizeMigrationKey = 0x0000002B;
    public static final int TPM_ORD_CertifyKey = 0x00000032;
    public static final int TPM_ORD_CertifyKey2 = 0x00000033;
    public static final int TPM_ORD_CertifySelfTest = 0x00000052;
    public static final int TPM_ORD_ChangeAuth = 0x0000000C;
    public static final int TPM_ORD_ChangeAuthAsymFinish = 0x0000000F;
    public static final int TPM_ORD_ChangeAuthAsymStart = 0x0000000E;
    public static final int TPM_ORD_ChangeAuthOwner = 0x00000010;
    public static final int TPM_ORD_CMK_ApproveMA = 0x0000001D;
    public static final int TPM_ORD_CMK_ConvertMigration = 0x00000024;
    public static final int TPM_ORD_CMK_CreateBlob = 0x0000001B;
    public static final int TPM_ORD_CMK_CreateKey = 0x00000013;
    public static final int TPM_ORD_CMK_CreateTicket = 0x00000012;
    public static final int TPM_ORD_CMK_SetRestrictions = 0x0000001C;
    public static final int TPM_ORD_ContinueSelfTest = 0x00000053;
    public static final int TPM_ORD_ConvertMigrationBlob = 0x0000002A;
    public static final int TPM_ORD_CreateCounter = 0x000000DC;
    public static final int TPM_ORD_CreateEndorsementKeyPair = 0x00000078;
    public static final int TPM_ORD_CreateMaintenanceArchive = 0x0000002C;
    public static final int TPM_ORD_CreateMigrationBlob = 0x00000028;
    public static final int TPM_ORD_CreateRevocableEK = 0x0000007F;
    public static final int TPM_ORD_CreateWrapKey = 0x0000001F;
    public static final int TPM_ORD_DAA_JOIN = 0x00000029;
    public static final int TPM_ORD_DAA_SIGN = 0x00000031;
    public static final int TPM_ORD_Delegate_CreateKeyDelegation = 0x000000D4;
    public static final int TPM_ORD_Delegate_CreateOwnerDelegation = 0x000000D5;
    public static final int TPM_ORD_Delegate_LoadOwnerDelegation = 0x000000D8;
    public static final int TPM_ORD_Delegate_Manage = 0x000000D2;
    public static final int TPM_ORD_Delegate_ReadTable = 0x000000DB;
    public static final int TPM_ORD_Delegate_UpdateVerification = 0x000000D1;
    public static final int TPM_ORD_Delegate_VerifyDelegation = 0x000000D6;
    public static final int TPM_ORD_DirRead = 0x0000001A;
    public static final int TPM_ORD_DirWriteAuth = 0x00000019;
    public static final int TPM_ORD_DisableForceClear = 0x0000005E;
    public static final int TPM_ORD_DisableOwnerClear = 0x0000005C;
    public static final int TPM_ORD_DisablePubekRead = 0x0000007E;
    public static final int TPM_ORD_DSAP = 0x00000011;
    public static final int TPM_ORD_EstablishTransport = 0x000000E6;
    public static final int TPM_ORD_EvictKey = 0x00000022;
    public static final int TPM_ORD_ExecuteTransport = 0x000000E7;
    public static final int TPM_ORD_Extend = 0x00000014;
    public static final int TPM_ORD_FieldUpgrade = 0x000000AA;
    public static final int TPM_ORD_FlushSpecific = 0x000000BA;
    public static final int TPM_ORD_ForceClear = 0x0000005D;
    public static final int TPM_ORD_GetAuditDigest = 0x00000085;
    public static final int TPM_ORD_GetAuditDigestSigned = 0x00000086;
    public static final int TPM_ORD_GetAuditEvent = 0x00000082;
    public static final int TPM_ORD_GetAuditEventSigned = 0x00000083;
    public static final int TPM_ORD_GetCapability = 0x00000065;
    public static final int TPM_ORD_GetCapabilityOwner = 0x00000066;
    public static final int TPM_ORD_GetCapabilitySigned = 0x00000064;
    public static final int TPM_ORD_GetOrdinalAuditStatus = 0x0000008C;
    public static final int TPM_ORD_GetPubKey = 0x00000021;
    public static final int TPM_ORD_GetRandom = 0x00000046;
    public static final int TPM_ORD_GetTestResult = 0x00000054;
    public static final int TPM_ORD_GetTicks = 0x000000F1;
    public static final int TPM_ORD_IncrementCounter = 0x000000DD;
    public static final int TPM_ORD_Init = 0x00000097;
    public static final int TPM_ORD_KeyControlOwner = 0x00000023;
    public static final int TPM_ORD_KillMaintenanceFeature = 0x0000002E;
    public static final int TPM_ORD_LoadAuthContext = 0x000000B7;
    public static final int TPM_ORD_LoadContext = 0x000000B9;
    public static final int TPM_ORD_LoadKey = 0x00000020;
    public static final int TPM_ORD_LoadKey2 = 0x00000041;
    public static final int TPM_ORD_LoadKeyContext = 0x000000B5;
    public static final int TPM_ORD_LoadMaintenanceArchive = 0x0000002D;
    public static final int TPM_ORD_LoadManuMaintPub = 0x0000002F;
    public static final int TPM_ORD_MakeIdentity = 0x00000079;
    public static final int TPM_ORD_MigrateKey = 0x00000025;
    public static final int TPM_ORD_NV_DefineSpace = 0x000000CC;
    public static final int TPM_ORD_NV_ReadValue = 0x000000CF;
    public static final int TPM_ORD_NV_ReadValueAuth = 0x000000D0;
    public static final int TPM_ORD_NV_WriteValue = 0x000000CD;
    public static final int TPM_ORD_NV_WriteValueAuth = 0x000000CE;
    public static final int TPM_ORD_OIAP = 0x0000000A;
    public static final int TPM_ORD_OSAP = 0x0000000B;
    public static final int TPM_ORD_OwnerClear = 0x0000005B;
    public static final int TPM_ORD_OwnerReadInternalPub = 0x00000081;
    public static final int TPM_ORD_OwnerReadPubek = 0x0000007D;
    public static final int TPM_ORD_OwnerSetDisable = 0x0000006E;
    public static final int TPM_ORD_PCR_Reset = 0x000000C8;
    public static final int TPM_ORD_PcrRead = 0x00000015;
    public static final int TPM_ORD_PhysicalDisable = 0x00000070;
    public static final int TPM_ORD_PhysicalEnable = 0x0000006F;
    public static final int TPM_ORD_PhysicalSetDeactivated = 0x00000072;
    public static final int TPM_ORD_Quote = 0x00000016;
    public static final int TPM_ORD_Quote2 = 0x0000003E;
    public static final int TPM_ORD_ReadCounter = 0x000000DE;
    public static final int TPM_ORD_ReadManuMaintPub = 0x00000030;
    public static final int TPM_ORD_ReadPubek = 0x0000007C;
    public static final int TPM_ORD_ReleaseCounter = 0x000000DF;
    public static final int TPM_ORD_ReleaseCounterOwner = 0x000000E0;
    public static final int TPM_ORD_ReleaseTransportSigned = 0x000000E8;
    public static final int TPM_ORD_Reset = 0x0000005A;
    public static final int TPM_ORD_ResetLockValue = 0x00000040;
    public static final int TPM_ORD_RevokeTrust = 0x00000080;
    public static final int TPM_ORD_SaveAuthContext = 0x000000B6;
    public static final int TPM_ORD_SaveContext = 0x000000B8;
    public static final int TPM_ORD_SaveKeyContext = 0x000000B4;
    public static final int TPM_ORD_SaveState = 0x00000098;
    public static final int TPM_ORD_Seal = 0x00000017;
    public static final int TPM_ORD_Sealx = 0x0000003D;
    public static final int TPM_ORD_SelfTestFull = 0x00000050;
    public static final int TPM_ORD_SetCapability = 0x0000003F;
    public static final int TPM_ORD_SetOperatorAuth = 0x00000074;
    public static final int TPM_ORD_SetOrdinalAuditStatus = 0x0000008D;
    public static final int TPM_ORD_SetOwnerInstall = 0x00000071;
    public static final int TPM_ORD_SetOwnerPointer = 0x00000075;
    public static final int TPM_ORD_SetRedirection = 0x0000009A;
    public static final int TPM_ORD_SetTempDeactivated = 0x00000073;
    public static final int TPM_ORD_SHA1Complete = 0x000000A2;
    public static final int TPM_ORD_SHA1CompleteExtend = 0x000000A3;
    public static final int TPM_ORD_SHA1Start = 0x000000A0;
    public static final int TPM_ORD_SHA1Update = 0x000000A1;
    public static final int TPM_ORD_Sign = 0x0000003C;
    public static final int TPM_ORD_Startup = 0x00000099;
    public static final int TPM_ORD_StirRandom = 0x00000047;
    public static final int TPM_ORD_TakeOwnership = 0x0000000D;
    public static final int TPM_ORD_Terminate_Handle = 0x00000096;
    public static final int TPM_ORD_TickStampBlob = 0x000000F2;
    public static final int TPM_ORD_UnBind = 0x0000001E;
    public static final int TPM_ORD_Unseal = 0x00000018;

    // TSC Ordinals
    
    public static final int TPM_PROTECTED_ORDINAL = 0x40000000;
    public static final int TSC_ORD_PhysicalPresence = 0x4000000A;
    public static final int TSC_ORD_ResetEstablishmentBit = 0x4000000B;
    
    
    // TPM 1.2 Structures Tags
    public static final short TPM_TAG_CONTEXTBLOB = 0x0001; //    TPM_CONTEXT_BLOB
    public static final short TPM_TAG_CONTEXT_SENSITIVE = 0x0002; //    TPM_CONTEXT_SENSITIVE
    public static final short TPM_TAG_CONTEXTPOINTER = 0x0003; //    TPM_CONTEXT_POINTER
    public static final short TPM_TAG_CONTEXTLIST = 0x0004; //    TPM_CONTEXT_LIST
    public static final short TPM_TAG_SIGNINFO = 0x0005; //    TPM_SIGN_INFO
    public static final short TPM_TAG_PCR_INFO_LONG = 0x0006; //    TPM_PCR_INFO_LONG
    public static final short TPM_TAG_PERSISTENT_FLAGS = 0x0007; //    TPM_PERMANENT_FLAGS
    public static final short TPM_TAG_VOLATILE_FLAGS = 0x0008; //    TPM_VOLATILE_FLAGS
    public static final short TPM_TAG_PERSISTENT_DATA = 0x0009; //    TPM_PERSISTENT_DATA
    public static final short TPM_TAG_VOLATILE_DATA = 0x000A; //    TPM_VOLATILE_DATA
    public static final short TPM_TAG_SV_DATA = 0x000B; //    TPM_SV_DATA
    public static final short TPM_TAG_EK_BLOB = 0x000C; //    TPM_EK_BLOB
    public static final short TPM_TAG_EK_BLOB_AUTH = 0x000D; //    TPM_EK_BLOB_AUTH
    public static final short TPM_TAG_COUNTER_VALUE = 0x000E; //    TPM_COUNTER_VALUE
    public static final short TPM_TAG_TRANSPORT_INTERNAL = 0x000F; //    TPM_TRANSPORT_INTERNAL
    public static final short TPM_TAG_TRANSPORT_LOG_IN = 0x0010; //    TPM_TRANSPORT_LOG_IN
    public static final short TPM_TAG_TRANSPORT_LOG_OUT = 0x0011; //    TPM_TRANSPORT_LOG_OUT
    public static final short TPM_TAG_AUDIT_EVENT_IN = 0x0012; //    TPM_AUDIT_EVENT_IN
    public static final short TPM_TAG_AUDIT_EVENT_OUT = 0x0013; //    TPM_AUDIT_EVENT_OUT
    public static final short TPM_TAG_CURRENT_TICKS = 0x0014; //    TPM_CURRENT_TICKS
    public static final short TPM_TAG_KEY = 0x0015; //    TPM_KEY
    public static final short TPM_TAG_STORED_DATA12 = 0x0016; //    TPM_STORED_DATA12
    public static final short TPM_TAG_NV_ATTRIBUTES = 0x0017; //    TPM_NV_ATTRIBUTES
    public static final short TPM_TAG_NV_DATA_PUBLIC = 0x0018; //    TPM_NV_DATA_PUBLIC
    public static final short TPM_TAG_NV_DATA_SENSITIVE = 0x0019; //    TPM_NV_DATA_SENSITIVE
    public static final short TPM_TAG_DELEGATIONS = 0x001A; //    TPM_DELEGATIONS
    public static final short TPM_TAG_DELEGATE_PUBLIC = 0x001B; //    TPM_DELEGATE_PUBLIC
    public static final short TPM_TAG_DELEGATE_TABLE_ROW = 0x001C; //    TPM_DELEGATE_TABLE_ROW
    public static final short TPM_TAG_TRANSPORT_AUTH = 0x001D; //    TPM_TRANSPORT_AUTH
    public static final short TPM_TAG_TRANSPORT_PUBLIC = 0x001E; //    TPM_TRANSPORT_PUBLIC
    public static final short TPM_TAG_PERMANENT_FLAGS = 0x001F; //    TPM_PERMANENT_FLAGS
    public static final short TPM_TAG_STCLEAR_FLAGS = 0x0020; //    TPM_STCLEAR_FLAGS
    public static final short TPM_TAG_STANY_FLAGS = 0x0021; //    TPM_STANY_FLAGS
    public static final short TPM_TAG_PERMANENT_DATA = 0x0022; //    TPM_PERMANENT_DATA
    public static final short TPM_TAG_STCLEAR_DATA = 0x0023; //    TPM_STCLEAR_DATA
    public static final short TPM_TAG_STANY_DATA = 0x0024; //    TPM_STANY_DATA
    public static final short TPM_TAG_FAMILY_TABLE_ENTRY = 0x0025; //    TPM_FAMILY_TABLE_ENTRY
    public static final short TPM_TAG_DELEGATE_SENSITIVE = 0x0026; //    TPM_DELEGATE_SENSITIVE
    public static final short TPM_TAG_DELG_KEY_BLOB = 0x0027; //    TPM_DELG_KEY_BLOB
    public static final short TPM_TAG_KEY12 = 0x0028; //    TPM_KEY12
    public static final short TPM_TAG_CERTIFY_INFO2 = 0x0029; //    TPM_CERTIFY_INFO2
    public static final short TPM_TAG_DELEGATE_OWNER_BLOB = 0x002A; //    TPM_DELEGATE_OWNER_BLOB
    public static final short TPM_TAG_EK_BLOB_ACTIVATE = 0x002B; //    TPM_EK_BLOB_ACTIVATE
    public static final short TPM_TAG_DAA_BLOB = 0x002C; //    TPM_DAA_BLOB
    public static final short TPM_TAG_DAA_CONTEXT = 0x002D; //    TPM_DAA_CONTEXT
    public static final short TPM_TAG_DAA_ENFORCE = 0x002E; //    TPM_DAA_ENFORCE
    public static final short TPM_TAG_DAA_ISSUER = 0x002F; //    TPM_DAA_ISSUER
    public static final short TPM_TAG_CAP_VERSION_INFO = 0x0030; //    TPM_CAP_VERSION_INFO
    public static final short TPM_TAG_DAA_SENSITIVE = 0x0031; //    TPM_DAA_SENSITIVE
    public static final short TPM_TAG_DAA_TPM = 0x0032; //    TPM_DAA_TPM
    public static final short TPM_TAG_CMK_MIGAUTH = 0x0033; //    TPM_CMK_MIGAUTH
    public static final short TPM_TAG_CMK_SIGTICKET = 0x0034; //    TPM_CMK_SIGTICKET
    public static final short TPM_TAG_CMK_MA_APPROVAL = 0x0035; //    TPM_CMK_MA_APPROVAL
    public static final short TPM_TAG_QUOTE_INFO2 = 0x0036; //    TPM_QUOTE_INFO2

    // TPM Key Handles
    public static final int TPM_KH_SRK = 0x40000000; // The handle points to the SRK    
    public static final int TPM_KH_OWNER = 0x40000001; // The handle points to the TPM Owner   
    public static final int TPM_KH_REVOKE = 0x40000002; // The handle points to the RevokeTrust value   
    public static final int TPM_KH_TRANSPORT = 0x40000003; // The handle points to the EstablishTransport static authorization  
    public static final int TPM_KH_OPERATOR = 0x40000004; // The handle points to the Operator auth   
    public static final int TPM_KH_ADMIN = 0x40000005; // The handle points to the delegation administration auth  
    public static final int TPM_KH_EK = 0x40000006; // The handle points to the PUBEK, only usable with TPM_OwnerReadInternalPub

    // TPM Key Usage
    /**
     * This SHALL indicate a signing key. The [private] key SHALL be used for
     * signing operations, only. This means that it MUST be a leaf of the
     * Protected Storage key hierarchy.
     */
    public static final short TPM_KEY_SIGNING = 0x0010;
    /**
     * This SHALL indicate a storage key. The key SHALL be used to wrap and
     * unwrap other keys in the Protected Storage hierarchy
     */
    public static final short TPM_KEY_STORAGE = 0x0011;
    /**
     * This SHALL indicate an identity key. The key SHALL be used for operations
     * that require a TPM identity, only.
     */
    public static final short TPM_KEY_IDENTITY = 0x0012;
    /**
     * This SHALL indicate an ephemeral key that is in use during the
     * ChangeAuthAsym process, only.
     */
    public static final short TPM_KEY_AUTHCHANGE = 0X0013;
    /**
     * This SHALL indicate a key that can be used for TPM_Bind and TPM_Unbind
     * operations only.
     */
    public static final short TPM_KEY_BIND = 0x0014;
    /**
     * This SHALL indicate a key that can perform signing and binding
     * operations. The key MAY be used for both signing and binding operations.
     * The TPM_KEY_LEGACY key type is to allow for use by applications where
     * both signing and encryption operations occur with the same key. The use
     * of this key type is not recommended
     */
    public static final short TPM_KEY_LEGACY = 0x0015;
    /** This SHALL indicate a key in use for TPM_MigrateKey */
    public static final short TPM_KEY_MIGRATE = 0x0016;

    // TPM_AUTH_DATA_USAGE values
    /**
     * This SHALL indicate that usage of the key without authorization is permitted.
     */
    public static final byte TPM_AUTH_NEVER = 0x00;
    /**
     * This SHALL indicate that on each usage of the key the authorization MUST be performed.
     */
    public static final byte TPM_AUTH_ALWAYS = 0x01;
    /**
     * This SHALL indicate that on commands that require the TPM to use the private portion of the key, the authorization MUST be performed. For commands that cause the TPM to read the public portion of the key,    but not to use the private portion (e.g. TPM_GetPubKey), the authorization may be omitted.
     */
    public static final byte TPM_AUTH_PRIV_USE_ONLY = 0x03;

    // TPM_KEY_FLAGS mask values

    public static class TPM_KEY_FLAGS_MASK
    {
        /**
         * Zero.
         */
        public static final int base = 0x00000000;
        /**
         *  This mask value SHALL indicate the use of redirected output.
         */
        public static final int redirection = 0x00000001;
        /**
         *  This mask value SHALL indicate that the key is migratable.
         */
        public static final int migratable = 0x00000002;
        /**
         * This mask value SHALL indicate that the key MUST be unloaded upon execution of the 
         * TPM_Startup(ST_Clear). This does not indicate that a nonvolatile key will remain loaded across
         * TPM_Startup(ST_Clear) events.
         */
        public static final int isVolatile = 0x00000004;
        /**
         * When TRUE the TPM MUST NOT check digestAtRelease or localityAtRelease for commands that use the
         * public portion of the key like TPM_GetPubKey.
         * When FALSE the TPM MUST check digestAtRelease and localityAtRelease for commands that use the public
         * portion of the key.
         */
        public static final int pcrIgnoredOnRead = 0x00000008;
        /**
         * When set indicates that the key is under control of a migration authority. 
         * The TPM MUST only allow the creation of a key with this flag in TPM_CMK_CreateKey
         */
        public static final int migrateAuthority = 0x00000010;
    }

    // TPM_ALGORITHM_ID values

    public static final int TPM_ALG_RSA = 0x00000001; // The RSA algorithm.            
    public static final int TPM_ALG_DES = 0x00000002; // The DES algorithm            
    public static final int TPM_ALG_3DES = 0X00000003; // The 3DES algorithm in EDE mode         
    public static final int TPM_ALG_SHA = 0x00000004; // The SHA1 algorithm            
    public static final int TPM_ALG_HMAC = 0x00000005; // The RFC 2104 HMAC algorithm          
    public static final int TPM_ALG_AES128 = 0x00000006; // The AES algorithm, key size 128         
    public static final int TPM_ALG_MGF1 = 0x00000007; // The XOR algorithm using MGF1 to create a string the size of the encrypted block
    public static final int TPM_ALG_AES192 = 0x00000008; // AES, key size 192           
    public static final int TPM_ALG_AES256 = 0x00000009; // AES, key size 256           
    public static final int TPM_ALG_XOR = 0x0000000A; // XOR using the rolling nonces          

    // TPM_ENC_SCHEME encryption scheme values
    public static final short TPM_ES_NONE = 0x0001;
    public static final short TPM_ES_RSAESPKCSv15 = 0x0002;
    public static final short TPM_ES_RSAESOAEP_SHA1_MGF1 = 0x0003;
    public static final short TPM_ES_SYM_CNT = 0x0004;
    public static final short TPM_ES_SYM_OFB = 0x0005;

    // TPM_SIG_SCHEME signature schemes

    public static final short TPM_SS_NONE = 0x0001;
    public static final short TPM_SS_RSASSAPKCS1v15_SHA1 = 0x0002;
    public static final short TPM_SS_RSASSAPKCS1v15_DER = 0x0003;
    public static final short TPM_SS_RSASSAPKCS1v15_INFO = 0x0004;

    // TPM Payload types
    public static final byte TPM_PT_ASYM = 0x01; // The entity is an asymmetric key 
    public static final byte TPM_PT_BIND = 0x02; // The entity is bound data  
    public static final byte TPM_PT_MIGRATE = 0x03; // The entity is a migration blob 
    public static final byte TPM_PT_MAINT = 0x04; // The entity is a maintenance blob 
    public static final byte TPM_PT_SEAL = 0x05; // The entity is sealed data  
    public static final byte TPM_PT_MIGRATE_RESTRICTED = 0x06; // The entity is a restricted-migration asymmetric key
    public static final byte TPM_PT_MIGRATE_EXTERNAL = 0x07; // The entity is a external migratable key
    public static final byte TPM_PT_CMK_MIGRATE = 0x08; // The entity is a CMK migratable blob

    // TPM_PROTOCOL_ID protocol id values
    public static final short TPM_PID_OIAP = 0x0001; // The OIAP protocol.                   
    public static final short TPM_PID_OSAP = 0x0002; // The OSAP protocol.                   
    public static final short TPM_PID_ADIP = 0x0003; // The ADIP protocol.                   
    public static final short TPM_PID_ADCP = 0X0004; // The ADCP protocol.                   
    public static final short TPM_PID_OWNER = 0X0005; // The protocol for taking ownership of a TPM.
    public static final short TPM_PID_DSAP = 0x0006; //  The DSAP protocol                    
    public static final short TPM_PID_TRANSPORT = 0x0007; // The transport protocol                    

    // TPM 1.1 Capability Tags
    public static final int TCPA_CAP_VERSION = 0x00000006;

    // TPM_CAPABILITY_AREA for TPM_GetCapability (TPM 1.2)
    public static final int TPM_CAP_ORD = 0x00000001;
    public static final int TPM_CAP_ALG = 0x00000002;
    public static final int TPM_CAP_PID = 0x00000003;
    public static final int TPM_CAP_FLAG = 0x00000004;
    public static final int TPM_CAP_PROPERTY = 0x00000005;
    public static final int TPM_CAP_VERSION = 0x00000006;
    public static final int TPM_CAP_KEY_HANDLE = 0x00000007;
    public static final int TPM_CAP_CHECK_LOADED = 0x00000008;
    public static final int TPM_CAP_SYM_MODE = 0x00000009;
    public static final int TPM_CAP_KEY_STATUS = 0x0000000C;
    public static final int TPM_CAP_NV_LIST = 0x0000000D;
    public static final int TPM_CAP_MFR = 0x00000010;
    public static final int TPM_CAP_NV_INDEX = 0x00000011;
    public static final int TPM_CAP_TRANS_ALG = 0x00000012;
    public static final int TPM_CAP_HANDLE = 0x00000014;
    public static final int TPM_CAP_TRANS_ES = 0x00000015;
    public static final int TPM_CAP_AUTH_ENCRYPT = 0x00000017;
    public static final int TPM_CAP_SELECT_SIZE = 0x00000018;
    public static final int TPM_CAP_VERSION_VAL = 0x0000001A;

    // TPM_CAP_PROPERTY Subcap Values for TPM_GetCapability
    public static final int TPM_CAP_PROP_PCR = 0x00000101; //
    public static final int TPM_CAP_PROP_DIR = 0x00000102; //
    public static final int TPM_CAP_PROP_MANUFACTURER = 0x00000103; //
    public static final int TPM_CAP_PROP_KEYS = 0x00000104; //
    public static final int TPM_CAP_PROP_MIN_COUNTER = 0x00000107; //
    public static final int TPM_CAP_PROP_AUTHSESS = 0x0000010A; //
    public static final int TPM_CAP_PROP_TRANSESS = 0x0000010B; //
    public static final int TPM_CAP_PROP_COUNTERS = 0x0000010C; //
    public static final int TPM_CAP_PROP_MAX_AUTHSESS = 0x0000010D; //
    public static final int TPM_CAP_PROP_MAX_TRANSESS = 0x0000010E; //
    public static final int TPM_CAP_PROP_MAX_COUNTERS = 0x0000010F; //
    public static final int TPM_CAP_PROP_MAX_KEYS = 0x00000110; //
    public static final int TPM_CAP_PROP_OWNER = 0x00000111; //
    public static final int TPM_CAP_PROP_CONTEXT = 0x00000112; //
    public static final int TPM_CAP_PROP_MAX_CONTEXT = 0x00000113; //
    public static final int TPM_CAP_PROP_FAMILYROWS = 0x00000114; //
    public static final int TPM_CAP_PROP_TIS_TIMEOUT = 0x00000115; //
    public static final int TPM_CAP_PROP_STARTUP_EFFECT = 0x00000116; //
    public static final int TPM_CAP_PROP_DELEGATE_ROW = 0x00000117; //
    public static final int TPM_CAP_PROP_DAA_MAX = 0x00000119; //
    public static final int CAP_PROP_SESSION_DAA = 0x0000011A; //
    public static final int TPM_CAP_PROP_CONTEXT_DIST = 0x0000011B; //
    public static final int TPM_CAP_PROP_DAA_INTERRUPT = 0x0000011C; //
    public static final int TPM_CAP_PROP_SESSIONS = 0X0000011D; //
    public static final int TPM_CAP_PROP_MAX_SESSIONS = 0x0000011E; //
    public static final int TPM_CAP_PROP_CMK_RESTRICTION = 0x0000011F; //
    public static final int TPM_CAP_PROP_DURATION = 0x00000120; //
    public static final int TPM_CAP_PROP_ACTIVE_COUNTER = 0x00000122; //
    public static final int TPM_CAP_PROP_MAX_NV_AVAILABLE = 0x00000123; //
    public static final int TPM_CAP_PROP_INPUT_BUFFER = 0x00000124; //

    public static final int TPM_CAP_FLAG_PERMANENT = 0x00000108;
    public static final int TPM_CAP_FLAG_VOLATILE = 0x00000109;

    // TPM Entity types
    public static final short TPM_ET_KEYHANDLE = 0x0001;
    public static final short TPM_ET_OWNER = 0x0002;
    public static final short TPM_ET_DATA = 0x0003;
    public static final short TPM_ET_SRK = 0x0004;
    public static final short TPM_ET_KEY = 0x0005;
    public static final short TPM_ET_REVOKE = 0x0006;
    public static final short TPM_ET_DEL_OWNER_BLOB = 0x0007;
    public static final short TPM_ET_DEL_ROW = 0x0008;
    public static final short TPM_ET_DEL_KEY_BLOB = 0x0009;
    public static final short TPM_ET_COUNTER = 0x000A;
    public static final short TPM_ET_NV = 0x000B;
    public static final short TPM_ET_KEYAES = 0x000C;
    public static final short TPM_ET_KEYDES = 0x000D;
    public static final short TPM_ET_OWNERAES = 0x000E;
    public static final short TPM_ET_OWNERDES = 0x000F;
    public static final short TPM_ET_KEYXOR = 0x0010;
    public static final short TPM_ET_RESERVED_HANDLE = 0x0040;

    // TPM Resource types
    public static final int TPM_RT_KEY = 0x00000001; // The handle is a key handle and is the result of a LoadKey type operation    
    public static final int TPM_RT_AUTH = 0x00000002; // The handle is an authorization handle. Auth handles come from TPM_OIAP, TPM_OSAP and TPM_DSAP     
    public static final int TPM_RT_HASH = 0X00000003; // Reserved for hashes                
    public static final int TPM_RT_TRANS = 0x00000004; // The handle is for a transport session. Transport handles come from TPM_EstablishTransport       
    public static final int TPM_RT_CONTEXT = 0x00000005; // Resource wrapped and held outside the TPM using the context save/restore commands       
    public static final int TPM_RT_COUNTER = 0x00000006; // Reserved for counters                
    public static final int TPM_RT_DELEGATE = 0x00000007; // The handle is for a delegate row. These are the internal rows held in NV storage by the TPM
    public static final int TPM_RT_DAA_TPM = 0x00000008; // The value is a DAA TPM specific blob           
    public static final int TPM_RT_DAA_V0 = 0x00000009; // The value is a DAA V0 parameter            
    public static final int TPM_RT_DAA_V1 = 0x0000000A; // The value is a DAA V1 parameter            

    // Transport Session constants
    public static final int TPM_TRANSPORT_ENCRYPT = 0x00000001;
    public static final int TPM_TRANSPORT_LOG = 0x00000002;
    public static final int TPM_TRANSPORT_EXCLUSIVE = 0x00000004;

    // TPM_PHYSICAL_PRESENCE constants
    public static final short TPM_PHYSICAL_PRESENCE_HW_DISABLE = 0x0200; // Sets the physicalPresenceHWEnable to  FALSE
    public static final short TPM_PHYSICAL_PRESENCE_CMD_DISABLE = 0x0100; // Sets the physicalPresenceCMDEnable to  FALSE
    public static final short TPM_PHYSICAL_PRESENCE_LIFETIME_LOCK = 0x0080; // Sets the physicalPresenceLifetimeLock to  TRUE
    public static final short TPM_PHYSICAL_PRESENCE_HW_ENABLE = 0x0040; // Sets the physicalPresenceHWEnable to  TRUE
    public static final short TPM_PHYSICAL_PRESENCE_CMD_ENABLE = 0x0020; // Sets the physicalPresenceCMDEnable to  TRUE
    public static final short TPM_PHYSICAL_PRESENCE_NOTPRESENT = 0x0010; // Sets PhysicalPresence = FALSE   
    public static final short TPM_PHYSICAL_PRESENCE_PRESENT = 0x0008; // Sets PhysicalPresence = TRUE    
    public static final short TPM_PHYSICAL_PRESENCE_LOCK = 0x0004; // Sets PhysicalPresenceLock = TRUE    

    // TPM/J-specific Error Codes 
    // (NOTE: these are unused right now.  We just use TPMException with a String explanation.)
    
    // FIXME: pick more appropriate/consistent error code for I/O exceptions?
    public static final int TPMJ_TPM_IO_ERROR_RETURNCODE = -1;

    /**
     * Returned for commands which are disallowed by TPM/J itself
     * (e.g., irreversible commands such as setting the TPM Physical Presence Lifetime Lock)
     */
    public static final int TPMJ_DISALLOWED_CMD = -2;

}
