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
package edu.mit.csail.tpmj.tests;

import java.io.*;
import java.util.Arrays;

import edu.mit.csail.tpmj.*;
import edu.mit.csail.tpmj.commands.*;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.*;
import edu.mit.csail.tpmj.structs.*;
import edu.mit.csail.tpmj.util.*;

public class TPMPcrTest
{
    public static void run( String fileName, TPM_SECRET keyAuth )
    {
        try
        {
            TPMDriver tpmDriver = TPMUtilityFuncs.getTPMDriver();

            // FIXME: Commenting this out, since TPM_Reset seems to cause problems 
            // in both Mac OS X and Windows Vista
//            System.out.println( "Calling TPM_Reset ... " );
//            TPM_Reset resetCmd = new TPM_Reset();
//            resetCmd.execute( tpmDriver );
//            System.out.println( "OK." );

            byte[] buf = FileUtil.readIntoByteArray( fileName );

            TPM_KEY inKey = new TPM_KEY( buf );
            System.out.println( "Read (from file) key: " + inKey.toString() );


            // Note this only works if key was created without authorization

            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK, inKey, null );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );

            System.out.println( "Reading the public key ... " );
            TPM_GetPubKey cmd = new TPM_GetPubKey( keyHandle );

            TPM_GetPubKeyOutput output = (TPM_GetPubKeyOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, cmd, keyAuth );
            TPM_PUBKEY pubKey = output.getPubKey(); 

            System.out.println( "PubKey=" + pubKey );

            System.out.println( "Quoting all " + TPMPcrFuncs.getNumPcrs() + " PCRs ..." );
            TPM_PCR_SELECTION allPcrs = new TPM_PCR_SELECTION( TPMPcrFuncs.getNumPcrs() );
            allPcrs.setAllOn();
            
            TPM_NONCE externalData = new TPM_NONCE();
            
            TPM_Quote quoteCmd = new TPM_Quote( keyHandle, externalData, allPcrs );
//            quoteCmd.setNoAuth();
//            TPM_QuoteOutput quoteOut = quoteCmd.execute( tpmDriver );
            TPM_QuoteOutput quoteOut = (TPM_QuoteOutput) TPMOIAPSession.executeOIAPSession(
                tpmDriver, quoteCmd, keyAuth );
            
            System.out.println( "Output=" + quoteOut  );
            TPM_PCR_COMPOSITE pcrData = quoteOut.getPcrData(); 
            byte[] pcrBytes = pcrData.toBytes();
            TPM_QUOTE_INFO quoteInfo = new TPM_QUOTE_INFO( TPMPcrFuncs.getVersionForQuoteInfo(),
                quoteOut.getPcrData(), externalData );
            System.out.println( "Quote Info: " + quoteInfo + "\nBytes: " + ByteArrayUtil.toHexString( quoteInfo.toBytes() ) );
            byte[] sig = quoteOut.getSig();
            System.out.println( "Verifying Signature ... ");
            boolean quoteOK = TPMPcrFuncs.verifyQuote( pubKey, quoteOut, externalData );
//            boolean quoteOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature( pubKey, sig, quoteInfo.toBytes() );
            System.out.println( "Sig OK?" + quoteOK );

            System.out.println( "Quoting single PCR #8: " );
            TPM_QuoteOutput quoteOut1 = TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, externalData, 8 );
            TPM_PCRVALUE pcrValue = quoteOut1.getPcrData().getPcrValues()[0];
            boolean quote1OK = TPMPcrFuncs.verifyQuote( pubKey.getPubKey().getKeyBytes(), 8, pcrValue, quoteOut1.getSig(), externalData );
            System.out.println( "PCR: " + pcrValue );
            System.out.println( "Sig OK?" + quote1OK );
            
            if ( tpmDriver.getTPMVersion().getMinor() > 1 )
            {
                System.out.println( "Quoting PCRs 16 and 23 ..." );
                TPM_PCR_SELECTION pcrSelection = new TPM_PCR_SELECTION( 24, 16, 23 );
                TPM_Quote quoteCmd2 = new TPM_Quote( keyHandle, externalData, pcrSelection );
//                quoteCmd2.setNoAuth();
//                TPM_QuoteOutput quoteOut2 = quoteCmd2.execute( tpmDriver );
                TPM_QuoteOutput quoteOut2 = (TPM_QuoteOutput) TPMOIAPSession.executeOIAPSession(
                    tpmDriver, quoteCmd2, keyAuth );
                System.out.println( "Output=" + quoteOut2 );
    
                TPM_QUOTE_INFO quoteInfo2 = new TPM_QUOTE_INFO( TPMPcrFuncs.getVersionForQuoteInfo(),
                    quoteOut2.getPcrData(), externalData );
                System.out.println( "Quote Info: " + quoteInfo2 );
                byte[] sig2 = quoteOut2.getSig();
                System.out.println( "Verifying Signature ... ");
                boolean quoteOK2 = TPMPcrFuncs.verifyQuote( pubKey, quoteOut2, externalData );
    //            boolean quoteOK2 = TPMStorageFuncs.TSS_VerifySHA1RSASignature( pubKey, sig2, quoteInfo2.toBytes() );
                System.out.println( "Sig OK?" + quoteOK2 );
    
                System.out.println( "Demonstrating Signing 'attack' ... " );
                byte[] fakeSig2 = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle, quoteInfo2.toBytes(), keyAuth );
                System.out.println( "Produced sig: " + ByteArrayUtil.toHexString( fakeSig2 ) );
                System.out.println( "Equal? " + Arrays.equals( sig2, fakeSig2 ) );
            }
            System.out.println( "Evicting keyHandle: 0x"
                + Integer.toHexString( keyHandle ) );
            TPMStorageFuncs.TPM_EvictKey( keyHandle );
            System.out.println( "OK" );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        catch ( Exception e )
        {
            System.err.println( "Exception: " + e );
            e.printStackTrace();
        }

    }

    public static void main( String[] args )
    {
        Debug.setThisClassDebugOn( true );

        String fileName = "testkey.key";
        if ( args.length > 0 )
        {
            fileName = args[0];
        }

        TPM_SECRET keyAuth = TPM_SECRET.NULL;
        
        String keyAuthString = "test";
        if ( args.length > 1 )
        {
            keyAuthString = args[1];
        }
        System.out.println( "keyAuthString: " + keyAuthString );
        if ( keyAuthString.length() > 0 )
        {
            keyAuth = TPMToolsUtil.convertAuthString( keyAuthString, "keyAuthString" );
        }
        
        TPMUtilityFuncs.initTPMDriver();

        TPMPcrTest.run( fileName, keyAuth );
        
        TPMUtilityFuncs.cleanupTPMDriver();
    }

}
