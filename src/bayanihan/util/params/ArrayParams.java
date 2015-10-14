/* 
 *      //\\
 *    ////\\\\	  Project Bayanihan
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
 */
package bayanihan.util.params;

public class ArrayParams extends Params
{
    public static final int INVALID_INDEX = -1;

    protected String[] args;
    protected String[] argNames = null;
    protected int base = 0;

    //////////////////
    // Constructors //
    //////////////////

    /**
     * Needed for HORB compatibility; does nothing.
     */
    public ArrayParams()
    {
    }

    public ArrayParams( String[] args )
    {
        this.args = args;
    }

    public ArrayParams( String[] args, String... argNames )
    {
        this( args );
        this.argNames = argNames;
    }

    /**
     * Constructor accepting array of {argName,arg} pairs.
     */
    public ArrayParams( String[][] argPairs )
    {
        this.argNames = new String[argPairs.length];
        this.args = new String[argPairs.length];

        for ( int i = 0; i < argPairs.length; i++ )
        {
            this.argNames[i] = argPairs[i][0];
            this.args[i] = argPairs[i][1];
        }
    }

    //////////////////////
    // Accessor methods //
    //////////////////////

    /**
     * Returns args array, regardless of base
     */
    public String[] getAllArgs()
    {
        return this.args;
    }

    /**
     * Returns args array, starting only from base.  (This uses
     * this.getLength() and this.getString(int).)
     */
    public String[] getArgs()
    {
        int length = this.getLength();
        String[] retArgs = new String[length];

        for ( int i = 0; i < length; i++ )
        {
            retArgs[i] = this.getString( i );
        }

        return retArgs;
    }

    /**
     * Returns argNames array, regardless of base
     */
    public String[] getAllArgNames()
    {
        return this.argNames;
    }

    /**
     * Returns argNames array, starting only from base.  (This uses
     * this.getLength() and this.getString(int).)
     */
    public String[] getArgNames()
    {
        int length = this.getLength();
        String[] retArgNames = new String[length];

        for ( int i = 0; i < length; i++ )
        {
            int index = i + base;
            if ( index < argNames.length )
            {
                retArgNames[i] = argNames[index];
            }
            else
            {
                retArgNames[i] = null;
            }
        }
        return retArgNames;
    }

    public void setBase( int base )
    {
        this.base = base;
    }

    public void setArgNames( String[] argNames )
    {
        this.argNames = argNames;
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getString( String name )
    {
        int i = INVALID_INDEX;
        String value = null;

        try
        // if name is a number
        {
            i = Integer.valueOf( name ).intValue();
        }
        catch ( NumberFormatException e )
        {
            i = INVALID_INDEX;
        }

        if ( i == INVALID_INDEX )
        {
            // name is not a number; try to match argName
            i = findArgName( name );
        }

        if ( i >= 0 )
        {
            return this.getString( i );
        }
        else
        {
            return null;
        }
    }

    /////////////////
    // new methods //
    /////////////////

    public int getLength()
    {
        return args.length - base;
    }

    public String getString( int i )
    {
        int index = i + base;
        if ( index < args.length )
        {
            return args[index];
        }
        else
        {
            return null;
        }
    }

    public int findArgName( String name )
    {
        int i = 0;

        if ( argNames == null )
        {
            return INVALID_INDEX;
        }

        while ( (i < argNames.length) && !argNames[i].equals( name ) )
        {
            i++;
        }

        if ( i == argNames.length )
        {
            i = INVALID_INDEX;
        }

        return i;
    }

    /**
     * Sets the String value of the param
     * given by name, if that exists.
     */
    public void setString( String name, String newVal )
    {
        int i = INVALID_INDEX;

        try
        // if name is a number
        {
            i = Integer.valueOf( name ).intValue();
        }
        catch ( NumberFormatException e )
        {
            i = INVALID_INDEX;
        }

        if ( i == INVALID_INDEX )
        {
            // name is not a number; try to match argName
            i = findArgName( name );
        }

        if ( i != INVALID_INDEX )
        {
            args[i] = newVal;
        }
    }

}
