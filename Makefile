C_COMPILER=arm-linux-gnueabihf-gcc
NATIVE_SOURCE_DIR=JNI/src
NATIVE_OBJ_DIR=JNI/lib

all: pit car native
	
pit:
	javac source/fsae/da/pit/*.java -d out

car: native
	javac source/fsae/da/car/*.java -d out/

native:
	$(C_COMPILER) -I $(JAVA_HOME)/include -I $(JAVA_HOME)/include/linux -shared -fPIC -o $(NATIVE_OBJ_DIR)/libnativei2c.so $(NATIVE_SOURCE_DIR)/NativeI2C.c

stage:
	git add -A
	git reset HEAD out JNI/lib
	git status

clean:
	rm -r out/*