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

import edu.mit.csail.tpmj.util.ByteArrayUtil;

public class TPM_RESULT extends SimpleTPMStruct
{
    private int returnCode;

    public TPM_RESULT( int returnCode )
    {
        this.returnCode = returnCode;
    }

    @Override
    public byte[] toBytes()
    {
        return ByteArrayUtil.toBytesInt32BE( returnCode );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        this.returnCode = ByteArrayUtil.readInt32BE( source, offset );
    }

    // CONSTANTS

    public static final int TPM_BASE = 0x0; // The start of TPM return codes
    public static final int TPM_SUCCESS = TPM_BASE; // Successful completion of the operation 
    public static final int TPM_NON_FATAL = 0x00000800; // Mask to indicate that the error code is a non-fatal failure.
    public static final int TPM_AUTHFAIL = TPM_BASE + 1; // Authentication failed
    public static final int TPM_BADINDEX = TPM_BASE + 2; // The index to a PCR, DIR or other register is incorrect
    public static final int TPM_BAD_PARAMETER = TPM_BASE + 3; // One or more parameter is bad
    public static final int TPM_AUDITFAILURE = TPM_BASE + 4; // An operation completed successfully but the auditing of that operation failed.
    public static final int TPM_CLEAR_DISABLED = TPM_BASE + 5; // The clear disable flag is set and all clear operations now require physical access
    public static final int TPM_DEACTIVATED = TPM_BASE + 6; // The TPM is deactivated 
    public static final int TPM_DISABLED = TPM_BASE + 7; // The TPM is disabled 
    public static final int TPM_DISABLED_CMD = TPM_BASE + 8; // The target command has been disabled
    public static final int TPM_FAIL = TPM_BASE + 9; // The operation failed
    public static final int TPM_BAD_ORDINAL = TPM_BASE + 10; // The ordinal was unknown or inconsistent
    public static final int TPM_INSTALL_DISABLED = TPM_BASE + 11; // The ability to install an owner is disabled 
    public static final int TPM_INVALID_KEYHANDLE = TPM_BASE + 12; // The key handle can not be intrepreted
    public static final int TPM_KEYNOTFOUND = TPM_BASE + 13; // The key handle points to an invalid key
    public static final int TPM_INAPPROPRIATE_ENC = TPM_BASE + 14; // Unacceptable encryption scheme 
    public static final int TPM_MIGRATEFAIL = TPM_BASE + 15; // Migration authorization failed 
    public static final int TPM_INVALID_PCR_INFO = TPM_BASE + 16; // PCR information could not be interpreted 
    public static final int TPM_NOSPACE = TPM_BASE + 17; // No room to load key.
    public static final int TPM_NOSRK = TPM_BASE + 18; // There is no SRK set
    public static final int TPM_NOTSEALED_BLOB = TPM_BASE + 19; // An encrypted blob is invalid or was not created by this TPM
    public static final int TPM_OWNER_SET = TPM_BASE + 20; // There is already an Owner
    public static final int TPM_RESOURCES = TPM_BASE + 21; // The TPM has insufficient internal resources to perform the requested action. 
    public static final int TPM_SHORTRANDOM = TPM_BASE + 22; // A random string was too short 
    public static final int TPM_SIZE = TPM_BASE + 23; // The TPM does not have the space to perform the operation. 
    public static final int TPM_WRONGPCRVAL = TPM_BASE + 24; // The named PCR value does not match the current PCR value. 
    public static final int TPM_BAD_PARAM_SIZE = TPM_BASE + 25; // The paramSize argument to the command has the incorrect value 
    public static final int TPM_SHA_THREAD = TPM_BASE + 26; // There is no existing SHA-1 thread. 
    public static final int TPM_SHA_ERROR = TPM_BASE + 27; // The calculation is unable to proceed because the existing SHA-1 thread has already encountered an error.
    public static final int TPM_FAILEDSELFTEST = TPM_BASE + 28; // Self-test has failed and the TPM has shutdown.
    public static final int TPM_AUTH2FAIL = TPM_BASE + 29; // The authorization for the second key in a 2 key function failed authorization
    public static final int TPM_BADTAG = TPM_BASE + 30; // The tag value sent to for a command is invalid 
    public static final int TPM_IOERROR = TPM_BASE + 31; // An IO error occurred transmitting information to the TPM
    public static final int TPM_ENCRYPT_ERROR = TPM_BASE + 32; // The encryption process had a problem. 
    public static final int TPM_DECRYPT_ERROR = TPM_BASE + 33; // The decryption process did not complete. 
    public static final int TPM_INVALID_AUTHHANDLE = TPM_BASE + 34; // An invalid handle was used.
    public static final int TPM_NO_ENDORSEMENT = TPM_BASE + 35; // The TPM does not a EK installed 
    public static final int TPM_INVALID_KEYUSAGE = TPM_BASE + 36; // The usage of a key is not allowed
    public static final int TPM_WRONG_ENTITYTYPE = TPM_BASE + 37; // The submitted entity type is not allowed 
    public static final int TPM_INVALID_POSTINIT = TPM_BASE + 38; // The command was received in the wrong sequence relative to TPM_Init and a subsequent TPM_Startup
    public static final int TPM_INAPPROPRIATE_SIG = TPM_BASE + 39; // Signed data cannot include additional DER information 
    public static final int TPM_BAD_KEY_PROPERTY = TPM_BASE + 40; // The key properties in TPM_KEY_PARMs are not supported by this TPM 
    public static final int TPM_BAD_MIGRATION = TPM_BASE + 41; // The migration properties of this key are incorrect.
    public static final int TPM_BAD_SCHEME = TPM_BASE + 42; // The signature or encryption scheme for this key is incorrect or not permitted in this situation.
    public static final int TPM_BAD_DATASIZE = TPM_BASE + 43; // The size of the data (or blob) parameter is bad or inconsistent with the referenced key
    public static final int TPM_BAD_MODE = TPM_BASE + 44; // A mode parameter is bad, such as capArea or subCapArea for TPM_GetCapability, phsicalPresence parameter for TPM_PhysicalPresence, or migrationType for TPM_CreateMigrationBlob.
    public static final int TPM_BAD_PRESENCE = TPM_BASE + 45; // Either the physicalPresence or physicalPresenceLock bits have the wrong value
    public static final int TPM_BAD_VERSION = TPM_BASE + 46; // The TPM cannot perform this version of the capability
    public static final int TPM_NO_WRAP_TRANSPORT = TPM_BASE + 47; // The TPM does not allow for wrapped transport sessions
    public static final int TPM_AUDITFAIL_UNSUCCESSFUL = TPM_BASE + 48; // TPM audit construction failed and the underlying command was returning a failure code also
    public static final int TPM_AUDITFAIL_SUCCESSFUL = TPM_BASE + 49; // TPM audit construction failed and the underlying command was returning success 
    public static final int TPM_NOTRESETABLE = TPM_BASE + 50; // Attempt to reset a PCR register that does not have the resettable attribute
    public static final int TPM_NOTLOCAL = TPM_BASE + 51; // Attempt to reset a PCR register that requires locality and locality modifier not part of command transport
    public static final int TPM_BAD_TYPE = TPM_BASE + 52; // Make identity blob not properly typed
    public static final int TPM_INVALID_RESOURCE = TPM_BASE + 53; // When saving context identified resource type does not match actual resource 
    public static final int TPM_NOTFIPS = TPM_BASE + 54; // The TPM is attempting to execute a command only available when in FIPS mode
    public static final int TPM_INVALID_FAMILY = TPM_BASE + 55; // The command is attempting to use an invalid family ID
    public static final int TPM_NO_NV_PERMISSION = TPM_BASE + 56; // The permission to manipulate the NV storage is not available
    public static final int TPM_REQUIRES_SIGN = TPM_BASE + 57; // The operation requires a signed command
    public static final int TPM_KEY_NOTSUPPORTED = TPM_BASE + 58; // Wrong operation to load an NV key 
    public static final int TPM_AUTH_CONFLICT = TPM_BASE + 59; // NV_LoadKey blob requires both owner and blob authorization 
    public static final int TPM_AREA_LOCKED = TPM_BASE + 60; // The NV area is locked and not writtable 
    public static final int TPM_BAD_LOCALITY = TPM_BASE + 61; // The locality is incorrect for the attempted operation 
    public static final int TPM_READ_ONLY = TPM_BASE + 62; // The NV area is read only and can’t be written to
    public static final int TPM_PER_NOWRITE = TPM_BASE + 63; // There is no protection on the write to the NV area
    public static final int TPM_FAMILYCOUNT = TPM_BASE + 64; // The family count value does not match
    public static final int TPM_WRITE_LOCKED = TPM_BASE + 65; // The NV area has already been written to 
    public static final int TPM_BAD_ATTRIBUTES = TPM_BASE + 66; // The NV area attributes conflict 
    public static final int TPM_INVALID_STRUCTURE = TPM_BASE + 67; // The structure tag and version are invalid or inconsistent 
    public static final int TPM_KEY_OWNER_CONTROL = TPM_BASE + 68; // The key is under control of the TPM Owner and can only be evicted by the TPM Owner.
    public static final int TPM_BAD_COUNTER = TPM_BASE + 69; // The counter handle is incorrect 
    public static final int TPM_NOT_FULLWRITE = TPM_BASE + 70; // The write is not a complete write of the area
    public static final int TPM_CONTEXT_GAP = TPM_BASE + 71; // The gap between saved context counts is too large 
    public static final int TPM_MAXNVWRITES = TPM_BASE + 72; // The maximum number of NV writes without an owner has been exceeded 
    public static final int TPM_NOOPERATOR = TPM_BASE + 73; // No operator AuthData value is set
    public static final int TPM_RESOURCEMISSING = TPM_BASE + 74; // The resource pointed to by context is not loaded 
    public static final int TPM_DELEGATE_LOCK = TPM_BASE + 75; // The delegate administration is locked 
    public static final int TPM_DELEGATE_FAMILY = TPM_BASE + 76; // Attempt to manage a family other then the delegated family
    public static final int TPM_DELEGATE_ADMIN = TPM_BASE + 77; // Delegation table management not enabled 
    public static final int TPM_TRANSPORT_NOTEXCLUSIVE = TPM_BASE + 78; // There was a command executed outside of an exclusive transport session
    public static final int TPM_OWNER_CONTROL = TPM_BASE + 79; // Attempt to context save a owner evict controlled key 
    public static final int TPM_DAA_RESOURCES = TPM_BASE + 80; // The DAA command has no resources availble to execute the command
    public static final int TPM_DAA_INPUT_DATA0 = TPM_BASE + 81; // The consistency check on DAA parameter inputData0 has failed. 
    public static final int TPM_DAA_INPUT_DATA1 = TPM_BASE + 82; // The consistency check on DAA parameter inputData1 has failed. 
    public static final int TPM_DAA_ISSUER_SETTINGS = TPM_BASE + 83; // The consistency check on DAA_issuerSettings has failed.
    public static final int TPM_DAA_TPM_SETTINGS = TPM_BASE + 84; // The consistency check on DAA_tpmSpecific has failed.
    public static final int TPM_DAA_STAGE = TPM_BASE + 85; // The atomic process indicated by the submitted DAA command is not the expected process.
    public static final int TPM_DAA_ISSUER_VALIDITY = TPM_BASE + 86; // The issuer’s validity check has detected an inconsistency 
    public static final int TPM_DAA_WRONG_W = TPM_BASE + 87; // The consistency check on w has failed.
    public static final int TPM_BAD_HANDLE = TPM_BASE + 88; // The handle is incorrect 
    public static final int TPM_BAD_DELEGATE = TPM_BASE + 89; // Delegation is not correct 
    public static final int TPM_BADCONTEXT = TPM_BASE + 90; // The context blob is invalid 
    public static final int TPM_TOOMANYCONTEXTS = TPM_BASE + 91; // Too many contexts held by the TPM
    public static final int TPM_MA_TICKET_SIGNATURE = TPM_BASE + 92; // Migration authority signature validation failure
    public static final int TPM_MA_DESTINATION = TPM_BASE + 93; // Migration destination not authenticated
    public static final int TPM_MA_SOURCE = TPM_BASE + 94; // Migration source incorrect 
    public static final int TPM_MA_AUTHORITY = TPM_BASE + 95; // Incorrect migration authority 
    public static final int TPM_PERMANENTEK = TPM_BASE + 97; // Attempt to revoke the EK and the EK is not revocable 
    public static final int TPM_BAD_SIGNATURE = TPM_BASE + 98; // Bad signature of CMK ticket
    public static final int TPM_RETRY = TPM_BASE + TPM_NON_FATAL; // The TPM is too busy to respond to the command immediately, but the command could be resubmitted at a later time. The TPM MAY return TPM_Retry for any command at any time.
    public static final int TPM_NEEDS_SELFTEST = TPM_BASE + TPM_NON_FATAL + 1; // SelfTestFull has not been run
    public static final int TPM_DOING_SELFTEST = TPM_BASE + TPM_NON_FATAL + 2; // The TPM is currently executing a full selftest
    public static final int TPM_DEFEND_LOCK_RUNNING = TPM_BASE + TPM_NON_FATAL
        + 3; // The TPM is defending against dictionary attacks and is in some time-out period.

    // HACK: dirty Windows Vista TBS error workaround
    // Added by Thomas Müller, xnos Internet Services (xnos.org), 2007
    public static final int WINDOWS_VISTA_TBS_COMMAND_BLOCKED = -2144861184;

    public static String getErrorName( int returnCode )
    {
        switch ( returnCode )
        {
            case TPM_SUCCESS:
                return "TPM_SUCCESS";
            case TPM_AUTHFAIL:
                return "TPM_AUTHFAIL";
            case TPM_BADINDEX:
                return "TPM_BADINDEX";
            case TPM_BAD_PARAMETER:
                return "TPM_BAD_PARAMETER";
            case TPM_AUDITFAILURE:
                return "TPM_AUDITFAILURE";
            case TPM_CLEAR_DISABLED:
                return "TPM_CLEAR_DISABLED";
            case TPM_DEACTIVATED:
                return "TPM_DEACTIVATED";
            case TPM_DISABLED:
                return "TPM_DISABLED";
            case TPM_DISABLED_CMD:
                return "TPM_DISABLED_CMD";
            case TPM_FAIL:
                return "TPM_FAIL";
            case TPM_BAD_ORDINAL:
                return "TPM_BAD_ORDINAL";
            case TPM_INSTALL_DISABLED:
                return "TPM_INSTALL_DISABLED";
            case TPM_INVALID_KEYHANDLE:
                return "TPM_INVALID_KEYHANDLE";
            case TPM_KEYNOTFOUND:
                return "TPM_KEYNOTFOUND";
            case TPM_INAPPROPRIATE_ENC:
                return "TPM_INAPPROPRIATE_ENC";
            case TPM_MIGRATEFAIL:
                return "TPM_MIGRATEFAIL";
            case TPM_INVALID_PCR_INFO:
                return "TPM_INVALID_PCR_INFO";
            case TPM_NOSPACE:
                return "TPM_NOSPACE";
            case TPM_NOSRK:
                return "TPM_NOSRK";
            case TPM_NOTSEALED_BLOB:
                return "TPM_NOTSEALED_BLOB";
            case TPM_OWNER_SET:
                return "TPM_OWNER_SET";
            case TPM_RESOURCES:
                return "TPM_RESOURCES";
            case TPM_SHORTRANDOM:
                return "TPM_SHORTRANDOM";
            case TPM_SIZE:
                return "TPM_SIZE";
            case TPM_WRONGPCRVAL:
                return "TPM_WRONGPCRVAL";
            case TPM_BAD_PARAM_SIZE:
                return "TPM_BAD_PARAM_SIZE";
            case TPM_SHA_THREAD:
                return "TPM_SHA_THREAD";
            case TPM_SHA_ERROR:
                return "TPM_SHA_ERROR";
            case TPM_FAILEDSELFTEST:
                return "TPM_FAILEDSELFTEST";
            case TPM_AUTH2FAIL:
                return "TPM_AUTH2FAIL";
            case TPM_BADTAG:
                return "TPM_BADTAG";
            case TPM_IOERROR:
                return "TPM_IOERROR";
            case TPM_ENCRYPT_ERROR:
                return "TPM_ENCRYPT_ERROR";
            case TPM_DECRYPT_ERROR:
                return "TPM_DECRYPT_ERROR";
            case TPM_INVALID_AUTHHANDLE:
                return "TPM_INVALID_AUTHHANDLE";
            case TPM_NO_ENDORSEMENT:
                return "TPM_NO_ENDORSEMENT";
            case TPM_INVALID_KEYUSAGE:
                return "TPM_INVALID_KEYUSAGE";
            case TPM_WRONG_ENTITYTYPE:
                return "TPM_WRONG_ENTITYTYPE";
            case TPM_INVALID_POSTINIT:
                return "TPM_INVALID_POSTINIT";
            case TPM_INAPPROPRIATE_SIG:
                return "TPM_INAPPROPRIATE_SIG";
            case TPM_BAD_KEY_PROPERTY:
                return "TPM_BAD_KEY_PROPERTY";
            case TPM_BAD_MIGRATION:
                return "TPM_BAD_MIGRATION";
            case TPM_BAD_SCHEME:
                return "TPM_BAD_SCHEME";
            case TPM_BAD_DATASIZE:
                return "TPM_BAD_DATASIZE";
            case TPM_BAD_MODE:
                return "TPM_BAD_MODE";
            case TPM_BAD_PRESENCE:
                return "TPM_BAD_PRESENCE";
            case TPM_BAD_VERSION:
                return "TPM_BAD_VERSION";
            case TPM_NO_WRAP_TRANSPORT:
                return "TPM_NO_WRAP_TRANSPORT";
            case TPM_AUDITFAIL_UNSUCCESSFUL:
                return "TPM_AUDITFAIL_UNSUCCESSFUL";
            case TPM_AUDITFAIL_SUCCESSFUL:
                return "TPM_AUDITFAIL_SUCCESSFUL";
            case TPM_NOTRESETABLE:
                return "TPM_NOTRESETABLE";
            case TPM_NOTLOCAL:
                return "TPM_NOTLOCAL";
            case TPM_BAD_TYPE:
                return "TPM_BAD_TYPE";
            case TPM_INVALID_RESOURCE:
                return "TPM_INVALID_RESOURCE";
            case TPM_NOTFIPS:
                return "TPM_NOTFIPS";
            case TPM_INVALID_FAMILY:
                return "TPM_INVALID_FAMILY";
            case TPM_NO_NV_PERMISSION:
                return "TPM_NO_NV_PERMISSION";
            case TPM_REQUIRES_SIGN:
                return "TPM_REQUIRES_SIGN";
            case TPM_KEY_NOTSUPPORTED:
                return "TPM_KEY_NOTSUPPORTED";
            case TPM_AUTH_CONFLICT:
                return "TPM_AUTH_CONFLICT";
            case TPM_AREA_LOCKED:
                return "TPM_AREA_LOCKED";
            case TPM_BAD_LOCALITY:
                return "TPM_BAD_LOCALITY";
            case TPM_READ_ONLY:
                return "TPM_READ_ONLY";
            case TPM_PER_NOWRITE:
                return "TPM_PER_NOWRITE";
            case TPM_FAMILYCOUNT:
                return "TPM_FAMILYCOUNT";
            case TPM_WRITE_LOCKED:
                return "TPM_WRITE_LOCKED";
            case TPM_BAD_ATTRIBUTES:
                return "TPM_BAD_ATTRIBUTES";
            case TPM_INVALID_STRUCTURE:
                return "TPM_INVALID_STRUCTURE";
            case TPM_KEY_OWNER_CONTROL:
                return "TPM_KEY_OWNER_CONTROL";
            case TPM_BAD_COUNTER:
                return "TPM_BAD_COUNTER";
            case TPM_NOT_FULLWRITE:
                return "TPM_NOT_FULLWRITE";
            case TPM_CONTEXT_GAP:
                return "TPM_CONTEXT_GAP";
            case TPM_MAXNVWRITES:
                return "TPM_MAXNVWRITES";
            case TPM_NOOPERATOR:
                return "TPM_NOOPERATOR";
            case TPM_RESOURCEMISSING:
                return "TPM_RESOURCEMISSING";
            case TPM_DELEGATE_LOCK:
                return "TPM_DELEGATE_LOCK";
            case TPM_DELEGATE_FAMILY:
                return "TPM_DELEGATE_FAMILY";
            case TPM_DELEGATE_ADMIN:
                return "TPM_DELEGATE_ADMIN";
            case TPM_TRANSPORT_NOTEXCLUSIVE:
                return "TPM_TRANSPORT_NOTEXCLUSIVE";
            case TPM_OWNER_CONTROL:
                return "TPM_OWNER_CONTROL";
            case TPM_DAA_RESOURCES:
                return "TPM_DAA_RESOURCES";
            case TPM_DAA_INPUT_DATA0:
                return "TPM_DAA_INPUT_DATA0";
            case TPM_DAA_INPUT_DATA1:
                return "TPM_DAA_INPUT_DATA1";
            case TPM_DAA_ISSUER_SETTINGS:
                return "TPM_DAA_ISSUER_SETTINGS";
            case TPM_DAA_TPM_SETTINGS:
                return "TPM_DAA_TPM_SETTINGS";
            case TPM_DAA_STAGE:
                return "TPM_DAA_STAGE";
            case TPM_DAA_ISSUER_VALIDITY:
                return "TPM_DAA_ISSUER_VALIDITY";
            case TPM_DAA_WRONG_W:
                return "TPM_DAA_WRONG_W";
            case TPM_BAD_HANDLE:
                return "TPM_BAD_HANDLE";
            case TPM_BAD_DELEGATE:
                return "TPM_BAD_DELEGATE";
            case TPM_BADCONTEXT:
                return "TPM_BADCONTEXT";
            case TPM_TOOMANYCONTEXTS:
                return "TPM_TOOMANYCONTEXTS";
            case TPM_MA_TICKET_SIGNATURE:
                return "TPM_MA_TICKET_SIGNATURE";
            case TPM_MA_DESTINATION:
                return "TPM_MA_DESTINATION";
            case TPM_MA_SOURCE:
                return "TPM_MA_SOURCE";
            case TPM_MA_AUTHORITY:
                return "TPM_MA_AUTHORITY";
            case TPM_PERMANENTEK:
                return "TPM_PERMANENTEK";
            case TPM_BAD_SIGNATURE:
                return "TPM_BAD_SIGNATURE";
            case TPM_RETRY:
                return "TPM_RETRY";
            case TPM_NEEDS_SELFTEST:
                return "TPM_NEEDS_SELFTEST";
            case TPM_DOING_SELFTEST:
                return "TPM_DOING_SELFTEST";
            case TPM_DEFEND_LOCK_RUNNING:
                return "TPM_DEFEND_LOCK_RUNNING";

            // HACK: dirty Windows Vista TBS error workaround
            // Added by Thomas Müller, xnos Internet Services (xnos.org), 2007
            case WINDOWS_VISTA_TBS_COMMAND_BLOCKED:
                return "WINDOWS_VISTA_TBS_COMMAND_BLOCKED";

            default:
                return "UNKNOWN ERROR: " + returnCode;
        }
    }

}
