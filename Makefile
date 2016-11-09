all:
	javac source/fsae/da/pit/*.java source/fsae/da/car/*.java -d out/

native:
	cd JNI
	gcc fsae_da_car_I2CSensor.c -shared -c -I $JAVA_HOME/include -I $JAVA_HOME/include/linux -o libI2CSensor.so
	cd ..

stage:
	git add source run.sh config Makefile README.md etc
	git status

clean:
	rm -r out/*