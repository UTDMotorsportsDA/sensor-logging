# fsae_DA
Data Acquisition software

Use Java version 1.8

Please keep any IDE workspace-specific files separate from the repository.

Data points will be communicated as UDP DatagramPackets with the following format:

	<name of sensor>=<sensor value>@<timestamp>

	format of sensor value will depend upon the sensor in question

	timestamp will be formatted as <seconds>:<milliseconds>:<microseconds>