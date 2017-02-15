ARM_CROSS_COMPILER=arm-linux-gnueabihf-gcc
NATIVE_SOURCE_DIR=source/JNI
NATIVE_OBJ_DIR=lib/JNI
BEAGLEGBONE_IP=192.168.3.142
CLASS_DIR=class

export CLASSPATH=$(CLASS_DIR):lib/*

all: common pit car native
	jar cfm car.jar Manifest-car.txt -C $(CLASS_DIR) fsae/ config/general.prop config/sensor.prop config/test_params.txt
	jar cfm car_sim.jar Manifest-car_sim.txt -C $(CLASS_DIR) fsae/ config/simulation
	jar cfe pit.jar fsae.da.pit.PitMain -C $(CLASS_DIR) fsae/ config/general.prop
	zip -qd car.jar fsae/da/pit/*
	zip -qd car_sim.jar fsae/da/pit/*
	zip -qd pit.jar fsae/da/car/*

common:
	javac source/fsae/da/*.java -d $(CLASS_DIR)

pit:
	javac source/fsae/da/pit/*.java -d $(CLASS_DIR)

car:
	javac source/fsae/da/car/*.java -d $(CLASS_DIR)

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
	rm -r *.jar $(CLASS_DIR)/*
