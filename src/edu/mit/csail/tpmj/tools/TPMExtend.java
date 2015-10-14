package edu.mit.csail.tpmj.tools;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.commands.TPM_CreateCounterOutput;
import edu.mit.csail.tpmj.drivers.TPMDriver;
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
public class TPMExtend
{

    public static void usage()
    {
        System.out.println( "Usage: java edu.mit.csail.tpmj.tools.TPMExtend <pcrNum> [data]" );
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
        SwitchParams params = new SwitchParams( args, "pcrNum", "data" );

        int pcrNum = params.getInt( "pcrNum" );

        TPM_NONCE digest = TPMToolsUtil.createTPM_SECRETFromParams( params,
            "data" );

        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMToolsUtil.initDriverFromParams( params );

        try
        {
            TPM_PCRVALUE oldVal = TPMPcrFuncs.TPM_PCRRead( pcrNum );
            System.out.println( "PCR " + pcrNum + ":\nOld value: " + oldVal );

            if ( digest != null )
            {
                System.out.println( "Extending by " + digest + "..." );
                TPMPcrFuncs.TPM_Extend( pcrNum, digest );
                TPM_PCRVALUE newVal = TPMPcrFuncs.TPM_PCRRead( pcrNum );
                System.out.println( "New value: " + newVal );
            }
        }
        catch ( TPMException e )
        {
            TPMToolsUtil.handleTPMException( e );
        }
        
        TPMToolsUtil.cleanupTPMDriver();
    }
}
