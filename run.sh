# #! /usr/bin/env bash

BROADCAST_IP=127.0.0.1
COMM_PORT=2016
SENSOR_CONFIG_FILE=../config/sensor_config.properties

if [ $# -lt 1 ]; then
	echo "specify a run target like car, pit, or sim";
	exit 0
fi

if [ $1 == "pit" ]; then
	if [ $# -lt 2 ]; then
		echo "please specify pit port"
		exit 0
	fi
	cd out
	sudo java fsae.da.pit.PitMain $2
	cd ..
elif [ $1 == "car" ]; then
	# if [ $# -lt 2 ]; then
	# 	echo "please specify pit IP, port, and sensor config file"
	# 	exit 0
	# fi
	cd out
	# java fsae.da.car.CarMain $2 $3 $4
	java fsae.da.car.CarMain $BROADCAST_IP $COMM_PORT $SENSOR_CONFIG_FILE
	cd ..
elif [ $1 == "sim" ]; then
	if [ $# -lt 4 ]; then
		echo "please specify pit IP, port, and sensor config file"
		exit 0
	fi
	x-terminal-emulator --working-directory=./out -e "java fsae.da.car.CarMain $2 $3 $4"
	x-terminal-emulator --working-directory=./out -e "sudo java fsae.da.pit.PitMain $3"
else
	echo "specify a run target like car, pit, or sim"
fi