all:
	javac src/com/company/*.java -d IntelliJ/out/production/IntelliJ

clean:
	rm IntelliJ/src/com/company/*.class

stage:
	git add IntelliJ/src/com/company/*.java
	git add Makefile
	git status
