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


#include <jni.h>
#include <stdio.h>
#include "IFXTPMJNIProxy.h"
/* To make this compile, you need to generate the ifxtpm.lib from ifxtpm.dll */
/* #import "IFXTPM.dll" */

/* excerpt from the TSS 1.1 spec, modified to make name match function names in ifxtpm.dll */

typedef unsigned char BYTE;
typedef signed char TSS_BOOL; /* Make specific to TSS to avoid potential conflicts */
typedef unsigned short UINT16;
typedef unsigned long UINT32;
typedef unsigned short UNICODE;
typedef void* PVOID;

typedef UINT32 TSS_RESULT; /* the return code from a TSS function */


/*  --- This should be used on Windows platforms */
#ifdef TDDLI_EXPORTS
#define TDDLI __declspec(dllexport)
#else
#define TDDLI __declspec(dllimport)
#endif

/* establish a connection to the TPM device driver */
extern TDDLI TSS_RESULT TDDL_Open();
/* close a open connection to the TPM device driver */
extern TDDLI TSS_RESULT TDDL_Close();
/* cancels the last outstanding TPM command */
extern TDDLI TSS_RESULT TDDL_Cancel();
/* read the attributes returned by the TPM HW/FW */
extern TDDLI TSS_RESULT TDDL_GetCapability(
	UINT32 CapArea,
	UINT32 SubCap,
	BYTE* pCapBuf,
	UINT32* puntCapBufLen
	);
/* set parameters to the TPM HW/FW */
extern TDDLI TSS_RESULT TDDL_SetCapability(
	UINT32 CapArea,
	UINT32 SubCap,
	BYTE* pCapBuf,
	UINT32* puntCapBufLen
	);
/* get status of the TPM driver and device */
extern TDDLI TSS_RESULT TDDL_GetStatus(
									   UINT32 ReqStatusType,
									   UINT32* puntStatus
									   );
/* send any data to the TPM module */
extern TDDLI TSS_RESULT TDDL_TransmitData(
	BYTE* pTransmitBuf,
	UINT32 TransmitBufLen,
	BYTE* pReceiveBuf,
	UINT32* puntReceiveBufLen
	);

/*
extern __declspec(dllimport) unsigned long TDDL_TransmitData( unsigned char* pTransmitBuf, unsigned long TransmitBufLen, unsigned char* pReceiveBuf, unsigned long* puntReceiveBufLen );
*/

/*
* Class:     edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver
* Method:    _TDDL_Open
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver__1TDDL_1Open
(JNIEnv *env , jclass class){
	return TDDL_Open();
}

/*
* Class:     edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver
* Method:    _TDDL_Close
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver__1TDDL_1Close
(JNIEnv *env , jclass class)
{
	return TDDL_Close();
}

/*
* Class:     edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver
* Method:    _TDDL_TransmitData
* Signature: ([B[B)I
*/
JNIEXPORT jint JNICALL Java_edu_mit_csail_tpmj_drivers_win32_Win32IFXTPMDriver__1TDDL_1TransmitData
(JNIEnv * env, jclass class, jbyteArray inBytes, jbyteArray outBytes)
{
	jbyte *cinArr = NULL;
	jint inArrLength = 0;
	jbyte *coutArr = NULL;
	jint outArrLength = 0;
	jlong ret = 0;

	/* printf("Inside call to Windows TPM driver ...\n"); */

	inArrLength = (*env)->GetArrayLength(env, inBytes);
	cinArr = (*env)->GetByteArrayElements(env, inBytes, NULL);
	if ( cinArr == NULL ) 
	{
		/* printf("Error getting in array ...\n");	*/
		return -1; 
	}


	outArrLength = (*env)->GetArrayLength(env, outBytes);
	coutArr = (*env)->GetByteArrayElements(env, outBytes, NULL);
	if ( coutArr == NULL ) 
	{
		/* printf("Error getting out array ...\n");	*/
		return -1; 
	}

	/* printf("Calling TDDL ...\n"); */

	ret = TDDL_TransmitData( cinArr, inArrLength, coutArr, &outArrLength ); 

	(*env)->SetByteArrayRegion(env, outBytes, 0, outArrLength, coutArr);

	return ret;
}
