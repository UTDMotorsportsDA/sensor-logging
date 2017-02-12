ARM_CROSS_COMPILER=arm-linux-gnueabihf-gcc
NATIVE_SOURCE_DIR=source/JNI
NATIVE_OBJ_DIR=lib/JNI
BEAGLEGBONE_IP=192.168.3.142

export CLASSPATH=class/:lib/*

all: common pit car native
	jar cfm car.jar Manifest-car.txt -C class/ fsae/
	jar cfe pit.jar fsae.da.pit.PitMain -C class fsae/
	zip -qd car.jar fsae/da/pit/*
	zip -qd pit.jar fsae/da/car/*

common:
	javac source/fsae/da/*.java -d class/

pit:
	javac source/fsae/da/pit/*.java -d class/

car:
	javac source/fsae/da/car/*.java -d class/

native:
	javah -jni -d $(NATIVE_SOURCE_DIR) fsae.da.car.NativeI2C

ifdef C_COMPILER
	$(C_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/fsae_da_car_NativeI2C.c
else
	$(ARM_CROSS_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/fsae_da_car_NativeI2C.c
endif

stage:
	git add -A
	git status

clean:
	rm -r class/*
