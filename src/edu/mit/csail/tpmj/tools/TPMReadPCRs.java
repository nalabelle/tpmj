/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
 * Parts, copyright (c) 2007, Thomas Müller, xnos Internet Services (xnos.org)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of MIT nor xnos nor the names of its contributors may be used 
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
 * Original author:  Thomas Müller, xnos Internet Services (xnos.org), 2007
 * Modified by: Luis F. G. Sarmenta, Massachusetts Institute of Technology (MIT)
 */ 
package edu.mit.csail.tpmj.tools;

import edu.mit.csail.tpmj.TPMException;
import edu.mit.csail.tpmj.funcs.TPMPcrFuncs;
import edu.mit.csail.tpmj.funcs.TPMUtilityFuncs;
import edu.mit.csail.tpmj.structs.TPM_PCRVALUE;
import edu.mit.csail.tpmj.util.TPMToolsUtil;

public class TPMReadPCRs
{

    /**
     * @param args
     */
    public static void main( String[] args )
    {
        TPMUtilityFuncs.initTPMDriver();

        for ( int i = 0; i < TPMPcrFuncs.getNumPcrs(); i++ )
        {
            try
            {
                TPM_PCRVALUE value = TPMPcrFuncs.TPM_PCRRead( i );
                String valueMod = value.toString().toUpperCase();
                System.out.println( "PCR [" + i + "] " + valueMod );
            }
            catch ( TPMException e )
            {
                TPMToolsUtil.handleTPMException( e );
            }
        }

        TPMUtilityFuncs.cleanupTPMDriver();
    }

}
