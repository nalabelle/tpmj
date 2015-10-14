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
package edu.mit.csail.tpmj.transport;

import java.util.ArrayList;

import edu.mit.csail.tpmj.commands.TPMCommand;
import edu.mit.csail.tpmj.commands.TPM_ExecuteTransport;
import edu.mit.csail.tpmj.structs.TPMOutputStruct;
import edu.mit.csail.tpmj.structs.TPM_SECRET;
import edu.mit.csail.tpmj.util.BasicByteArrayable;
import edu.mit.csail.tpmj.util.ByteArrayReadWriter;
import edu.mit.csail.tpmj.util.ByteArrayStruct;
import edu.mit.csail.tpmj.util.ByteArrayUtil;
import edu.mit.csail.tpmj.util.Debug;

public class TPMTransportLogEntry extends BasicByteArrayable
{
    private TPMCommand origCmd;
    private TPMCommand transCmd;
    private TPMOutputStruct transOut;
    private TPMOutputStruct unencOut;

    public TPMTransportLogEntry( TPMCommand origCmd, TPMCommand transCmd,
        TPMOutputStruct transOut, TPMOutputStruct unencOut )
    {
        this.origCmd = origCmd;
        this.transCmd = transCmd;
        this.transOut = transOut;
        this.unencOut = unencOut;
    }
    
    public TPMTransportLogEntry( byte[] source )
    {
        this.fromBytes( source, 0 );
    }

    public TPMCommand getOrigCmd()
    {
        return origCmd;
    }

    public TPMCommand getTransCmd()
    {
        return transCmd;
    }

    public TPMOutputStruct getTransOut()
    {
        return transOut;
    }

    public TPMOutputStruct getUnencOut()
    {
        return unencOut;
    }

    @Override
    public String toString()
    {
        return "TPMTransportLogEntry:\n"
            + "origCmd = " + origCmd //+ "\n"
            + "transCmd = " + transCmd //+ "\n"
            + "transOut = " + transOut //+ "\n"
            + "unencOut = " + unencOut;
    }

    @Override
    public byte[] toBytes()
    {
        byte[] origCmdArr = origCmd.toBytes();
        byte[] transCmdArr = transCmd.toBytes();
        byte[] transOutArr = transOut.toBytes();
        byte[] unencOutArr = unencOut.toBytes();

        byte[] origCmdType = origCmd.getClass().getCanonicalName().getBytes();
        byte[] transCmdType = transCmd.getClass().getCanonicalName().getBytes();
        byte[] transOutType = transOut.getClass().getCanonicalName().getBytes();
        byte[] unencOutType = unencOut.getClass().getCanonicalName().getBytes();
        
        return ByteArrayUtil.buildBuf( 
            origCmdType.length, origCmdType, origCmdArr.length, origCmdArr,
            transCmdType.length, transCmdType, transCmdArr.length, transCmdArr, 
            transOutType.length, transOutType, transOutArr.length, transOutArr,
            unencOutType.length, unencOutType, unencOutArr.length, unencOutArr );
    }

    @Override
    public void fromBytes( byte[] source, int offset )
    {
        ByteArrayReadWriter brw = new ByteArrayReadWriter( source, offset );
        byte[] origCmdType = brw.readSizedByteArray();
        byte[] origCmdArr = brw.readSizedByteArray();
        byte[] transCmdType = brw.readSizedByteArray();
        byte[] transCmdArr = brw.readSizedByteArray();
        byte[] transOutType = brw.readSizedByteArray();
        byte[] transOutArr = brw.readSizedByteArray();
        byte[] unencOutType = brw.readSizedByteArray();
        byte[] unencOutArr = brw.readSizedByteArray();
        
        try
        {
            this.origCmd = (TPMCommand) Class.forName( new String( origCmdType ) ).newInstance();
            this.origCmd.fromBytes( origCmdArr, 0 );

            this.transCmd = (TPMCommand) Class.forName( new String( transCmdType ) ).newInstance();
            this.transCmd.fromBytes( transCmdArr, 0 );
            
            this.transOut = (TPMOutputStruct) Class.forName( new String( transOutType ) ).newInstance();
            this.transOut.fromBytes( transOutArr, 0 );
            
            this.unencOut = (TPMOutputStruct) Class.forName( new String( unencOutType ) ).newInstance();
            this.unencOut.fromBytes( unencOutArr, 0 );
        }
        catch ( InstantiationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( IllegalAccessException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( ClassNotFoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
}
