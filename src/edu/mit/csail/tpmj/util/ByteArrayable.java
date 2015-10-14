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

/**
 * Interface for a class that can represent itself as
 * a primitive byte array.  Note that this is different
 * from Serializable classes.
 * 
 * @author lfgs
 */
public interface ByteArrayable
{
    /**
     * Returns a new byte array representing the data in the object.
     * (Note: this should generally return a clone of the data.)
     * @return
     */
    public byte[] toBytes();
    
    /**
     * Initializes/replaces values in the object from
     * a byte array representation of the object.
     * <p>
     * Notes: 
     * <p>
     * 1) This should make a copy of the data, not
     * just point to the byte array itself.
     * <p>
     * 2) This method is defined in this interface 
     * to enforce the implementation of this method,
     * as well as allow generic tools to potentially dynamically
     * instantiate a class from a classname and set its value
     * from a byte[] (e.g., in a serialization setting).
     * 
     * Typically, though, a ByteArrayable class should
     * have a constructor which takes a byte[] and calls
     * this.fromBytes.  The constructor should be the
     * preferred way of setting value of the structure,
     * if possible. 
     * 
     * The offset is taken as a parameter to allow reading
     * fields from a structure represented as a byte array.
     * Note that there is no length parameter because the object
     * should be allowed to determine its own length
     * (i.e., we don't want to require the caller to know,
     * and it some cases, it can't be known)
     * 
     * @param source byte array to read from
     * @param offset offset to read from
     */
    public void fromBytes( byte[] source, int offset );
}
