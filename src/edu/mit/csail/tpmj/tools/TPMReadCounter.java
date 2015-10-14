package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateCounterOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMCounterFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_COUNTER_VALUE;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

/**
 */
public class TPMReadCounter
{

    public static void usage()
    {
        System.out.println( "Usage: TPMReadCounter <counterID>\n\n"
            + "- counterID is the counter handle number (not the label).\n" );
        System.exit( -1 );
    }

    public static void main( String[] args )
    {
        if ( args.length == 0 )
        {
            usage();
        }

        // Parse command-line arguments
        System.out.println( "\nParsing command-line arguments ...\n" );
        SwitchParams params = new SwitchParams( args, "counterID", "counterPwd" );

        int countID = params.getInt( "counterID" );

        // <counter password>
        TPM_SECRET counterAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "counterPwd" );
        
        if ( counterAuth == null )
        {
            counterAuth = TPM_SECRET.NULL;
            System.out.println( "Using NULL (all zeroes) as counterAuth" );
        }

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            System.out.println( "Reading counter 0x"
                + Integer.toHexString( countID ) );
            TPM_COUNTER_VALUE counterValue = TPMCounterFuncs.TPM_ReadCounter( countID );
            System.out.println( "Output: " + counterValue );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }
}
