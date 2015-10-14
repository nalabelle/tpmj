/*
 * Copyright (c) 2006-2007, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006-2007
 */
package edu.mit.csail.tpmj.util;

import java.io.IOException;
import java.math.BigInteger;

import bayanihan.util.params.SwitchParams;
import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.drivers.TPMDriver;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_RESULT;
import edu.mit.csail.tpmj.structs.TPM_SECRET;

public class TPMToolsUtil
{
    public static final String[] NO_ARGS = new String[0];

    public static void handleTPMException( TPMException e, boolean withStackTrace )
    {
        System.out.println( "TPM Exception: " + e );
        System.out.println( "Occured on input: " + e.getTPMInputStruct() );
        System.out.println( "Output (if any): " + e.getTPMOutputStruct() );
        System.out.println( "Return Code (if any): " + e.getReturnCode() + " ("
            + TPM_RESULT.getErrorName( e.getReturnCode() ) + ")" );
        if ( withStackTrace )
        {
            System.out.println( "Stack trace: " );
            e.printStackTrace();
        }
    }

    public static void handleTPMException( TPMException e )
    {
        handleTPMException( e, false );
    }
    
    
    //    public static void handleTPMErrorReturnCodeException(
    //        TPMErrorReturnCodeException e )
    //    {
    //        System.out.println( "TPM Returned Error Code: "
    //            + Integer.toHexString( e.getReturnCode() ) );
    //        // actually, handleTPMException is pretty good already,
    //        // I just include this code here to show an example of
    //        // specific handling of returncode errors.
    //        System.out.println( "Generic TPM Exception info follows ..." );
    //        handleTPMException( e );
    //    }
    
    /**
     * This method can be called within command-line programs to allow
     * the initialization of the driver object to be controllable by
     * command-line switches.  The default behavior is to check
     * for the -D switch, and create a DebugginTPMDriverWrapper around
     * the tpmDriver. 
     */
    public static TPMDriver initDriverFromParams( SwitchParams params )
    {
        if ( params.getBoolean( "D" ) )
        {
            // This causes the TPMDriverFactory to create a DebuggingTPMDriver wrapper
            // when it does create the driver.
            Debug.setDebugOn( TPMDriver.class, true );
        }
        
        // Initialize the TPM driver
        TPMDriver tpmDriver = TPMUtilityFuncs.getTPMDriver();
        return tpmDriver;
    }
    
    /**
     * Calls TPMUtilityFuncs.cleanupTPMDriver()
     */
    public static void cleanupTPMDriver()
    {
        TPMUtilityFuncs.cleanupTPMDriver();
    }
    
    public static TPM_SECRET createTPM_SECRETFromParams( SwitchParams params, String argName )
    {
        String authString = params.getString( argName );
        
        return convertAuthString( authString, argName );
    }
    
    /**
     * Converts a given String, and prints out the original and encoded versions.
     * 
     * @param authString
     * @param ifxStyle
     * @param label  
     * @return
     */
    public static TPM_SECRET convertAuthString( String authString, String label )
    {
        TPM_SECRET secret = createTPM_SECRETFromPrefixedString( authString );
        String encodingStyle = getPasswordEncodingStyle( authString );
        
        System.out.println( label + " = " + authString + ", Encoded (" + encodingStyle +") = " + secret );
        
        return secret;
    }

    /**
     * Creates a TPM_SECRET from a string with a prefix.
     * This can be used for handling command-line passwords.
     * Possible special forms:
     * -i = "Infineon-style" = hash of UTF16LE without null terminator (default)
     * -I = "Infineon-style" = hash of UTF16LE with null terminator
     * -T = "Strict TSS style" = hash of UTF16BE with null terminator
     * -p = "Plain IBM tpm tools style" = hash of 8-bit ASCII without null terminator
     * -P = "Plain IBM tpm tools style" = hash of 8-bit ASCII with null terminator
     * -null = null (no authorization)
     * "" = empty string (all zeroes)
     * -zero = empty string (all zeroes)
     * -B64 = Base-64 (for use with Vista's password backup file)
     * 0x = Hex
     * <p>
     */
    public static TPM_SECRET createTPM_SECRETFromPrefixedString( String password )
    {
        if ( ( password == null ) || password.equals( "-null" ) )
        {
            return null;
        }
        TPM_SECRET secret = null;
        

        if ( ( password.length() == 0 )|| password.equals( "-zero" ) )
        {
            return TPM_SECRET.NULL;
        }
        if ( password.toLowerCase().startsWith( "-p" ) )
        {
            if ( password.startsWith( "-P" ) )
            {
                password = password + "\0"; // add null terminator
            }
            password = password.substring( 2 );
            secret = TPMToolsUtil.createTPM_SECRETFromStringIBMStyle( password );
        }
        else if ( password.startsWith( "-B64" ) )
        {
            password = password.substring( 4 );
            byte[] buf = null;
            try
            {
                buf = new sun.misc.BASE64Decoder().decodeBuffer( password );
                System.out.println( ByteArrayUtil.toHexString( buf ));
                secret = new TPM_SECRET( buf );
            }
            catch ( IOException e )
            {
                System.err.println( "Error decoding Base64 string.  Using null as password." );
                secret = null;
            }
        }
        else if ( password.startsWith( "0x" ) )
        {
            password = password.substring( 2 );
            byte[] bytes = ByteArrayUtil.parseHexString( password );
            secret = new TPM_SECRET( bytes );
        }
        else if ( password.startsWith( "-T" ) )
        {
            password = password.substring( 2 );
            secret = TPMToolsUtil.createTPM_SECRETFromStringStrictTSSStyle( password );
        }
        else // if ( password.startsWith( "-i" ) )
        {
            if ( password.toLowerCase().startsWith( "-i" ) )
            {
                if ( password.startsWith( "-I" ) )
                {
                    password = password + "\0";
                }
                password = password.substring( 2 );
            }
            
            secret = TPMToolsUtil.createTPM_SECRETFromStringInfineonStyle( password );
        }
        
        return secret;
    }

    /**
     * Returns which style
     * -i = "Infineon-style" = hash of UTF16LE without null terminator (default)
     * -I = "Infineon-style" = hash of UTF16LE with null terminator (default)
     * -T = "Strict TSS style" = hash of UTF16BE with null terminator
     * -p = "Plain IBM tpm tools style" = hash of 8-bit ASCII without null terminator
     * -P = "Plain IBM tpm tools style" = hash of 8-bit ASCII with null terminator
     * -null = null (no authorization)
     * "" = empty string (all zeroes)
     * -B64 = Base-64 (for use with Vista's password backup file)
     * -0x = Hex
     * <p>
     */
    private static String getPasswordEncodingStyle( String password )
    {
        if ( ( password == null ) || password.equals( "-null" ) )
        {
            return "NULL [no authorization]";
        }
        

        if ( password.length() == 0 )
        {
            return "All zeros";
        }
        if ( password.toLowerCase().startsWith( "-p" ) )
        {
            if ( password.startsWith( "-P" ) )
            {
                return "SHA1 of Plain ASCII with null terminator";
            }
            else
            {
                return "SHA1 Plain ASCII without null terminator";
            }
        }
        else if ( password.startsWith( "-B64" ) )
        {
            return "Base64";
        }
        else if ( password.startsWith( "0x" ) )
        {
            return "Hex";
        }
        else if ( password.startsWith( "-T" ) )
        {
            return "Strict TSS (SHA1 of UTF16BE with null Terminator)";
        }
        else // if ( password.startsWith( "-i" ) )
        {
            String ret = "Infineon/Vista (SHA1 of UTF16LE without null terminator";
            if ( password.toLowerCase().startsWith( "-i" ) )
            {
                if ( password.startsWith( "-I" ) )
                {
                    ret = "SHA1 of UTF16LE with null terminator";
                }
            }
            return ret;
        }
    }
    
    
//    /**
//     * Encodes the password string, with option to use Infineon-style encoding.
//     * If password is null, then returns null.
//     * If password is zero-length string, returns TPM_SECRET.NULL
//     * 
//     * @param password
//     * @param ifxStyle
//     * @return
//     */
//    public static TPM_SECRET createTPM_SECRETFromString( String password,
//        boolean ifxStyle )
//    {
//        if ( password == null )
//        {
//            return null;
//        }
//        else if ( password.length() <= 0 )
//        {
//            return TPM_SECRET.NULL;
//        }
//        else if ( ifxStyle )
//        {
//            return TPMToolsUtil.createTPM_SECRETFromStringInfineonStyle( password );
//        }
//        else
//        {
//            return TPMToolsUtil.createTPM_SECRETFromStringIBMStyle( password );
//        }
//    }

    /**
     * Currently, just does a straight SHA-1 hash of the ASCII byte array
     * of the string (no null terminator).
     * This is what IBM's tpm-3.0.3 code does.
     * 
     * @param password
     * @return
     */
    private static TPM_SECRET createTPM_SECRETFromStringIBMStyle( String password )
    {
        // use password.charAt instead of using password.toBytes() just in case password.toBytes() returns 2 bytes per char (possible on some platforms)
        byte[] passwordBytes = new byte[password.length()];
        for ( int i = 0; i < password.length(); i++ )
        {
            passwordBytes[i] = (byte) password.charAt( i );
        }
        byte[] hash = CryptoUtil.computeSHA1Hash( passwordBytes );
    
        return new TPM_SECRET( hash );
    }

    /**
     * Does a SHA-1 of the little-endian Unicode-16 of the password (no null terminator).
     * This is what Infineon's TPM software stack for Windows does,
     * and what Vista does also.
     * 
     * @param password
     * @return
     */
    private static TPM_SECRET createTPM_SECRETFromStringInfineonStyle(
        String password )
    {
        // use password.charAt instead of using password.toBytes() just in case password.toBytes() returns 2 bytes per char (possible on some platforms)
        byte[] unicodeBytes = new byte[password.length() * 2];
        for ( int i = 0; i < password.length(); i++ )
        {
            unicodeBytes[(i * 2)] = (byte) password.charAt( i );
        }
        byte[] hash = CryptoUtil.computeSHA1Hash( unicodeBytes );
    
        return new TPM_SECRET( hash );
    }

    /**
     * Does a SHA-1 of the big-endian Unicode-16 of the password WITH 16-bit null terminator included.
     * This is what the TSS spec seems to specify, but which doesn't seem to be followed
     * by Vista or most other TSS stacks.
     * 
     * @param password
     * @return
     */
    private static TPM_SECRET createTPM_SECRETFromStringStrictTSSStyle(
        String password )
    {
        // use password.charAt instead of using password.toBytes() just in case password.toBytes() returns 2 bytes per char (possible on some platforms)
        byte[] unicodeBytes = new byte[password.length() * 2 + 2];
        for ( int i = 0; i < password.length(); i++ )
        {
            unicodeBytes[(i * 2) + 1] = (byte) password.charAt( i );
        }
        byte[] hash = CryptoUtil.computeSHA1Hash( unicodeBytes );
    
        return new TPM_SECRET( hash );
    }
    
    
    public static void main( String[] args )
    {
        TPM_SECRET secret = null;
        
        System.out.println( "Testing ... " );
        secret = TPMToolsUtil.convertAuthString( "tpmowner", "test" );
        secret = TPMToolsUtil.convertAuthString( "-itpmowner", "test" );
        secret = TPMToolsUtil.convertAuthString( "-Itpmowner", "test" );
        secret = TPMToolsUtil.convertAuthString( "-ptpmowner", "test" );
        secret = TPMToolsUtil.convertAuthString( "-Ptpmowner", "test" );
        secret = TPMToolsUtil.convertAuthString( "-null", "test" );
        secret = TPMToolsUtil.convertAuthString( null, "null" );
        secret = TPMToolsUtil.convertAuthString( "", "empty" );
        secret = TPMToolsUtil.convertAuthString( "0x1234567890ABCDEF12341234567890ABCDEF1234", "hex" );
        secret = TPMToolsUtil.convertAuthString( "0xFEDCBA9876543210FEDCBA9876543210FEDCBA98", "hex" );
        secret = TPMToolsUtil.convertAuthString( "-B64TdtvZcTWksq249+LdUYiiFVXiUY=", "Base64 of Vista hash of 'tpmowner'" );
        
        System.out.println( "\nTesting command-line param ..." );
        SwitchParams params = new SwitchParams( args, "pwd" );
        secret = TPMToolsUtil.createTPM_SECRETFromParams( params, "pwd" );
    }
}
