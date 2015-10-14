package edu.mit.csail.tpmj.tools;

import java.util.Arrays;

import bayanihan.util.params.SwitchParams;

import edu.mit.csail.tpmj.TPMConsts;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_MakeIdentityOutput;
import edu.mit.csail.tpmj.commands.TPM_QuoteOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthorizationSession;
import edu.mit.csail.tpmj.funcs.TPMIdentityFuncs;
import edu.mit.csail.tpmj.funcs.TPMPcrFuncs;
import edu.mit.csail.tpmj.funcs.TPMStorageFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_DIGEST;
import edu.mit.csail.tpmj.structs.TPM_KEY;
import edu.mit.csail.tpmj.structs.TPM_PUBKEY;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.tests.TPMPcrTest;
import edu.mit.csail.tpmj.tools.special.TPMSelfTest;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.Debug;
import edu.mit.csail.tpmj.util.FileUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 */
public class TPMCreateAIK
{
    public static void usage()
    {
        System.out.println( "Usage: TPMCreateAIK <fileName> <aikPwd> <labelPrivCA> <ownerPwd> [srkPwd] \n\n"
            + "Outputs: AIK key blob (TPM_KEY format) in file <fileName>\n\n"
            + "Parameters:\n"
            + "fileName - The filename the AIK will be stored to.\n\n"
            + "aikPwd - The password for the AIK\n\n"
            + "labelPrivCA - Data to be used for labelPrivCADigest\n" 
            + "              (use 0x followed by 20 bytes in hex or -B64 followed by Base64 string)\n\n"
            + "ownerPwd - The password of the owner of the TPM\n\n"
            + "srkPwd - The password to the SRK of the TPM (defaults to null)" );
        System.exit( -1 );
    }

    /**
     *  Creates a new AIK with the specified password and stores it in aikFile.
     *  If no password is specified, the password defaults to 'test'.
     */
    // Unit test
    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "fileName", "aikPwd",
            "labelPrivCA",
            "ownerPwd", "srkPwd" );
        String fileName = params.getString( "fileName" );
        TPM_SECRET keyAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "aikPwd" );
        
        TPM_SECRET labelPrivCASecret = TPMToolsUtil.createTPM_SECRETFromParams( params, "labelPrivCA" );
        TPM_DIGEST labelPrivCADigest = new TPM_DIGEST( labelPrivCASecret.toBytes() );
        
        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "ownerPwd" );
        TPM_SECRET srkAuth = TPMToolsUtil.createTPM_SECRETFromParams( params, "srkPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        // Try to create the AIK using the ownerAuth, srkAuth, and keyAuth
        // TPM_AUTH2FAIL indicates a failure in the owner password.
        try
        {

            TPMSelfTest.doSelfTest();
            
            System.out.println( "Creating Identity Key ... " );


            // Create the AIK.  This call can take quite a while.
            TPM_MakeIdentityOutput output = TPMIdentityFuncs.TPM_CreateAIK(
                ownerAuth, srkAuth, keyAuth, labelPrivCADigest );
            
            System.out.println( "DONE.\n" );
            // System.out.println( "Returned: " + output );

            TPM_KEY key = output.getIdKey();
            System.out.println( "ID KEY = " + key + "\n" );

            byte[] identityBindingBytes = output.getIdentityBinding();
            System.out.println( "Identity Binding = (" + identityBindingBytes.length + " bytes):" );
            System.out.println( ByteArrayUtil.toPrintableHexString( identityBindingBytes ) );

            byte[] blob = key.toBytes();

            System.out.println( "Writing " + fileName + " ..." );
            FileUtil.writeByteArray( fileName, blob );

            System.out.println( "\n\n **** NOW TESTING THE NEW AIK ****\n");
            
            // Now, we've written the AIK to disk.  Test to make sure it works.            
            System.out.println( "Reading " + fileName + " ..." );
            byte[] buf = FileUtil.readIntoByteArray( fileName );

            System.out.println( "buf == blob?" + Arrays.equals( buf, blob ) );

            TPM_KEY readKey = new TPM_KEY( buf );
            System.out.println( "Key from file: " + readKey );

            System.out.println( "Loading the key into the TPM ..." );
            int keyHandle = TPMStorageFuncs.TPM_LoadKey( TPMConsts.TPM_KH_SRK,
                buf, srkAuth );
            System.out.println( "keyHandle = 0x"
                + Integer.toHexString( keyHandle ) );

            TPM_PUBKEY pubKey = null;
            try
            {
                System.out.println( "Reading the public key ... " );
                pubKey = TPMStorageFuncs.TPM_GetPubKey( keyHandle, keyAuth );
                System.out.println( "PubKey=" + pubKey );

            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
            }
            
            try
            {
                System.out.println( "Trying TPM_Quote of PCR 0 ... " );
                TPM_QuoteOutput quoteOut = TPMPcrFuncs.TPM_Quote( keyHandle, keyAuth, TPM_SECRET.NULL, 0 );
                System.out.println( "output=" + quoteOut );
                System.out.println( "Verifying signature by software ... " );
                boolean ok = TPMPcrFuncs.verifyQuote( pubKey, quoteOut, TPM_SECRET.NULL );
                System.out.println( "OK? " + ok );
            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
            }

            System.out.println( "\n\n **** NOW TESTING SIGNING OF ARBITRARY DATA (THIS SHOULD FAIL!) ****\n");
            
            try
            {
                System.out.println( "Trying to sign 'Hello World!' ... (THIS SHOULD FAIL)" );
                String helloWorld = "Hello World!";
                byte[] helloBytes = helloWorld.getBytes();

                byte[] sig = TPMStorageFuncs.TPM_SignSHA1OfData( keyHandle,
                    helloBytes, keyAuth );
                System.out.println( "Signature returned (" + sig.length
                    + " bytes): " + ByteArrayUtil.toHexString( sig ) );

                if ( pubKey != null )
                {
                    System.out.println( "Verifying signature ... " );
                    // NOTE: this uses helloWorld not helloWorldHash 
                    // since verifySignature already uses SHA1withRSA
                    boolean signOK = TPMStorageFuncs.TSS_VerifySHA1RSASignature(
                        pubKey, sig, helloBytes );
                    System.out.println( "Signature OK? " + signOK );
                }
            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
                
                System.out.println( "\n" );
                
                System.out.println( "Evicting keyHandle: 0x"
                    + Integer.toHexString( keyHandle ) );
                TPMStorageFuncs.TPM_EvictKey( keyHandle );
                System.out.println( "OK" );
            }

            
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

        TPMToolsUtil.cleanupTPMDriver();
    }

}
