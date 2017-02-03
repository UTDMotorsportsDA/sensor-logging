ARM_CROSS_COMPILER=arm-linux-gnueabihf-gcc
NATIVE_SOURCE_DIR=JNI/src
NATIVE_OBJ_DIR=JNI/lib
BEAGLEGBONE_IP=192.168.3.142

export CLASSPATH=out:lib/*

all: common pit car native

common:
	javac source/fsae/da/*.java -d out/

pit:
	javac source/fsae/da/pit/*.java -d out/

car:
	javac source/fsae/da/car/*.java -d out/

native:
	javah -jni -cp out -d JNI/src fsae.da.car.NativeI2C

ifdef C_COMPILER
	$(C_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/fsae_da_car_NativeI2C.c
else
	$(ARM_CROSS_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/fsae_da_car_NativeI2C.c
endif

load:
	cp -r JNI logger
	rm -r logger/JNI/src
	cp -r config logger
	cp -r out logger
	rm -r logger/out/fsae/da/pit
	cp run.sh logger
	scp -r logger $(user)@$(BEAGLEGBONE_IP):/home/$(user)

stage:
	git add -A
	git status

clean:
	rm -r out/*
