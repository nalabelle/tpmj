/*
 * Copyright (c) 2006, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2006
 */ 
package edu.mit.csail.tpmj.util;

import java.util.Arrays;

/**
 * Implements hashCode and equals methods
 * that work correctly with other ByteArrayables.
 * (using java.util.Arrays.hashCode and java.util.Arrays.equals).
 * It is highly recommended that ByteArrayable
 * implementations descend from this class.
 * If not, then they MUST use Arrays.hashCode
 * and Arrays.equals on the result of their toBytes and fromBytes
 * methods.
 * 
 * @author lfgs
 *
 */
public abstract class BasicByteArrayable implements ByteArrayable
{
    /**
     * Returns true if the other object is also a ByteArrayable
     * and has the same bytes, as compared by 
     * <code>Arrays.equals( byte[], byte[] )</code>.
     * <p>
     * Note that this will <b>not</b> return true
     * when compared with a primitive byte[].
     * Even two byte[] instances containing the same bytes
     * do not return true when you call equals on one of them.
     */
    public boolean equals( Object arg0 )
    {
        if ( arg0 instanceof ByteArrayable )
        {
            byte[] argBytes = ((ByteArrayable) arg0).toBytes();
            return Arrays.equals( this.toBytes(), argBytes );
        }
        else
        {
            return false;
        }

    }

    public int hashCode()
    {
        return Arrays.hashCode( this.toBytes() );
    }

    public String toString()
    {
        return this.getClass().getName() + ": "
            + ByteArrayUtil.toPrintableHexString( this );
    }

    public abstract byte[] toBytes();

    public abstract void fromBytes( byte[] source, int offset );

    public static void main( String[] args )
    {
        // testing hash and equals functions
        byte[] a = new byte[20];
        byte[] b = new byte[20];

        System.out.println( "a.hashCode = " + a.hashCode() );
        System.out.println( "b.hashCode = " + b.hashCode() );
        System.out.println( "a.equals(b) = " + a.equals( b ) );

        System.out.println( "Arrays.hashCode( a ) = " + Arrays.hashCode( a ) );
        System.out.println( "Arrays.hashCode( b ) = " + Arrays.hashCode( b ) );
        System.out.println( "Arrays.equals(a,b) = " + Arrays.equals( a, b ) );

        System.out.println( "Comparing ByteArrayStructs ..." );
        ByteArrayStruct aS = new ByteArrayStruct( a );
        ByteArrayStruct bS = new ByteArrayStruct( b );
        System.out.println( "aS.hashCode() = " + aS.hashCode() );
        System.out.println( "bS.hashCode() = " + bS.hashCode() );
        System.out.println( "aS.equals(bS) = " + aS.equals( bS ) );

    }

}
