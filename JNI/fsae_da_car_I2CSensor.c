#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#define I2C_DEV_PREFIX "/dev/i2c-"

/*
 * Class:     fsae_da_car_I2CSensor
 * Method:    openI2Cbus
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_fsae_da_car_I2CSensor_openI2Cbus
	(JNIEnv *env, jclass cl, jint busNum) {

}

/*
 * Class:     fsae_da_car_I2CSensor
 * Method:    setI2Cslave
 * Signature: (C)V
 */
JNIEXPORT void JNICALL Java_fsae_da_car_I2CSensor_setI2Cslave
	(JNIEnv *env, jclass cl, jchar addr) {
		if(ioctl)
}

/*
 * Class:     fsae_da_car_I2CSensor
 * Method:    writeBytes
 * Signature: ([CI)V
 */
JNIEXPORT void JNICALL Java_fsae_da_car_I2CSensor_writeBytes
	(JNIEnv *, jclass, jcharArray, jint);

/*
 * Class:     fsae_da_car_I2CSensor
 * Method:    readBytes
 * Signature: (C[CI)V
 */
JNIEXPORT void JNICALL Java_fsae_da_car_I2CSensor_readBytes
	(JNIEnv *, jclass, jchar, jcharArray, jint);