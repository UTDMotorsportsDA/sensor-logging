/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class main */

#ifndef _Included_main
#define _Included_main
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     main
 * Method:    createCanvasForLedDisplay
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_main_createCanvasForLedDisplay
  (JNIEnv *, jobject);

/*
 * Class:     main
 * Method:    deleteCanvas
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_main_deleteCanvas
  (JNIEnv *, jobject, jlong);

/*
 * Class:     main
 * Method:    displaySpeedAndRpm
 * Signature: (IIJ)V
 */
JNIEXPORT void JNICALL Java_main_displaySpeedAndRpm
  (JNIEnv *, jobject, jint, jint, jlong);

/*
 * Class:     main
 * Method:    displayTemperature
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_main_displayTemperature
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     main
 * Method:    displayGForceGrid
 * Signature: (IIJ)V
 */
JNIEXPORT void JNICALL Java_main_displayGForceGrid
  (JNIEnv *, jobject, jint, jint, jlong);

#ifdef __cplusplus
}
#endif
#endif
