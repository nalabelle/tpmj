/* 
 *      //\\
 *    ////\\\\    Project Bayanihan
 *   o |.[].|o 
 *  -->|....|->-  Worldwide Volunteer Computing Using Java
 *  o o.o.o.o\<\  
 * -->->->->->-   Copyright 1999, Luis F. G. Sarmenta.
 *   <\<\<\<\<\   All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of the copyright owner nor the names of other 
 *    contributors may be used to endorse or promote products derived from 
 *    this software without specific prior written permission.
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
 * Original author:  Luis F. G. Sarmenta, 1999
 * Repackaged and released under BSD-style license 
 * by Luis F. G. Sarmenta, 2007
 * (Also added var args constructor.)
 */
package bayanihan.util.params;

import java.util.*;

/**
 * Parses a command-line String array with switch options.
 * Switch options are indicated by a String starting with a switchMark
 * ("/" by default).
 * <p>
 * When a switch is encountered on the command-line, the following
 * String (if it exists) is considered the switch's value, and is stored
 * under the switch's name (<em>not</em> including the switchMark).  
 * (A switch and its parameter must be separated by a space.)
 * If a switch is the last argument, or is followed by another switch,
 * then the switch is assigned the value "true", which can be read as a boolean.
 * A boolean switch can be prevented from inadvertently taking the next String
 * as its parameter by following it with an explicit "true" or "false".
 * <p>
 * After all switch expressions have been processed, the sequence of 
 * <em>remaining</em> Strings are treated as in ArrayParams.
 * <p>
 * For more readability, and so that using a "/" after a boolean switch
 * is not necessary, it is recommended that users type switches on the 
 * command-line <em>after</em> all non-switch arguments.
 * <p>
 * NOTE: Default switch changed to "/" by lfgs 20070326.
 */
public class SwitchParams extends ArrayParams
{
    protected Hashtable ht = new Hashtable();

    public String switchMark = "/";

    //////////////////
    // Constructors //
    //////////////////

    public SwitchParams( String[] args )
    {
        super( args );
        initArgs();
    }

    //   public SwitchParams( String[] args, String[] argNames )
    //   {
    //      this( args );
    //      this.argNames = argNames;
    //   }

    public SwitchParams( String[] args, String... argNames )
    {
        super( args, argNames );
        initArgs();
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getString( String name )
    {
        String s = (String) ht.get( name );
        if ( s != null )
        {
            return s;
        }
        else
        {
            return super.getString( name );
        }
    }

    /////////////////
    // new methods //
    /////////////////

    protected boolean isSwitch( String s )
    {
        return ((s != null) && s.startsWith( switchMark ));
    }

    /**
     * Called in constructor after args is initialized; goes through
     * args and finds switches.  If switch is followed by a string
     * that is not a switch, add that to the hash table.  If a switch
     * is found alone, add an empty string (but not null) for that switch.
     * Other non-switch strings not associated with a switch are added
     * to a new array in order of appearance.  (i.e., after calling this method,
     * args will be a new array, and Switch-related strings
     * are only accessible by name, and not through the array.)
     */
    protected void initArgs()
    {
        Vector v = new Vector();
        int i = 0;

        while ( i < args.length )
        {
            String s = args[i]; // get current String arg

            if ( isSwitch( s ) ) // found a switch option
            {
                String name = s.substring( switchMark.length() );
                String value;

                if ( (++i < args.length) // String exists after switch
                    && !isSwitch( args[i] ) ) // and is not a switch
                {
                    value = args[i]; // get switch value
                    i++; // move to String after value
                }
                else
                // next String is a switch
                {
                    value = ""; // current switch gets empty string value, which is interpreted as a boolean true value
                    // Note: do not move i, since we want to 
                    // process next String (or end if 
                    // i = args.length)
                }
                ht.put( name, value );
            }
            else
            // non-switch String arg
            {
                v.addElement( s ); // add to numbered String args
                i++; // move on to next String
            }
        }

        // convert Vector to String array
        String[] newArgs = new String[v.size()];
        i = 0;
        for ( Enumeration e = v.elements(); e.hasMoreElements(); )
        {
            newArgs[i++] = (String) e.nextElement();
        }

        this.args = newArgs;
    }

    /**
     * This is a test routine that demonstrates the use of this class.
     * Try running:<br>
     * <code>java bayanihan.util.params.SwitchParams a b /x hello c d /y h i 
     * /z /p /q true e f g /r false /s </code>
     */
    public static void main( String[] args )
    {
        System.out.println( "Command-line arguments: " );
        for ( int i = 0; i < args.length; i++ )
        {
            System.out.println( "args[" + i + "] = " + args[i] );
        }
        
        String[] argnames =
            { "first", "second", "third" };
        SwitchParams p = new SwitchParams( args, argnames );

        System.out.println( "\nSwitches: " );
        for ( Enumeration e = p.ht.keys(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();
            System.out.println( "getString(" + name + ") = " + p.getString( name ) );
            System.out.println( "getBoolean(" + name + ") = " + p.getBoolean( name ) );
        }
        
        System.out.println( "\nTrying non-existent switches ..." );
        System.out.println( "getString(foo) = " + p.getString( "foo" ) );
        System.out.println( "getBoolean(foo) = " + p.getBoolean( "foo" ) );
        
        System.out.println( "\nArray args: " );
        for ( int i = 0; i < p.args.length; i++ )
        {
            System.out.println( "(" + i + ","
                + p.getString( String.valueOf( i ) ) + ")" );
        }
        System.out.println( "\nNamed array args: " );
        for ( int i = 0; i < argnames.length; i++ )
        {
            System.out.println( "getString(" + argnames[i] + ","
                + p.getString( argnames[i] ) + ")" );
            System.out.println( "getBoolean(" + argnames[i] + ","
                + p.getBoolean( argnames[i] ) + ")" );
        }
    }
}
