#!/usr/bin/env bash

if [ $# -lt 1 ]; then
	echo "specify a run target like car or pit";
	exit 0
fi

if [ $1 == "pit" ]; then
	java -cp out/ fsae.da.pit.PitMain ${@:2}
elif [ $1 == "car" ]; then
	java -cp out/ fsae.da.car.CarMain ${@:2}
else
	echo "specify a run target like car or pit"
fi
