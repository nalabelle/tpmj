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

import java.util.HashMap;

import edu.mit.csail.tpmj.util.stats.Stopwatch;

public class Debug
{
    public static boolean ALL_DEBUG_OFF = false;
    public static boolean DEFAULT_DEBUG_ON = false;
    private static int MY_STACKPOS = 3;
    private static int MY_CALLER_STACKPOS = 4;
    private static HashMap<String,Boolean> onMap = new HashMap<String,Boolean>();

    public static void setDebugOn( Class c, boolean on )
    {
        setDebugOn( c.getName(), on );
    }
    
    public static boolean isDebugOn( Class c )
    {
        return isDebugOn( c.getName() );
    }
    
    public static void setThisClassDebugOn( boolean on )
    {
        String callingClass = getCaller();
        setDebugOn( callingClass, on );
    }

    public static void setDebugOn( String className, boolean on )
    {
        if ( on )
        {
            System.out.println( "setDebugOn " + className );
        }
        onMap.put( className, on );
    }

    public static boolean isDebugOn( String className )
    {
        Boolean b = onMap.get( className );
        if ( b == null )
        {
            return DEFAULT_DEBUG_ON;
        }
        else
        {
            return b.booleanValue();
        }
    }

    private static boolean isCallerDebugOn()
    {
        // need to inc the stackpos since we went down one level
        // by calling isCallerDebugOn
        return isDebugOn( getCallingClass(MY_CALLER_STACKPOS + 1) );
    }
    
    private static String getCallingClass( int stackPos )
    {
        return Thread.currentThread().getStackTrace()[stackPos].getClassName();
    }

    private static String getCaller()
    {
        return Thread.currentThread().getStackTrace()[MY_CALLER_STACKPOS].getClassName();
    }
    
    private static String getCurrentMethod()
    {
        return Thread.currentThread().getStackTrace()[MY_STACKPOS].getMethodName();
    }

    
    // printing/logging commands
    
    public static boolean isDebugOn()
    {
        String callingClass = getCaller();
//        System.out.println( "isThisClassDebugOn " + callingClass );
        boolean isThisClassOn = isDebugOn( callingClass );
        return ( !ALL_DEBUG_OFF && isThisClassOn );
    }
    
    public static void print( Object... objs )
    {
        // NOTE: It is tempting to call this.isDebugOn() here,
        // but does does NOT work because it changes the stack positions.
        
        if ( !ALL_DEBUG_OFF && isCallerDebugOn() )
        {
            System.out.print( concatToString( objs ) );
        }
    }
    
    public static void println( Object... objs  )
    {
        if ( !ALL_DEBUG_OFF && isCallerDebugOn() )
        {
            System.out.println( concatToString( objs ) );
        }
    }
    
    /**
     * Used by print and println to convert Object list to string
     * to be printed.  Uses toString, except for byte[], in which
     * case it uses ByteArrayUtil.toPrintableHexString. 
     * 
     * @param objs
     * @return
     */
    public static String concatToString( Object...  objs )
    {
        String s = "";
        if ( objs == null )
        {
            return null;
        }
        
        for ( Object o: objs )
        {
            String sub = null;
            if ( o == null )
            {
                sub = "[NULL]";
            }
            else if ( o instanceof byte[] ) 
            {
                sub = ByteArrayUtil.toPrintableHexString( (byte[]) o );
            }
            else
            {
                sub = o.toString();
            }
            s = s + sub;
        }
        
        return s;
    }
    
    // Unit test
    public static void main( String[] args )
    {
        Stopwatch sw = new Stopwatch();
        
        System.out.println( "Debug is off" );
        byte[] arr = new byte[4096];
        
        System.out.println( "Debug.isDebugOn() returns " + Debug.isDebugOn() );
        
        System.out.println( "Debug using object list" );
        sw.start();
        for ( int turns = 0; turns < 10; turns++ )
        {
            // note use of comma before arr, not plus
            Debug.println( "Turn " + turns + ", obj = ", arr );
        }
        sw.stop();
        // This should take very little time
        System.out.println( "Time = " + sw.getTime() );
        
        System.out.println( "Debug using + " );
        sw.reset();
        sw.start();
        for ( int turns = 0; turns < 10; turns++ )
        {
            // this performs the toPrintableHexString function even if Debug is OFF
            Debug.println( "Turn " + turns + ", obj = " + ByteArrayUtil.toPrintableHexString( arr ) );
        }
        sw.stop();
        // This should take a much longer time
        System.out.println( "Time = " + sw.getTime() );
        
        Debug.setThisClassDebugOn( true );
        
        System.out.println( "Debug On" );

        System.out.println( "Debug.isDebugOn() returns " + Debug.isDebugOn() );
        
        System.out.println( "Debug using object list" );
        sw.reset();
        sw.start();
        for ( int turns = 0; turns < 10; turns++ )
        {
            // note use of comma before arr, not plus
            Debug.println( "Turn " + turns + ", obj = ", arr );
        }
        sw.stop();
        System.out.println( "Time = " + sw.getTime() );
        
    }
}
