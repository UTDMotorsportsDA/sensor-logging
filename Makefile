ARM_CROSS_COMPILER=arm-linux-gnueabihf-gcc
NATIVE_SOURCE_DIR=source/JNI
NATIVE_OBJ_DIR=lib/JNI
CLASS_DIR=class
MANIFEST_DIR=manifest
JAR_DEST=jar
SOURCE_ROOT=source/edu/utdallas/utdmotorsports

export CLASSPATH=$(CLASS_DIR):lib/*

all: common pit car native
	jar -xf lib/json-simple-1.1.1.jar org && cp -r org $(CLASS_DIR) && rm -r org/
	jar cfm $(JAR_DEST)/car.jar $(MANIFEST_DIR)/Manifest-car.txt -C $(CLASS_DIR) edu/ -C $(CLASS_DIR) org/ config/
	jar cfm $(JAR_DEST)/car_sim.jar $(MANIFEST_DIR)/Manifest-car_sim.txt -C $(CLASS_DIR) edu/ -C $(CLASS_DIR) org/ config/simulation
	jar cfm $(JAR_DEST)/pit.jar $(MANIFEST_DIR)/Manifest-pit.txt -C $(CLASS_DIR) edu/ config/general.prop
	zip -qd $(JAR_DEST)/car.jar edu/utdallas/utdmotorsports/pit/* config/simulation/* config/simulation
	zip -qd $(JAR_DEST)/car_sim.jar edu/utdallas/utdmotorsports/pit/*
	zip -qd $(JAR_DEST)/pit.jar edu/utdallas/utdmotorsports/car/*

	@echo "\nsuccessfully compiled the project into the $(JAR_DEST)/ folder"

common:
	javac $(SOURCE_ROOT)/*.java -d $(CLASS_DIR)

pit:
	javac $(SOURCE_ROOT)/pit/*.java -d $(CLASS_DIR)

car:
	javac $(SOURCE_ROOT)/car/Sensor.java -d $(CLASS_DIR)
	javac $(SOURCE_ROOT)/car/sensors/*.java -d $(CLASS_DIR)
	javac $(SOURCE_ROOT)/car/*.java -d $(CLASS_DIR)

native:
	javah -jni -d $(NATIVE_SOURCE_DIR) edu.utdallas.utdmotorsports.car.NativeI2C

test:
	javac source/test/*.java -d $(CLASS_DIR)

ifdef C_COMPILER
	$(C_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/edu_utdallas_utdmotorsports_car_NativeI2C.c
else
	$(ARM_CROSS_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/edu_utdallas_utdmotorsports_car_NativeI2C.c
endif

stage:
	git add -A
	git status

clean:
	rm -r $(JAR_DEST)/*.jar $(CLASS_DIR)/*
