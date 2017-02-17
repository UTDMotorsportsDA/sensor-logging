# UTD Motorsports Data Aqcuisition Software
This repository contains code and related files for the UT Dallas Motorsports FSAE team's Data Aqcuisition System.

## Index
* Design Overview - basic ideas behind the design of this system
* System Layout - what hardware is used, how devices are connected
* Communication Protocol - how data gets from the car to the pit
* Java Class Descriptions
	* embedded software (runs on the system built in the car)
    * pit software (runs on a network-connected computer in the pit)

## Design Overview
The basic idea is to collect data points from sensors built into the car and perform 3 main tasks:
* Log data to files
* Present data to the Pit Engineers
* Display data to the driver

Requirements:

* The system must be modular and expandable to allow additional features and new sensor types to be added
* The system must be able to record sensor data of any arbitrary format (numbers, vectors, strings, etc.).
* Logging time intervals must be configurable on a sensor-by-sensor basis.
* ...

## System Layout
\<insert topology image here\>

Main subsystems:

* Data Controller
* Instrument Cluster 
* Pit Interface

As depicted, the system consists of the Engine Control Unit (ECU), which connects to most sensors, and an Adafruit 9-DOF IMU (3-axis accelerometer, gyroscope, and magnetometer) both feeding data to a Beaglebone Black. ...

## Communication Protocol
All devices on the network communicate with each other using [JSON (Javascript Object Notation)](http://json.org)