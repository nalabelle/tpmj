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
public class TPMCreateCounter
{

    public static void usage()
    {
        System.out.println( "Usage: TPMCreateCounter <ownerPwd> [counterLabel] [counterPwd] \n\n"
            + "If no counter label is specified, it defaults to 'CNTR'.\n"
            + "The counter label must be 4 bytes in length.\n\n"
            + "If no counter password is given, a null password (all-zeros) is used." );
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
        SwitchParams params = new SwitchParams( args, "ownerPwd",
            "counterLabel", "counterPwd" );

        // <owner password>
        TPM_SECRET ownerAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "ownerPwd" );
        
        // Extract the counter label from the command line.
        String labelString = params.getString( "counterLabel", "CNTR" );
        byte[] label = labelString.getBytes();
        if ( label.length != 4 )
        {
            System.out.println( "Error: The counter label must be 4 bytes long!" );
            usage();
        }

        // <counter password>
        TPM_SECRET counterAuth = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "counterPwd" );
        
        if ( counterAuth == null )
        {
            counterAuth = TPM_SECRET.NULL;
            System.out.println( "Using NULL (all zeroes) as counterAuth" );
        }
        
        
        int countID = -1;

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            // Create the counter
            System.out.println( "Creating monotonic counter for label "
                + labelString + "(0x"
                + ByteArrayUtil.toHexString( label ) + ") ..." );

            TPM_CreateCounterOutput ccOut = TPMCounterFuncs.TPM_CreateCounter(
                counterAuth, label, ownerAuth );

            countID = ccOut.getCountID();
            TPM_COUNTER_VALUE ccOutCount = ccOut.getCounterValue();

            System.out.println( "Created Counter ID 0x" + Integer.toHexString( countID ) + "\n" 
                + ccOutCount );
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }
}
