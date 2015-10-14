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
package edu.mit.csail.tpmj.tools.special;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateRevocableEK;
import edu.mit.csail.tpmj.commands.TPM_CreateRevocableEKOutput;
import edu.mit.csail.tpmj.commands.TPM_ReadPubek;
import edu.mit.csail.tpmj.commands.TPM_ReadPubekOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_KEY_PARMS;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_RSA_KEY_PARMS;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 * Creates a revocable EK (NOT TESTED)
 * 
 * Use tools.TPMCreateRevocableEK to create a revocable EK:<br>
 * <pre>
 * java edu.mit.csail.tpmj.tools.TPMCreateRevocableEK
 * </pre>
 * This tool is intended for advanced experimenter's use only!
 * <p>
 * Some TPM 1.2 chips on consumer PCs seem to be sold to the consumer
 * without an EK. This command is intended to try to allow you to create a revocable EK.
 * <p>
 * The authorization secret used for revoking is all-zeros.
 * <p>
 * If successful, this program writes the output of the TPM_CreateRevocableEK command 
 * to a file revocableEKcmd.bin. These bytes should follow the output structure 
 * for TPM_CreateRevocableEK.  Sect 14.2 in the TPM 1.2 Commands spec.)
 * <p>
 * <b>Warning:</b> This has <b>not</b> been fully tested. 
 * We tried to run it on a Broadcom TPM 1.2 chip which did not have an EK, 
 * but the TPM disallowed the TPM_CreateRevocableEK command.
 * <p>
 * If you intend to try this code, you are advised to browse 
 * through the source code first to make sure you understand what you
 * are doing.
 * 
 * @author lfgs
 */
public class TPMCreateRevocableEK
{

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.special.CreateRevocableEK"
            + "\n"
            + "This creates a revocable EK "
            + "with a null reset password (for testing purposes only)." );
    }

    public static void main( String[] args )
    {
        Debug.setThisClassDebugOn( true );

        TPM_SECRET inputEKreset = TPM_SECRET.NULL;

        try
        {
            TPMDriver tpmDriver = TPMUtilityFuncs.getTPMDriver();

            TPM_KEY_PARMS keyParms = new TPM_KEY_PARMS();
            keyParms.setAlgorithmID( TPMConsts.TPM_ALG_RSA );
            TPM_RSA_KEY_PARMS rsaKeyParms = new TPM_RSA_KEY_PARMS( 2048, 2,
                new byte[0] );
            keyParms.setParmData( rsaKeyParms );

            TPM_CreateRevocableEK cmd = new TPM_CreateRevocableEK(
                TPM_SECRET.NULL, keyParms, false, TPM_SECRET.NULL );

            byte[] inblob = cmd.toBytes();

            System.out.println( "Formed Command ... "
                + ByteArrayUtil.toPrintableHexString( inblob ) );

            System.out.println( "Writing revocableEKcmd.bin ..." );
            FileUtil.writeByteArray( "revocableEKcmd.bin", inblob );

            System.out.println( "\n*** Executing ... ***" );

            TPM_CreateRevocableEKOutput ekOut = cmd.execute( tpmDriver );
            TPM_PUBKEY pubKey = ekOut.getPubKey();
            TPM_DIGEST checksum = ekOut.getChecksum();
            TPM_NONCE outputEKreset = ekOut.getOutputEKreset();
            System.out.println( "Public Endorsement Key: " + pubKey );
            System.out.println( "Checksum: " + checksum );
            System.out.println( "Output EK reset: " + outputEKreset );

            byte[] blob = ekOut.toBytes();

            System.out.println( "Writing revocableEKout.bin ..." );
            FileUtil.writeByteArray( "revocableEKout.bin", blob );

            System.out.println( "Reading Public Endorsement Key ..." );
            System.out.println( "(using all-zeros as nonce)" );
            TPM_ReadPubek readPubekCmd = new TPM_ReadPubek( TPM_SECRET.NULL );
            TPM_ReadPubekOutput pubekOut = readPubekCmd.execute( tpmDriver );
            TPM_PUBKEY rpubKey = pubekOut.getPubKey();
            TPM_DIGEST rchecksum = pubekOut.getChecksum();
            System.out.println( "Public Endorsement Key: " + rpubKey );
            System.out.println( "Checksum: " + rchecksum );

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
}
