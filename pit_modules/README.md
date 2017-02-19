# Pit Software Development Instructions
##

### Prerequisites:
* If you can't access the spreadsheet, message Brian Tilley or Derrick Heydari to be added to the Data Acquisition Box folder.
* You'll need to know how to commit, pull, and push code changes to GitHub. To be added as a collaborator, contact Brian Tilley on any of the relevant GroupMe chats.
* Currently, the project can be built with the ```make``` command under Linux. If you need more information on how to build and run the code, please contact Brian Tilley.
* Please read through the guidelines.

### To contribute, please do the following:

1. Read the module you want to claim
2. Fill in your name and expected completion date in the spreadsheet (located in Box folder)
3. Develop and test
4. Organize source and documentation (.java source files go in source/fsae/da/pit/)
5. Commit your code to GitHub (always pull changes before you push!)
6. Mark your module as completed in the spreadsheet.

### Testing:

* Building the project with ```make``` command produces multiple .jar files in the ```jar/``` folder. Execute car_sim.jar (in Linux: ```java -jar car_sim.jar```) to start a simulated instance of the built-in car software. This program will behave the same as the car will when our system is deployed.

Take advantage of anything and everything in the car software you think might help.

Java's full reference documentation is available at https://docs.oracle.com/javase/8/ (or you can Google "classname se 8").