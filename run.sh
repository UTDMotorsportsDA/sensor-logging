#!/usr/bin/env bash

CLASSPATH=out:lib/*

if [ $# -lt 1 ]; then
	echo "specify a run target like car or pit";
	exit 0
fi

if [ $1 == "pit" ]; then
	java -cp $CLASSPATH fsae.da.pit.PitMain ${@:2}
elif [ $1 == "car" ]; then
	java -Djava.library.path=JNI/lib fsae.da.car.CarMain ${@:2}
else
	echo "specify a run target like car or pit"
fi
