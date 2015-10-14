/*
 * Copyright (c) 2006,2007 Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006,2007
 */
package edu.mit.csail.tpmj.transport;

import java.util.ArrayList;
import java.util.Map;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.commands.TPM_EstablishTransport;
import edu.mit.csail.tpmj.commands.TPM_EstablishTransportOutput;
import edu.mit.csail.tpmj.commands.TPM_ExecuteTransportOutput;
import edu.mit.csail.tpmj.commands.TPM_ReleaseTransportSigned;
import edu.mit.csail.tpmj.commands.TPM_ReleaseTransportSignedOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.structs.TPM_CURRENT_TICKS;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_SIGN_INFO;
import edu.mit.csail.tpmj.structs.TPM_STORE_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_TRANSPORT_LOG_IN;
import edu.mit.csail.tpmj.structs.TPM_TRANSPORT_LOG_OUT;
import edu.mit.csail.tpmj.util.BasicByteArrayable;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPMTransportLog extends BasicByteArrayable
{
    private int tpmManufacturer = TPMDriver.TPM_MANUFACTURER_UNKNOWN;
    private ArrayList<TPMTransportLogEntry> log = new ArrayList<TPMTransportLogEntry>();

    // NOTE: No no-args constructor is provided because of the danger
    // of creating an empty transport log to be filled and forgetting to set the manufacturer

    public TPMTransportLog( int tpmManufacturer )
    {
        super();
        this.tpmManufacturer = tpmManufacturer;
    }

    /**
     * Construct using fromBytes with offset 0
     * 
     * @param source
     */
    public TPMTransportLog( byte[] source )
    {
        this.fromBytes( source, 0 );
    }

    /**
     * Construct using fromBytes with offset 
     * @param source
     * @param offset
     */
    public TPMTransportLog( byte[] source, int offset )
    {
        this.fromBytes( source, offset );
    }
    
    
    public int getTpmManufacturer()
    {
        return tpmManufacturer;
    }

    public boolean add( TPMTransportLogEntry logEntry )
    {
        // System.out.println( "TPMTransportLog.add called " + logEntry );
        return log.add( logEntry );
    }

    // NOTE: Commented out to avoid reusing the same transport log.
    //    public void clear()
    //    {
    //        log.clear();
    //    }

    public TPMTransportLogEntry get( int index )
    {
        return log.get( index );
    }

    public int size()
    {
        return log.size();
    }

    public TPM_CURRENT_TICKS getStartTicks()
    {
        TPMTransportLogEntry logEntry = log.get( 0 );
        TPM_EstablishTransportOutput estOut = (TPM_EstablishTransportOutput) logEntry.getTransOut();
        return estOut.getCurrentTicks();
    }

    public TPM_NONCE getAntiReplayNonce()
    {
        TPMTransportLogEntry logEntry = log.get( log.size() - 1 );
        TPM_ReleaseTransportSigned relCmd = (TPM_ReleaseTransportSigned) logEntry.getTransCmd();
        TPM_NONCE antiReplay = relCmd.getAntiReplay();
        // System.out.println( "TPMTransportLog: antiReplay = " + antiReplay );
        return antiReplay;
    }

    /**
     * This method is called in getLogSignInfo to get the initial transDigest.
     * According to spec, transDigest starts as NULL and is extended every time.
     * However, I've tested it on a Broadcom 1.2 chip, and verified
     * that actually using null (i.e., an empty string) works, 
     * not using an all-zeros TPM_DIGEST.
     * I've also tested it on an ST Micro chip (on an Gateway M465-E),
     * and the empty string also worked.
     * However on an Infineon 1.2 chip, using an all-zeros TPM_DIGEST works.
     * <p>
     * This current version checks for Infineon and returns
     * a null in all other cases.
     * 
     * @return
     */
    private TPM_DIGEST setupInitialTransDigest()
    {
//        if ( this.getTpmManufacturer() == TPMDriver.TPM_MANUFACTURER_BROADCOM )
//        {
//            return null;
//        }
//        else
//        {
//            return new TPM_DIGEST();
//        }
        if ( this.getTpmManufacturer() == TPMDriver.TPM_MANUFACTURER_INFINEON )
        {
            return new TPM_DIGEST();
        }
        else
        {
            return null;
        }
        
    }

    /**
     * This method is called in getTransportLogIn to get a pubKeyHash in case
     * there are no keys in the command.
     * <p> 
     * I've tested it on a Broadcom 1.2 chip, and verified 
     * that actually using null (i.e., a zero-length emptry string, 
     * as opposed to an all-zeros TPM_DIGEST) works.)
     * i.e., it is correct for the TPM_TRANSPORT_LOG_IN to be shorter 
     * if there is no pubKeyHash
     * I've also tested it on an ST Micro chip (on an Gateway M465-E),
     * and the empty string also worked.
     * However on an Infineon 1.2 chip, using an all-zeros TPM_DIGEST works.
     * <p>
     * This current version checks for Infineon and returns
     * a null in all other cases.
     * 
     * @return
     */
    private TPM_DIGEST createEmptyPubKeyHash()
    {
//        if ( this.getTpmManufacturer() == TPMDriver.TPM_MANUFACTURER_BROADCOM )
//        {
//            return null;
//        }
//        else
//        {
//            return new TPM_DIGEST();
//        }
        if ( this.getTpmManufacturer() == TPMDriver.TPM_MANUFACTURER_INFINEON )
        {
            return new TPM_DIGEST();
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns a TPM_SIGN_INFO structure.  NOTE that the way the 
     * hashes are computed, specifically, the way the initial
     * transDigest, as well as the pubKeyHash in each TPM_TRANSPORT_LOGIN
     * structure, are treated has been found to be different
     * depending on the manufacturer (e.g., Infineon vs. Broadcom).
     * This method computes it according to the tpm Manufacturer
     * designated in the TPMTransportLog at its creation time,
     * such that the signature produced by ReleaseTransportSigned
     * will match properly. 
     * 
     * @param keyMap
     * @return
     */
    public TPM_SIGN_INFO getLogSignInfo( //TPM_NONCE antiReplay,
        Map<Integer, TPM_STORE_PUBKEY> keyMap )
    {
        //        TPM_DIGEST transDigest = this.computeLogDigest( keyMap );

        if ( (log.size() < 2)
            || !(log.get( 0 ).getTransOut() instanceof TPM_EstablishTransportOutput)
            || !(log.get( log.size() - 1 ).getTransOut() instanceof TPM_ReleaseTransportSignedOutput) )
        {
            // log must have at least an Establish and a ReleaseTransportSigned
            return null;
        }

        // NOTE: according to spec, transDigest starts as NULL
        // and is extended every time.

        // NOTE: I've tested it on a Broadcom 1.2 chip, and verified
        // that actually using null (i.e., an empty string)
        // works, not using an all-zeros TPM_DIGEST.
        //        TPM_DIGEST transDigest = null;

        // Try this for Infineon
        TPM_DIGEST transDigest = setupInitialTransDigest();

        TPM_CURRENT_TICKS previousCurrentTicks = this.getStartTicks();
        TPM_NONCE antiReplay = this.getAntiReplayNonce();
        byte[] concat = new byte[0];

        for ( int i = 0; i < log.size(); i++ )
        {
            TPMTransportLogEntry logEntry = log.get( i );

            // Debug.println( "Log entry " + i + ": " + logEntry );

            if ( i == (log.size() - 1) )
            {
                TPM_ReleaseTransportSigned relCmd = (TPM_ReleaseTransportSigned) logEntry.getTransCmd();
                antiReplay = relCmd.getAntiReplay();
                // System.out.println( "TPMTransportLog: antiReplay = " + antiReplay );
            }

            TPM_TRANSPORT_LOG_IN inLog = this.getTransportLogIn( logEntry,
                keyMap );
            if ( inLog != null )
            {
                //                Debug.println( "inLog: " + inLog + "\n(bytes): "
                //                    + ByteArrayUtil.toPrintableHexString( inLog.toBytes() ) );

                concat = ByteArrayUtil.concat( transDigest, inLog );
                transDigest = CryptoUtil.computeTPM_DIGEST( concat );

                //                Debug.println( "concat: "
                //                    + ByteArrayUtil.toPrintableHexString( concat ) );
                //                Debug.println( "transDigest: " + transDigest );
            }
            TPM_TRANSPORT_LOG_OUT outLog = this.getTransportLogOut( logEntry,
                previousCurrentTicks );
            if ( outLog != null )
            {
                //                Debug.println( "outLog: " + outLog + "\n(bytes): "
                //                    + ByteArrayUtil.toPrintableHexString( outLog.toBytes() ) );

                concat = ByteArrayUtil.concat( transDigest, outLog );
                transDigest = CryptoUtil.computeTPM_DIGEST( concat );

                //                Debug.println( "concat: "
                //                    + ByteArrayUtil.toPrintableHexString( concat ) );
                //                Debug.println( "transDigest: " + transDigest );
            }
        }

        TPM_SIGN_INFO signInfo = new TPM_SIGN_INFO( TPM_SIGN_INFO.FIXED_TRAN,
            antiReplay, transDigest.toBytes() );
        return signInfo;
    }

    public boolean verify( byte[] modulusBytes,
        Map<Integer, TPM_STORE_PUBKEY> keyMap )
    {
        TPM_ReleaseTransportSignedOutput signOut = getReleasedTransportSignedOutput();

        TPM_SIGN_INFO signInfo = this.getLogSignInfo( keyMap );
        Debug.println( "got sign info: ", signInfo );
        byte[] signInfoBytes = signInfo.toBytes();
        Debug.println( "sign info bytes: ", signInfoBytes );
        Debug.println( "Verifying signature ... " );
        Debug.println( "PubKey key byte array (" + modulusBytes.length
            + " bytes): ", modulusBytes );
        Debug.println( "Verifying using verifySHA1RSASignature on signInfo ... " );
        boolean signOK = CryptoUtil.verifySHA1RSASignature( modulusBytes,
            signOut.getSignature(), signInfoBytes );
        Debug.println( "Signature OK? " + signOK );
        return signOK;
    }

    /**
     * Returns the TPM_ReleaseTransportSignedOutput structure,
     * which contains (among other things), the signature
     * over the TPM_SIGN_INFO structure of the transport session log.
     * (Returns null if last entry's output is not a TPM_ReleaseTransportSignedOutput
     * 
     * @return
     */
    public TPM_ReleaseTransportSignedOutput getReleasedTransportSignedOutput()
    {
        TPMTransportLogEntry logEntry = log.get( log.size() - 1 );
        try
        {
            TPM_ReleaseTransportSignedOutput signOut = (TPM_ReleaseTransportSignedOutput) logEntry.getTransOut();
            return signOut;
        }
        catch ( ClassCastException cce )
        {
            return null;
        }
    }

    public byte[] getSignature()
    {
        TPM_ReleaseTransportSignedOutput signOut = this.getReleasedTransportSignedOutput();
        if ( signOut != null )
        {
            return signOut.getSignature();
        }
        else
        {
            return null;
        }
    }

    @Override
    public byte[] toBytes()
    {
        
        byte[][] logEntryArrs = new byte[this.size()][];
        int totalLength = 0;

        // for tpmManufacturer
        totalLength += 4;

        // for number of entries
        totalLength += 4;

        for ( int i = 0; i < this.size(); i++ )
        {
            logEntryArrs[i] = this.get( i ).toBytes();
            totalLength += (logEntryArrs[i].length + 4);
        }
        byte[] concat = new byte[totalLength];

        ByteArrayReadWriter brw = new ByteArrayReadWriter( concat, 0 );

        brw.writeInt32( this.getTpmManufacturer() );
        brw.writeInt32( this.size() );

        for ( int i = 0; i < this.size(); i++ )
        {
            brw.writeSizedByteArray( logEntryArrs[i] );
        }
        return concat;
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );

        this.tpmManufacturer = brw.readInt32();
        
        int logSize = brw.readInt32();

        this.log = new ArrayList<TPMTransportLogEntry>();

        for ( int i = 0; i < logSize; i++ )
        {
            byte[] entryArr = brw.readSizedByteArray();
            TPMTransportLogEntry logEntry = new TPMTransportLogEntry( entryArr );
            this.add( logEntry );
        }
    }

    public String toString()
    {
        String s = "TPMTransportLog (manuf 0x" + this.getTpmManufacturer()
            + "):\n" + "(" + this.size() + " entries)";
        for ( int i = 0; i < this.size(); i++ )
        {
            s = s + "\n" + "Entry " + i + ": " + this.get( i );
        }
        return s;

    }

    private TPM_TRANSPORT_LOG_OUT getTransportLogOut(
        TPMTransportLogEntry logEntry, TPM_CURRENT_TICKS previousCurrentTicks )
    {
        TPM_CURRENT_TICKS currentTicks;
        TPM_DIGEST parameters;
        int locality;
        TPM_TRANSPORT_LOG_OUT outLog;

        if ( logEntry.getTransCmd().getOrdinal() == TPMConsts.TPM_ORD_EstablishTransport )
        {
            TPM_EstablishTransport estCmd = (TPM_EstablishTransport) logEntry.getTransCmd();
            TPM_EstablishTransportOutput tOut = (TPM_EstablishTransportOutput) logEntry.getTransOut();
            byte[] paramsConcat = ByteArrayUtil.concatObjectsBE(
                tOut.getReturnCode(), estCmd.getOrdinal(), tOut.getLocality(),
                tOut.getCurrentTicks(), tOut.getTransNonce() );
            //            Debug.println( "paramsConcat: " + ByteArrayUtil.toPrintableHexString( paramsConcat ) );
            parameters = CryptoUtil.computeTPM_DIGEST( paramsConcat );
            currentTicks = tOut.getCurrentTicks();
            locality = tOut.getLocality();
            outLog = new TPM_TRANSPORT_LOG_OUT( currentTicks, parameters,
                locality );
        }
        else if ( logEntry.getTransCmd().getOrdinal() == TPMConsts.TPM_ORD_ExecuteTransport )
        {
            TPM_ExecuteTransportOutput tOut = (TPM_ExecuteTransportOutput) logEntry.getTransOut();

            byte[] h2Concat = TPMTransportSession.computeUnwrappedOutputParamBytesToDigest(
                logEntry.getOrigCmd().getOrdinal(), logEntry.getUnencOut() );
            //            Debug.println( "h2Concat: " + ByteArrayUtil.toPrintableHexString( h2Concat ) );
            parameters = CryptoUtil.computeTPM_DIGEST( h2Concat );
            currentTicks = new TPM_CURRENT_TICKS( tOut.getCurrentTicks(),
                previousCurrentTicks.getTickRate(),
                previousCurrentTicks.getTickNonce() );
            locality = tOut.getLocality();
            outLog = new TPM_TRANSPORT_LOG_OUT( currentTicks, parameters,
                locality );
        }
        else if ( logEntry.getTransCmd().getOrdinal() == TPMConsts.TPM_ORD_ReleaseTransportSigned )
        {
            // note: according to specs, TPM_ReleaseTransportSigned doesn't to do anything with
            // TPM_TRANSPORT_LOG_IN, just with TPM_TRANSPORT_LOG_OUT
            TPM_ReleaseTransportSigned relCmd = (TPM_ReleaseTransportSigned) logEntry.getTransCmd();
            TPM_ReleaseTransportSignedOutput relOut = (TPM_ReleaseTransportSignedOutput) logEntry.getTransOut();
            byte[] paramsConcat = ByteArrayUtil.concatObjectsBE(
                relCmd.getOrdinal(), relCmd.getAntiReplay() );
            parameters = CryptoUtil.computeTPM_DIGEST( paramsConcat );
            currentTicks = relOut.getCurrentTicks();
            locality = relOut.getLocality();
            outLog = new TPM_TRANSPORT_LOG_OUT( currentTicks, parameters,
                locality );
        }
        else
        {
            return null;
        }
        return outLog;
    }

    private TPM_TRANSPORT_LOG_IN getTransportLogIn(
        TPMTransportLogEntry logEntry, Map<Integer, TPM_STORE_PUBKEY> keyMap )
    {
        TPM_DIGEST parameters;
        TPM_DIGEST pubKeyHash;
        TPM_TRANSPORT_LOG_IN inLog;

        if ( logEntry.getTransCmd().getOrdinal() == TPMConsts.TPM_ORD_EstablishTransport )
        {
            TPM_EstablishTransport estCmd = (TPM_EstablishTransport) logEntry.getTransCmd();
            byte[] paramsConcat = ByteArrayUtil.concatObjectsBE(
                estCmd.getOrdinal(), estCmd.getTransPublic(),
                estCmd.getSecretSize(), estCmd.getSecret() );
            //            Debug.println( "paramsConcat: " + ByteArrayUtil.toPrintableHexString( paramsConcat ) );
            parameters = CryptoUtil.computeTPM_DIGEST( paramsConcat );

            // NOTE:
            // Works for Broadcom 1.2
            // pubKeyHash = null; // new TPM_DIGEST(); // NULL
            // But this works for Infineon chip
            // pubKeyHash = new TPM_DIGEST();
            // So, use tpmManufacturer specific method instead.

            pubKeyHash = this.createEmptyPubKeyHash();

            inLog = new TPM_TRANSPORT_LOG_IN( parameters, pubKeyHash );
        }
        else if ( logEntry.getTransCmd().getOrdinal() == TPMConsts.TPM_ORD_ExecuteTransport )
        {
            byte[] h1Concat = TPMTransportSession.computeWrappedCmdParamBytesToDigest( logEntry.getOrigCmd().toBytes() );
            //            Debug.println( "h1Concat: " + ByteArrayUtil.toPrintableHexString( h1Concat ) );
            parameters = CryptoUtil.computeTPM_DIGEST( h1Concat );
            // FIXME: !!! This does not work if there are any keys.
            // If there are keys, then the key handles must be changed to
            // the actual public keys to used.

            // NOTE:
            // Works for Broadcom 1.2
            // pubKeyHash = null; // new TPM_DIGEST(); // NULL
            // But this works for Infineon chip
            // pubKeyHash = new TPM_DIGEST();
            // So, use tpmManufacturer specific method instead.

            pubKeyHash = this.createEmptyPubKeyHash();

            inLog = new TPM_TRANSPORT_LOG_IN( parameters, pubKeyHash );
        }
        else
        // if ( transCmd.getOrdinal() == TPMConsts.TPM_ORD_ReleaseTransportSigned )
        {
            // note: according to specs, TPM_ReleaseTransportSigned doesn't to do anything with
            // TPM_TRANSPORT_LOG_IN, just with TPM_TRANSPORT_LOG_OUT
            inLog = null;
        }
        return inLog;
    }

}
