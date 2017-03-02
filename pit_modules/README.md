# Pit Software Development Instructions

### Prerequisites:
* If you can't access the spreadsheet, message Brian Tilley or Derrick Heydari to be added to the Data Acquisition Box folder.
* You'll need to know how to commit, pull, and push code changes to GitHub, optionally through the [GitHub desktop client](https://desktop.github.com/) for Mac OS X and Windows. To be added as a collaborator, contact Brian Tilley on any of the relevant GroupMe chats.
* The Ant build file ```build.xml``` is provided in the root level of the project. You will need to add a ```build.properties``` file that includes the ```jdk.home.1.8``` property.
* Read the guidelines, which pertain to good practice and documentation.

The project is already set up to run in the IntelliJ IDEA Community Edition IDE, which is recommended for ease of development.
You will still need to add a build.properties file.

### To contribute, please do the following:

1. Read the module you want to claim
2. Fill in your name and expected completion date in the spreadsheet [here][1].
3. Develop and test
4. Organize source and documentation (.java source files go in source/fsae/da/pit/)
5. Commit your code to GitHub
6. Mark your module as completed in the spreadsheet.

### Workflow tips:

* The provided Ant target ```all``` compiles the whole project into jar files.
* To start a simulated instance of the car controller, run ```car_sim.jar```.
This will transmit data on the network and behave exactly like the car.

Take advantage of anything and everything in the car software you think might help.

Java's full reference documentation is available at https://docs.oracle.com/javase/8/ (or you can Google "classname se 8").

[1]: https://utdallas.box.com/s/z3t8b6cc3dclpurn3x4womfurn6svl9l