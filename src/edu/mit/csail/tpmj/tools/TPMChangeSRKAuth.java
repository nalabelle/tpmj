package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateCounterOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMAuthFuncs;
import edu.mit.csail.tpmj.funcs.TPMCounterFuncs;
import edu.mit.csail.tpmj.funcs.TPMPcrFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_NONCE;
import edu.mit.csail.tpmj.structs.TPM_PCRVALUE;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 */
public class TPMChangeSRKAuth
{

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMChangeSRKAuth <ownerPwd> <newSRKPwd>\n\n" );
        System.out.println( "NOTE: This tool does NOT have the ability to change whether\n" +
                            "authorization is needed to use the SRK or not. If you took ownership\n" +
                            "with a \"null\" SRK password, then even if you change the SRK password,\n" +
                            "the SRK will still be usable without an authorization session.\n  " +
                            "(However, if you do use authorization, then the new password " +
                            "will be required.)" );
        System.exit( -1 );
    }

    
    public static void main( String[] args )
    {
        if ( args.length < 1 )
        {
            usage();
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "ownerPwd", "newSRKPwd" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {

            System.out.println( "Changing SRK password ..." );
            TPM_SECRET oldAuth = TPMToolsUtil.createTPM_SECRETFromParams(
                params, "ownerPwd" );

            TPM_SECRET newAuth = TPMToolsUtil.createTPM_SECRETFromParams(
                params, "newSRKPwd" );

            TPMAuthFuncs.TPM_ChangeSRKAuth( oldAuth, newAuth );
            System.out.println( "Done." );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }
}
