SIZE=3

all: build

build: 
	mvn package
	docker build -t purvesta/cs455-p3:latest .

run: build
	docker-compose up --scale server=$(SIZE)

daemon: build
	docker-compose up -d --scale server=$(SIZE)

clean:
	mvn clean
	docker-compose down
