#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <linux/i2c-dev.h>
#include <unistd.h>
#include <string.h>

#include "fsae_da_car_NativeI2C.h"

#define I2C_DEVICE_FILENAME_PREFIX "/dev/i2c-"

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1openDevice(JNIEnv *env, jclass cls, jint deviceNumber) {
    // limit filename range
	if(deviceNumber < 0 || deviceNumber > 127)
		return -1;

	// result file descriptor, generated filename
	int fd;
	char filename[256];
	char deviceNumberString[4];

	// generate strings
	sprintf(deviceNumberString, "%d", deviceNumber);
	strcpy(filename, I2C_DEVICE_FILENAME_PREFIX);

	// concatenate
	strcat(filename, deviceNumberString);

	// let caller deal with errors
	return open(filename, O_RDWR);
}

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1setSlave(JNIEnv *env, jclass cls, jbyte slaveAddress, jint deviceFileDescriptor) {
    return ioctl(deviceFileDescriptor, I2C_SLAVE, slaveAddress);
}

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1write(JNIEnv *env, jclass cls, jbyteArray _bytes, jint numBytes, jint deviceFileDescriptor) {
    if((*env)->GetArrayLength(env, _bytes) < numBytes) return -1;

    char* bytes = (char*)malloc(numBytes);
    (*env)->GetByteArrayRegion(env, _bytes, 0, numBytes, bytes);

	if(write(deviceFileDescriptor, bytes, numBytes) == numBytes) {
	    free(bytes);
		return 0;
	}
	else {
	    free(bytes);
		return -1;
    }
}

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1writeByte(JNIEnv *env, jclass cls, jbyte byte, jint deviceFileDescriptor) {
    if(write(deviceFileDescriptor, &byte, 1) == 1)
    	return 0;
    else
    	return -1;
}

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1read(JNIEnv *env, jclass cls, jbyteArray _buffer, jint deviceFileDescriptor) {
    int numBytes = (*env)->GetArrayLength(env, _buffer);
    if(numBytes < 1)
        return -1;

    char* buffer = (char*)malloc(numBytes);

    if(read(deviceFileDescriptor, buffer, numBytes) == numBytes) {
        (*env)->SetByteArrayRegion(env, _buffer, 0, numBytes, (jbyte*)buffer);
        free(buffer);
    	return 0;
	}
	else {
	    free(buffer);
	    return -1;
	}
}

JNIEXPORT jint JNICALL
Java_fsae_da_car_NativeI2C_n_1readByte(JNIEnv *env, jclass cls, jint deviceFileDescriptor) {
    char buffer;
	if(read(deviceFileDescriptor, &buffer, 1) == 1)
		return buffer;
	else
		return -1;
}

#ifdef __cplusplus
}
#endif