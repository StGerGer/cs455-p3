SIZE=3

all: daemon

build: 
	mvn package

run: build
	docker pull purvesta/cs455-p3:latest
	docker-compose up --scale server=$(SIZE)

daemon:
	docker pull purvesta/cs455-p3:latest
	docker-compose up -d --scale server=$(SIZE)

clean:
	mvn clean
	docker-compose down
