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
package edu.mit.csail.tpmj;

import edu.mit.csail.tpmj.structs.TPMInputStruct;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;

public class TPMException extends Exception
{
    public static final int NO_RETURN_CODE = -1;

    protected TPMInputStruct input;
    protected TPMOutputStruct output;

    public TPMException( Exception e )
    {
        super( e );
    }

    public TPMException( String s )
    {
        super( s );
    }

    public TPMException( TPMInputStruct input, TPMOutputStruct output,
        Exception e )
    {
        super( e );
        this.input = input;
        this.output = output;
    }

    public TPMException( TPMInputStruct input, Exception e )
    {
        this( input, null, e );
    }

    public TPMException( TPMInputStruct input, TPMOutputStruct output, String s )
    {
        super( s );
        this.input = input;
        this.output = output;
    }

    public TPMException( TPMInputStruct input, TPMOutputStruct output )
    {
        super();
        this.input = input;
        this.output = output;
    }

    public TPMException( TPMInputStruct input, String s )
    {
        this( input, null, s );
    }

    public TPMException( TPMInputStruct input )
    {
        this( input, (TPMOutputStruct) null );
    }

    public TPMInputStruct getTPMInputStruct()
    {
        return this.input;
    }

    public TPMOutputStruct getTPMOutputStruct()
    {
        return this.output;
    }

    public int getReturnCode()
    {
        if ( output != null )
        {
            return this.output.getReturnCode();
        }
        else
        {
            return TPMException.NO_RETURN_CODE;
        }
    }
}
