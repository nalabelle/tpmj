/*
 * Copyright (c) 2007, Thomas Müller, xnos Internet Services (xnos.org)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of xnos nor the names of its contributors may be used 
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
 */ 
// TBSProxy.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "TBSProxy.h"
#include <tbs.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

//
//TODO: If this DLL is dynamically linked against the MFC DLLs,
//		any functions exported from this DLL which call into
//		MFC must have the AFX_MANAGE_STATE macro added at the
//		very beginning of the function.
//
//		For example:
//
//		extern "C" BOOL PASCAL EXPORT ExportedFunction()
//		{
//			AFX_MANAGE_STATE(AfxGetStaticModuleState());
//			// normal function body here
//		}
//
//		It is very important that this macro appear in each
//		function, prior to any calls into MFC.  This means that
//		it must appear as the first statement within the 
//		function, even before any object variable declarations
//		as their constructors may generate calls into the MFC
//		DLL.
//
//		Please see MFC Technical Notes 33 and 58 for additional
//		details.
//


// CTBSProxyApp

BEGIN_MESSAGE_MAP(CTBSProxyApp, CWinApp)
END_MESSAGE_MAP()


// CTBSProxyApp construction

CTBSProxyApp::CTBSProxyApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CTBSProxyApp object

CTBSProxyApp theApp;


// CTBSProxyApp initialization

BOOL CTBSProxyApp::InitInstance()
{
	CWinApp::InitInstance();

	return TRUE;
}

//global fields
TBS_HCONTEXT phContext;
TBS_RESULT result = 0xF;

// Java JNI Proxy Methods
JNIEXPORT jlong JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbs_1getReturnCode
  (JNIEnv *env, jobject obj)
{
	UINT32 returnRes = result;
	result = 0xF; //reset result
	return returnRes;
}

JNIEXPORT void JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbsip_1Context_1Create
	(JNIEnv *env, jobject obj)
{	
	// create context params
	TBS_CONTEXT_PARAMS pContextParams;
	pContextParams.version = TBS_CONTEXT_VERSION_ONE;

	// create context
	result = Tbsi_Context_Create(&pContextParams,&phContext);
}

JNIEXPORT void JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbsip_1Context_1Close
  (JNIEnv *env, jobject obj)
{
	// close context
	result = Tbsip_Context_Close(phContext);	
}

JNIEXPORT void JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbsip_1Cancel_1Commands
  (JNIEnv *env, jobject obj)
{
	// not implemented yet
}

JNIEXPORT jbyteArray JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbsip_1Physical_1Presence_1Command
  (JNIEnv *env, jobject obj, jbyteArray, jint)
{
	// not implemented yet
	return env->NewByteArray(0);
}

JNIEXPORT jbyteArray JNICALL Java_edu_mit_csail_tpmj_drivers_win32_WindowsVistaTBSProxy_Tbsip_1Submit_1Command
  (JNIEnv *env, jobject obj, jbyteArray input, jint input_length)
{	
	// get input buf
	jboolean iscopy;
	UINT32 inputBufLen = input_length;
	jbyte *inputBuf = env->GetByteArrayElements(input, &iscopy);
	// copy input buf
	byte *pCommandBuf = new byte[inputBufLen];
	memcpy(pCommandBuf,inputBuf,inputBufLen);

	// prepare result buf
	UINT32 outputBufLen = 4096;
	byte *pResultBuf = new byte[outputBufLen];

	// submit commad
	result = Tbsip_Submit_Command(phContext,TBS_COMMAND_LOCALITY_ZERO,TBS_COMMAND_PRIORITY_NORMAL,pCommandBuf,inputBufLen,pResultBuf,&outputBufLen);

	// copy outputBuf
	jbyte *outputBuf = new jbyte[outputBufLen];
	memcpy(outputBuf,pResultBuf,outputBufLen);

	// convert output buf
	jbyteArray outputBufArray;
	outputBufArray = env->NewByteArray(outputBufLen);
	env->SetByteArrayRegion(outputBufArray,0,outputBufLen,outputBuf);
	// return result
	return outputBufArray;
}