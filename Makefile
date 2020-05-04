SIZE=3

all: build

build: 
	mvn package
	docker-compose up -d --scale server=$(SIZE)

clean:
	mvn clean
	docker-compose down
