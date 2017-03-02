# UTD Motorsports Data Acquisition Software
This repository contains code and related files for the UT Dallas Motorsports FSAE team's Data Acquisition System.

[Generated JavaDoc][1]

## Index
* Design Overview - basic ideas behind the design of this system
* System Layout - what hardware is used, how devices are connected
* Communication Protocol - how data gets from the car to the pit

## Design Overview
The main goal is to collect data points from sensors built into the car and perform 3 tasks:
* Log data to files
* Present data to the Pit Engineers
* Display data to the driver

Requirements:

* The system must be modular and expandable to allow the addition of new features and sensors.
* The system must be able to record sensor data of any arbitrary format (numbers, vectors, strings, etc.).
* Bandwidth must be used sparingly and data logging rates must be configurable.

## System Layout
\<insert topology image here\>

Primary subsystems:

* Data Controller - collect and distribute sensor data
    
    Everything under the package ```edu.utdallas.utdmotorsports.car``` supports this subsystem.
    
* Instrument Cluster (a.k.a. dashboard) - display data and alerts to the driver

    Everything under the package ```edu.utdallas.utdmotorsports.cluster``` supports this subsystem.

* Pit User Interface - display data, graphs and analysis to the race engineers

    Everything under the package ```edu.utdallas.utdmotorsports.pit``` supports this subsystem.

All subsystems are connected to a network LAN mounted to the vehicle. The pit computer connects via Wi-Fi,
and all other systems are directly connected to the in-car router with Ethernet.

The Data Controller is centered around a BeagleBone Black embedded Linux computer. The BeagleBone receives data
from an Adafruit 9-DOF IMU, which supplies accelerometer, gyroscope, and magnetometer readings. All other data
is transmitted from the Engine Control Unit (ECU) to the BeagleBone via CAN bus. With the exception of the IMU, all
sensors to the ECU, which internally logs data as it feeds the Controller. The BeagleBone runs the car software
in this project to ultimately transmit data to one or more devices on the network.

The Instrument Cluster is driven by a Raspberry Pi, which receives only UDP multicast data sent by the Controller. The viewable
portion of the cluster consists of 8 individual warning LEDs and a general purpose 32x16 pixel RGB LED matrix that shows
selected data, such as numerical speed and engine RPM.
 
Pit Software, in this system, serves to display vehicle data to the engineers in real-time. Additionally, this software will
perform analysis on the data to make sense of information from multiple sensors and better guide the engineers on potential
improvements to be made to the vehicle.

## Communication Protocol
All devices on the network communicate with each other using [JSON](http://json.org) objects. The [json-simple library](2) is
included in this project, and examples can be found both in the car software and in the [library wikis](3). All devices
must first connect to the system's multicast group on the correct port, both of which will be known by each device at startup.

### Automatic service discovery:
All services within this protocol have predetermined names by which they can be identified. For a device wishing to connect
to a service, the device must send a discovery request message formatted as follows:
```
{
    "discovery request" :
    {
        "name" : "service's predetermined name"
    }
}
```
**note: whitespace and indentation is optional in JSON. The above is equivalent to ```{"discovery request":{"name":"service's predetermined name"}}```.**

Services must listen on the multicast group for messages of this format that match their name and issue a response like the following:
```
{
	"discovery response" :
	{
		"name" : "service's predetermined name",
		"location" : "IP_address:port",
		"params" : "URL",
		"clock" : "hours:minutes:seconds.milliseconds"
	}
}
```
Here, the name matches the client's request and the remaining three values are determined by the service that responds.

The "params" value specifies where a client may retreive a file containing additional information.
This section is optional, and it is up to the designer of the service whether or not include a "params" field.

"location" defines the IP address and port at which a client may connect to the service.

"clock" tells the client what time the service currently has. The main purpose for knowing a service's system time is
for synchronization.

After sending a response like the above, it is up to the service to allow or decline an incoming connection.

#### Halley's Comet Data Transmitter
For reliability, ease of use, and data integrity, the Data Controller implements this automatic discovery protocol. Devices
on the network, primarily the pit software, may attempt to connect and will be subject to the Controller's maximum connection limit.
Below is an example of how the controller might respond to a request:
```
{
	"discovery response" :
	{
		"name" : "Halley’s Comet Data Transmitter",
		"location" : "192.168.3.220:7342",
		"params" : "https://192.168.3.220/service_params.txt",
		"clock" : "13:00:48.279"
	}
}
```
Notice that a "params" field is included in the response. This URL points to a file that enumerates
all available sensors and their data formats.

```service_params.txt```:
```
IMU_accelerometer=vec-3-double
engine_RPM=int
speed=double
```

int and double formats are identical to those in Java, and the vec-3-double is three doubles separated by commas ( ```0.1,0.345,0.987654```).
Data sent over dedicated connections appears as follows:

```{"data":"sensor=value@timestamp!status""}``` (e.g.: ```{"data":"engine_RPM=4532@1488397113307!OK"}```)

* "timestamp" is in the format of Unix time milliseconds, which is the number of milliseconds that have passed since
January 1, 1970.
* "status" is either "OK" or "critical"

Additionally, the car software provides a data broadcast, meaning the entire multicast group receives identical messages to those shown.

[1]: https://utdmotorsportsda.github.io/sensor-logging/
[2]: https://code.google.com/archive/p/json-simple/
[3]: https://code.google.com/archive/p/json-simple/wikis