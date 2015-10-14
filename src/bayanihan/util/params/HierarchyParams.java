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
 */
package bayanihan.util.params;

import java.util.*;
import java.io.*;

public class HierarchyParams extends ArrayParams
{
    //////////////////
    // Constructors //
    //////////////////

    /**
     * Needed for HORB compatibility; does nothing.
     */
    public HierarchyParams()
    {
    }

    public HierarchyParams( String src )
    {
        processFormattedData( src );
    }

    public HierarchyParams( InputStream in ) throws IOException
    {
        DataInputStream d = new DataInputStream( in );
        String s = null;
        StringBuffer buffer = new StringBuffer();
        do
        {
            s = d.readLine();
            if ( s != null )
                buffer.append( s + "\n" );
        }
        while ( s != null );

        processFormattedData( buffer.toString() );
    }

    public HierarchyParams( String[] args )
    {
        super( args );
    }

    public HierarchyParams( String[] args, String[] argNames )
    {
        super( args, argNames );
    }

    public HierarchyParams( String[][] argPairs )
    {
        super( argPairs );
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public String getString( String name )
    {
        return super.getString( name );
    }

    /////////////////
    // new methods //
    /////////////////

    public HierarchyParams getSubHierarchyParams( String base )
    {
        int start = -1;

        for ( int i = 0; i < argNames.length && start == -1; i++ )
        {
            if ( argNames[i].startsWith( base ) )
                start = i;
        }

        if ( start != -1 )
        {
            int baselen = base.length();
            int end = start;

            while ( end < argNames.length && argNames[end].startsWith( base ) )
                end++;

            String[] temp1 = new String[end - start];
            String[] temp2 = new String[end - start];
            for ( int i = 0; i < temp1.length; i++ )
            {
                temp1[i] = args[i + start];
                temp2[i] = argNames[i + start].substring( baselen + 1 );
            }
            return new HierarchyParams( temp1, temp2 );
        }
        else
            return null;
    }

    protected void processFormattedData( String src )
    {
        Vector baseName = new Vector();
        Vector names = new Vector();
        Vector values = new Vector();

        int ptr = 0;
        int begin = 0;
        int nestLevel = 0;
        boolean whitespace = false;
        boolean foundEquals = false;
        String lastToken = null;
        String lastBaseName = null;
        String paramName = null;
        char c;

        if ( src == null )
            return;

        while ( ptr < src.length() )
        {
            foundEquals = false;
            whitespace = true;

            do
            {
                c = src.charAt( ptr );

                whitespace = false;

                if ( Character.isSpace( c ) )
                    whitespace = true;
                else if ( c == '=' )
                {
                    whitespace = true;
                    foundEquals = true;
                }
                else if ( c == '{' )
                {
                    if ( lastBaseName != null )
                    {
                        baseName.addElement( lastBaseName );
                        lastBaseName = null;
                    }
                    whitespace = true;
                }
                else if ( c == '}' )
                {
                    if ( baseName.size() > 0 )
                    {
                        baseName.removeElementAt( baseName.size() - 1 );
                    }
                    whitespace = true;
                }
            }
            while ( whitespace && ++ptr < src.length() );

            begin = ptr;
            if ( foundEquals && paramName == null )
                paramName = lastToken;

            if ( ptr < src.length() )
            {
                do
                {
                    c = src.charAt( ptr );

                    whitespace = false;

                    if ( Character.isSpace( c ) )
                        whitespace = true;
                    else if ( c == '=' )
                        whitespace = true;
                    else if ( c == '{' )
                        whitespace = true;
                    else if ( c == '}' )
                        whitespace = true;
                }
                while ( !whitespace && ++ptr < src.length() );

                lastToken = src.substring( begin, ptr );
                lastBaseName = lastToken;

                if ( paramName != null )
                {
                    int l = baseName.size();
                    String temp = "";
                    if ( baseName.size() > 0 )
                    {
                        for ( int i = 0; i < l; i++ )
                        {
                            temp += (String) (baseName.elementAt( i ));
                            temp += ".";
                        }
                    }
                    temp += paramName;

                    names.addElement( temp );
                    values.addElement( lastToken );
                    paramName = null;
                }
            }
        }

        argNames = new String[names.size()];
        args = new String[values.size()];

        for ( int i = 0; i < names.size(); i++ )
        {
            argNames[i] = (String) (names.elementAt( i ));
            args[i] = (String) (values.elementAt( i ));
        }
    }
}
