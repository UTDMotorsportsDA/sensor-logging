all:
	javac source/fsae/da/pit/*.java source/fsae/da/car/*.java -d out/

stage:
	git add source run.sh config Makefile README.md etc
	git status

clean:
	rm -r out/*