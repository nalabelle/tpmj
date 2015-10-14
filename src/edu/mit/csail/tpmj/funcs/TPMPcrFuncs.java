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

import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_PCRVALUE;
import edu.mit.csail.tpmj.structs.TPM_PCR_COMPOSITE;
import edu.mit.csail.tpmj.structs.TPM_PCR_INFO;
import edu.mit.csail.tpmj.structs.TPM_PCR_SELECTION;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_QUOTE_INFO;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.structs.TPM_STRUCT_VER;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPMPcrFuncs extends TPMUtilityFuncs
{
    private static int numPcrs;

    /**
     * Returns the number of PCRs.
     * Saves the actual value in a static variable the first time
     * it successfully gets it, and then uses the saved value
     * for subsequent calls.
     * 
     * @return number of PCRs, 0 if there is an error.
     */
    public static int getNumPcrs()
    {
        if ( numPcrs == 0 )
        {
            try
            {
                numPcrs = TPMGetCapabilityFuncs.getNumPcrs();
            }
            catch ( TPMException e )
            {
                Debug.println( "TPMException getting numPCRs: ", e );
            }
        }
        return numPcrs;
    }

    /**
     * Clears the saved value of numPcrs so that a call to getNumPcrs()
     * would trigger a call to TPM_GetCapability to get the number of PCRs again from the TPM.
     */
    public static void clearNumPcrs()
    {
        numPcrs = 0;
    }

    public static TPM_PCR_COMPOSITE readPCRsIntoComposite( int... pcrNums )
        throws TPMException
    {
        TPM_PCR_SELECTION pcrSelection = new TPM_PCR_SELECTION(
            TPMPcrFuncs.getNumPcrs(), pcrNums );

        TPM_PCRVALUE[] pcrVals = new TPM_PCRVALUE[pcrNums.length];
        for ( int i = 0; i < pcrNums.length; i++ )
        {
            pcrVals[i] = TPMPcrFuncs.TPM_PCRRead( pcrNums[i] );
        }
        TPM_PCR_COMPOSITE pcrComposite = new TPM_PCR_COMPOSITE( pcrSelection,
            pcrVals );
        return pcrComposite;
    }

    public static TPM_PCR_INFO readPCRsIntoPCRInfo( int... pcrNums )
        throws TPMException
    {
        TPM_PCR_COMPOSITE pcrComposite = TPMPcrFuncs.readPCRsIntoComposite( pcrNums );
        
        return new TPM_PCR_INFO( pcrComposite );
    }

    public static TPM_PCRVALUE TPM_PCRRead( int pcrIndex ) throws TPMException
    {
        TPM_PCRRead cmd = new TPM_PCRRead( pcrIndex );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_PCRReadOutput output = cmd.execute( tpmDriver );
        // If we reach here, then there was no error
        return output.getOutDigest();
    }

    public static TPM_PCRVALUE TPM_Extend( int pcrNum, TPM_NONCE inDigest )
        throws TPMException
    {
        TPM_Extend cmd = new TPM_Extend( pcrNum, inDigest );
        // Note: this can throw a TPMException, 
        // in which case, output is never returned
        TPM_PCRReadOutput output = cmd.execute( tpmDriver );
        // If we reach here, then there was no error
        return output.getOutDigest();
    }

    public static boolean TPM_PCR_Reset( int pcrNum ) throws TPMException
    {
        int numPCRs = getNumPcrs();
        TPM_PCR_SELECTION pcrSelection = new TPM_PCR_SELECTION( numPCRs, pcrNum );
        TPM_PCR_Reset cmd = new TPM_PCR_Reset( pcrSelection );
        TPMOutputStruct output = cmd.execute( tpmDriver );
        // FIXME: Use correct TPM no error constant.
        return (output.getReturnCode() == 0);
    }

    /**
     * Returns the version to use for QuoteInfo (i.e., 1.1.0.0 if using TPM 1.2 or higher,
     * or the result of TPMDriver's getTPMVersion if TPM 1.1. 
     * 
     * @return
     */
    public static TPM_STRUCT_VER getVersionForQuoteInfo()
    {
        TPM_STRUCT_VER ver = tpmDriver.getTPMVersion();
        if ( ver.getMinor() > 1 )
        {
            return TPM_STRUCT_VER.TPM_1_1_VER;
        }
        else
        {
            return ver;
        }
    }

    public static boolean verifyQuote( TPM_PUBKEY pubKey,
        TPM_QuoteOutput quoteOut, TPM_NONCE externalData )
    {
        return verifyQuote( pubKey.getPubKey().getKeyBytes(), quoteOut,
            externalData );
    }

    /**
     * @param pubKeyBytes
     * @param quoteOut
     * @param externalData
     * @return
     */
    public static boolean verifyQuote( byte[] pubKeyBytes,
        TPM_QuoteOutput quoteOut, TPM_NONCE externalData )
    {
        return verifyQuote( pubKeyBytes, quoteOut.getPcrData(),
            quoteOut.getSig(), externalData );
    }

    public static boolean verifyQuote( byte[] pubKey, int pcrNum,
        TPM_PCRVALUE pcrValue, byte[] sig, TPM_NONCE externalData )
    {
        TPM_PCR_COMPOSITE pcrComposite = new TPM_PCR_COMPOSITE(
            new TPM_PCR_SELECTION( TPMPcrFuncs.getNumPcrs(), pcrNum ), pcrValue );
        return verifyQuote( pubKey, pcrComposite, sig, externalData );
    }

    /**
     * @param pubKey
     * @param pcrComposite
     * @param sig
     * @param externalData
     * @return
     */
    public static boolean verifyQuote( byte[] pubKey,
        TPM_PCR_COMPOSITE pcrComposite, byte[] sig, TPM_NONCE externalData )
    {
        TPM_QUOTE_INFO quoteInfo = createTPM_QUOTE_INFO( pcrComposite,
            externalData );
        Debug.println( "Quote Info: ", quoteInfo );
        Debug.println( "Verifying Signature ... " );
        boolean quoteOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature( pubKey,
            sig, quoteInfo.toBytes() );
        return quoteOK;
    }

    /**
     * @param pcrComposite
     * @param externalData
     * @return
     */
    public static TPM_QUOTE_INFO createTPM_QUOTE_INFO(
        TPM_PCR_COMPOSITE pcrComposite, TPM_NONCE externalData )
    {
        TPM_QUOTE_INFO quoteInfo = new TPM_QUOTE_INFO(
            TPMPcrFuncs.getVersionForQuoteInfo(), pcrComposite, externalData );
        return quoteInfo;
    }

    /**
     * @param keyHandle
     * @param externalData
     * @param pcrNum
     * @return
     * @throws TPMException
     */
    public static TPM_QuoteOutput TPM_Quote( int keyHandle, TPM_SECRET keyAuth,
        TPM_NONCE externalData, int... pcrs ) throws TPMException
    {
        TPM_PCR_SELECTION pcrSelection = new TPM_PCR_SELECTION(
            TPMPcrFuncs.getNumPcrs(), pcrs );
        return TPM_Quote( keyHandle, keyAuth, externalData, pcrSelection );
    }

    /**
     * @param keyHandle
     * @param keyAuth
     * @param externalData
     * @param pcrSelection
     * @return
     * @throws TPMException
     */
    public static TPM_QuoteOutput TPM_Quote( int keyHandle, TPM_SECRET keyAuth,
        TPM_NONCE externalData, TPM_PCR_SELECTION pcrSelection )
        throws TPMException
    {
        TPM_Quote quoteCmd2 = new TPM_Quote( keyHandle, externalData,
            pcrSelection );
        TPM_QuoteOutput quoteOut2 = (TPM_QuoteOutput) TPMOIAPSession.executeOIAPSession(
            tpmDriver, quoteCmd2, keyAuth );
        return quoteOut2;
    }

    /**
     * Quotes all available PCRs.
     * 
     * @param keyHandle
     * @param keyAuth
     * @param externalData
     * @return
     * @throws TPMException
     */
    public static TPM_QuoteOutput TPM_Quote( int keyHandle, TPM_SECRET keyAuth,
        TPM_NONCE externalData ) throws TPMException
    {
        TPM_PCR_SELECTION allPcrs = new TPM_PCR_SELECTION(
            TPMPcrFuncs.getNumPcrs() );
        allPcrs.setAllOn();

        return TPM_Quote( keyHandle, keyAuth, externalData, allPcrs );

    }

}
