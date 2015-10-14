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

public class MultiParams extends HashParams
{
    protected Vector v = new Vector();

    //////////////////
    // Constructors //
    //////////////////

    public MultiParams()
    {
        super();
    }

    public MultiParams( Hashtable ht )
    {
        super( ht );
    }

    public MultiParams( Params p )
    {
        super();
        this.addParams( p );
    }

    public MultiParams( Hashtable ht, Params p )
    {
        super( ht );
        this.addParams( p );
    }

    /**
     * Creates a combination of two Params; p1 takes priority over
     * p2.
     */
    public MultiParams( Params p1, Params p2 )
    {
        super();
        if ( p1 != null )
            this.addParams( p1 );
        if ( p2 != null )
            this.addParams( p2 );
    }

    /**
     * Creates a combination of 3 Params; p1 takes priority over
     * p2 which takes priority over p3.
     */
    public MultiParams( Params p1, Params p2, Params p3 )
    {
        super();
        if ( p1 != null )
            this.addParams( p1 );
        if ( p2 != null )
            this.addParams( p2 );
        if ( p3 != null )
            this.addParams( p3 );
    }

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method first checks the Hashtable for name, and then
     * checks the added Params in the order in which they are added.
     */
    public String getString( String name )
    {
        String s = super.getString( name );
        if ( s == null )
        {
            // not in hashtable, so search Vector in order
            Enumeration e = v.elements();
            while ( (s == null) && e.hasMoreElements() )
            {
                s = ((Params) e.nextElement()).getString( name );
            }
        }
        return s;
    }

    /////////////////
    // new methods //
    /////////////////

    /**
     * Add Params p to the end of the list (lowest priority).
     */
    public void addParams( Params p )
    {
        this.v.addElement( p );
    }

    /**
     * Add Params p to the beginning of the list (highest
     * priority except for direct hashed-in entries).
     */
    public void insertParams( Params p )
    {
        this.v.insertElementAt( p, 0 );
    }
}
