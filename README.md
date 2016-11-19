#Data Acquisition team software

Use Java version 1.8

Please keep any IDE workspace-specific files separate from the repository.

Data points will be communicated as UDP DatagramPackets with the following format:

	<name of sensor>=<sensor value>@<timestamp>

	format of sensor value will depend upon the sensor in question

	timestamp is number of milliseconds since 1/1/1970 according to the sender's system clock

example usage:

    make
	./run.sh sim

The above will compile source code for simulating on the current machine, then open 2 terminal windows (one for car, one for pit).
