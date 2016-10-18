all:
	javac com/company/*.java

clean:
	rm com/company/*.class

stage:
	git add IntelliJ/src/com/company/*.java
	git add Makefile
	git status
