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

public class PropertyParams extends Params
{
    protected Properties props;

    //////////////////
    // Constructors //
    //////////////////

    public PropertyParams( Properties props )
    {
        this.props = props;
    }

    public PropertyParams( InputStream in ) throws IOException
    {
        this.props = new Properties();
        this.props.load( in );
        in.close();
    }

    public PropertyParams( String fileName ) throws IOException
    {
        this( new FileInputStream( fileName ) );
    }

    ////////////////////////////////////
    // Abstract method implementation //
    ////////////////////////////////////

    public String getString( String name )
    {
        return this.props.getProperty( name );
    }

    public ArrayParams toArrayParams()
    {
        int i = 0;
        String[] args = new String[props.size()];
        String[] argNames = new String[props.size()];

        for ( Enumeration e = props.propertyNames(); e.hasMoreElements(); )
        {
            argNames[i] = (String) e.nextElement();
            args[i] = props.getProperty( argNames[i] );
            i++;
        }

        return new ArrayParams( args, argNames );
    }
}
