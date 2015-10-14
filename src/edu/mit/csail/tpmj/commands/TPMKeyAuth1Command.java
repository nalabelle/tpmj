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
package edu.mit.csail.tpmj.commands;

/**
 * Extend this class for Auth1 commands that have
 * one keyHandle after the ordinal.
 * This class defines the keyHandle.
 * Remember that the keyHandle is usually <b>not</b>
 * included in the getInParamsForAuthDigest.
 * @author lfgs
 */
public abstract class TPMKeyAuth1Command extends TPMAuth1Command
{
    public static final int KEYHANDLE_OFFSET = 10;

    public int keyHandle;

    public TPMKeyAuth1Command( int ordinal, int keyHandle )
    {
        super( ordinal );
        this.setKeyHandle( keyHandle );
    }

    public int getKeyHandle()
    {
        return keyHandle;
    }

    public void setKeyHandle( int keyHandle )
    {
        this.keyHandle = keyHandle;
    }
}