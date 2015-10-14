/*
 * Copyright (c) 2007, Massachusetts Institute of Technology (MIT)
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
 * Original author:  Luis F. G. Sarmenta, MIT, 2007
 */ 
package edu.mit.csail.tpmj.structs;

import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.CryptoUtil;

public class TPM_PCR_INFO extends SimpleTPMStruct
{
    /*
     * typedef struct tdTPM_PCR_INFO{
     *    TPM_PCR_SELECTION pcrSelection;
     *    TPM_COMPOSITE_HASH digestAtRelease;
     *    TPM_COMPOSITE_HASH digestAtCreation;
     * } TPM_PCR_INFO;
     */

    private TPM_PCR_SELECTION pcrSelection;
    private TPM_DIGEST digestAtRelease;

    // NOTE: As far as I can tell from the TPM 1.2 Commands spec, digestAtCreation is never set by the user.
    // It is set by Seal and other commands internally (so that the TPM_PCR_INFO struct resulting internally
    // is stored with the sealed data).
    private TPM_DIGEST digestAtCreation;

    /**
     * Empty constructor for use with readStruct.
     */
    public TPM_PCR_INFO()
    {
        // do nothing
    }

    public TPM_PCR_INFO( TPM_PCR_SELECTION pcrSelection, TPM_DIGEST digestAtRelease, TPM_DIGEST digestAtCreation )
    {
        super();
        this.pcrSelection = pcrSelection;
        this.digestAtRelease = digestAtRelease;
        this.digestAtCreation = digestAtCreation;
    }
    
    public TPM_PCR_INFO( byte[] source )
    {
        this.fromBytes( source, 0 );
    }
    
    /**
     * Given a TPM_PCR_COMPOSITE, creates a TPM_PCR_INFO with the
     * same selection, the composite's hash for digestAtRelease,
     * and all zeros for digestAtCreation.
     * 
     * @param pcrComposite
     */
    public TPM_PCR_INFO( TPM_PCR_COMPOSITE pcrComposite )
    {
        this.setPcrSelection( pcrComposite.getSelect() );
        TPM_DIGEST digest = CryptoUtil.computeTPM_DIGEST( pcrComposite );
        this.setDigestAtCreation( new TPM_DIGEST() );
        this.setDigestAtRelease( digest );
        
        // FIXME: LFGS 20070402: This has been tested and works with TPM_Seal with a Broadcom TPM 1.2 chip, and an Infineon 1.1 chip.
        // Setting both digests to digest also works.
        // BUT, neither works with the Infineon 1.2 chip on the Intel Mac.
    }

    public TPM_DIGEST getDigestAtCreation()
    {
        return digestAtCreation;
    }

    public void setDigestAtCreation( TPM_DIGEST digestAtCreation )
    {
        this.digestAtCreation = digestAtCreation;
    }

    public TPM_DIGEST getDigestAtRelease()
    {
        return digestAtRelease;
    }

    public void setDigestAtRelease( TPM_DIGEST digestAtRelease )
    {
        this.digestAtRelease = digestAtRelease;
    }

    public TPM_PCR_SELECTION getPcrSelection()
    {
        return pcrSelection;
    }

    public void setPcrSelection( TPM_PCR_SELECTION pcrSelection )
    {
        this.pcrSelection = pcrSelection;
    }

    /**
     * If digestAtRelease or digestAtCreation is null, substitutes 20 zeros.
     */
    @Override
    public byte[] toBytes()
    {
        TPM_DIGEST daR = ( digestAtRelease != null ) ? digestAtRelease : new TPM_DIGEST(); 
        TPM_DIGEST daC = ( digestAtCreation != null ) ? digestAtCreation : new TPM_DIGEST(); 
        return ByteArrayUtil.buildBuf( pcrSelection, daR, daC );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        this.pcrSelection = new TPM_PCR_SELECTION();
        brw.readStruct( this.pcrSelection );
        this.digestAtRelease = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ));
        this.digestAtCreation = new TPM_DIGEST( brw.readBytes( TPM_DIGEST.SIZE ));
    }

    public String toString()
    {
        return "TPM_PCR_INFO: \n" 
            + "pcrSelection = " + this.pcrSelection + "\n" 
            + "digestAtRelease =  " + this.digestAtRelease + "\n"
            + "digestAtCreation =  " + this.digestAtCreation + "\n"
            ;
    }
}
