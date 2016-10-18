all:
	javac com/company/*.java

clean:
	mv IntelliJ/src/com/company/*.class IntelliJ/out/production/IntelliJ/com/company/	

stage:
	git add IntelliJ/src/com/company/*.java
	git add Makefile
	git status
