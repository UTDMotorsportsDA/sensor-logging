#!/usr/bin/env bash

BROADCAST_IP=127.0.0.1
COMM_PORT=2016
SENSOR_CONFIG_FILE=config/sensor_config.properties
SPOOF_SENSOR_CONFIG_FILE=config/spoof_config.properties

if [ $# -lt 1 ]; then

	echo "specify a run target like car, pit, or sim";
	exit 0

fi

if [ $1 == "pit" ]; then

	if [ $# -gt 1 ]; then
		COMM_PORT=$2
	fi
    bash exec_scripts/pit.sh $COMM_PORT

elif [ $1 == "car" ]; then

	if [[ $# -gt 1 ]]; then
		BROADCAST_IP=$2
	fi
	if [[ $# -gt 2 ]]; then
		COMM_PORT=$3
	fi
	if [[ $# -gt 3 ]]; then
		SENSOR_CONFIG_FILE=$4
	fi

	bash exec_scripts/car.sh $BROADCAST_IP $COMM_PORT $SENSOR_CONFIG_FILE

elif [ $1 == "sim" ]; then

 	if [[ $# -gt 1 ]]; then
 		BROADCAST_IP=$2
 	fi
 	if [[ $# -gt 2 ]]; then
 		COMM_PORT=$3
 	fi
 	if [[ $# -gt 3 ]]; then
 		SENSOR_CONFIG_FILE=$4
 	fi

 	x-terminal-emulator --working-directory=. -e bash exec_scripts/pit.sh $COMM_PORT
 	x-terminal-emulator --working-directory=. -e bash exec_scripts/car.sh $BROADCAST_IP $COMM_PORT $SPOOF_SENSOR_CONFIG_FILE

else
	echo "specify a run target like car, pit, or sim"

fi