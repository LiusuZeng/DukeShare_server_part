target:
	mkdir ./bin
	javac ./src/*.java -d ./bin
clean:
	rm -rf ./bin
