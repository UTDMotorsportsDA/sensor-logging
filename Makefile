all:
	javac source/pit/*.java -d out/pit/*.java
	javac source/car/*.java -d out/car/*.java

stage:
	git add source run.sh config Makefile README.md
	git status
