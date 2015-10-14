/* 
 *      //\\
 *    ////\\\\    Project Bayanihan
 *   o |.[].|o 
 *  -->|....|->-  Worldwide Volunteer Computing Using Java
 *  o o.o.o.o\<\  
 * -->->->->->-   Copyright 1999, 2007 Luis F. G. Sarmenta.
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
 * (Modified to handle hex integers and longs starting with "0x")
 */
package bayanihan.util.params;

import java.util.Vector;

public abstract class Params
{
    public abstract String getString( String name );

    public String getString( String name, String defval )
    {
        String temp = getString( name );
        if ( temp != null )
        {
            return temp;
        }
        else
        {
            return defval;
        }
    }

    public int getInt( String name ) throws NumberFormatException
    {
        int value = 0;
        String s = getString( name );
        if ( s != null )
        {
            if ( s.toLowerCase().startsWith( "0x" ) )
            {
                value = Integer.parseInt( s.substring( 2 ), 16 );
            }
            else
            {
                value = Integer.parseInt( s );
            }
        }
        else
        {
            throw new NumberFormatException();
        }
        return value;
    }

    public int getInt( String name, int defval )
    {
        int value = defval;
        try
        {
            value = getInt( name );
        }
        catch ( NumberFormatException e )
        {
            value = defval;
        }
        return value;
    }

    public long getLong( String name ) throws NumberFormatException
    {
        long value = 0;
        String s = getString( name );
        if ( s != null )
        {
            if ( s.toLowerCase().startsWith( "0x" ) )
            {
                value = Long.parseLong( s.substring( 2 ), 16 );
            }
            else
            {
                value = Long.parseLong( s );
            }
        }
        else
        {
            throw new NumberFormatException();
        }
        return value;
    }

    public long getLong( String name, long defval )
    {
        long value = defval;
        try
        {
            value = getLong( name );
        }
        catch ( NumberFormatException e )
        {
            value = defval;
        }
        return value;
    }

    public float getFloat( String name ) throws NumberFormatException
    {
        float value = 0;
        String s = getString( name );
        if ( s != null )
        {
            value = Float.valueOf( s ).floatValue();
        }
        else
        {
            throw new NumberFormatException();
        }
        return value;
    }

    public float getFloat( String name, float defval )
    {
        float value = defval;
        try
        {
            value = getFloat( name );
        }
        catch ( NumberFormatException e )
        {
            value = defval;
        }
        return value;
    }

    public double getDouble( String name ) throws NumberFormatException
    {
        double value = 0.0;
        String s = getString( name );
        if ( s != null )
        {
            value = Double.valueOf( s ).doubleValue();
        }
        else
        {
            throw new NumberFormatException();
        }
        return value;
    }

    public double getDouble( String name, double defval )
    {
        double value = 0.0;
        try
        {
            value = getDouble( name );
        }
        catch ( NumberFormatException e )
        {
            value = defval;
        }
        return value;
    }

    /**
     * Returns false if getString( name ) is null
     * or is "false", "off", or "0" (ignoring case);
     * returns true for any other non-null value (including
     * empty string)
     * 
     * @param name
     * @param defval
     * @return
     */
    public boolean getBoolean( String name )
    {
        return getBoolean( name, false );
    }

    /**
     * Returns defval if getString( name ) is null;
     * returns false if string is "false", "off", or "0" (ignoring case);
     * returns true for any other non-null value (including
     * empty string)
     * 
     * @param name
     * @param defval
     * @return
     */
    public boolean getBoolean( String name, boolean defval )
    {
        boolean value = defval;
        String s = getString( name );
        if ( s != null )
        {
            if ( s.equalsIgnoreCase( "false" )
                || s.equalsIgnoreCase( "off" )
                || s.equalsIgnoreCase( "0" ) )
            {
                value = false;
            }
            else // if ( s.equalsIgnoreCase( "true" ) || s.equalsIgnoreCase( "on" ) )
            {
                // Any other non-null string (including empty string) will be interpreted as true
                value = true;
            }
        }
        return value;
    }

    public String[] getStringArray( String name )
    {
        if ( getString( name + "0" ) != null )
        {
            Vector v = new Vector();
            int i = 0;
            String s = null;

            while ( (s = getString( name + i )) != null )
            {
                v.addElement( s );
                i++;
            }

            String[] res = new String[v.size()];

            v.copyInto( res );

            return res;
        }
        else
        {
            return null;
        }
    }

    /////////////////////
    // Utility methods //
    /////////////////////

    /**
     * If o is Parameterizable, and p is not null,
     * calls o.parameterize( p )
     * to parameterize o according to the p Params object.
     * Does nothing if o is not Parameterizable or p is null.
     */
    public static void parameterize( Object o, Params p )
    {
        if ( (o instanceof Parameterizable) && (p != null) )
        {
            ((Parameterizable) o).parameterize( p );
        }
    }

    /**
     * If o is Parameterizable, calls parameterize( o, this )
     * to parameterize o according to this Params object.
     * Does nothing if o is not Parameterizable.
     */
    public void parameterizeObject( Object o )
    {
        Params.parameterize( o, this );
    }
}
