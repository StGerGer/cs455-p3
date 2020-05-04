# CS455 p3-identityserver

* Authors: Nate St. George, Tanner Purves
* Class: CS455 or CS555 [Distributed Systems] Section #1 Spring 2020

## Overview

Distributed IdentityServer which accepts account requests from users.

## Files
    * IdClient.java      - Client entrypoint file
    * IdServer.java      - Server entrypoint file
    * ServerRequest.java - Source file
    * UserData.java      - Source file
    * mysecurity.policy  - Java security policy file
    * pom.xml            - Maven config file
    * Dockerfile         - Build file
    * docker-compose.yml - Build file
    * Makefile           - Build file
    * README.md          - This file

## Building the code

From within the root directory of the project:

    $ make [SIZE=<num_servers] // By default, 3 servers are generated unless otherwise specified with the SIZE parameter
    $ java -cp target/p3-1.0-SNAPSHOT-jar-with-dependencies.jar IdClient --server <serverhost> [--numport <port#] <query>

When wanting to take down servers and clean up directory, from the root directory simply run:
    $ make clean

## Testing

Testing this code was a bit different than the previous project. Initially it was trying to get the docker containers to be able to see each other and communicate through the RMI port. Then it was checking the logs to see if our DEBUG statements indicated that they had performed correctly when it came to elections and server discovery. However after looking through each of the feature requirements, we were able to ensure that we tested correctly.

## Reflection

Once again we communicated quite well throughout this process of developing this project. We were able to split up the worklaod evenly and work together well enough as a team to put together a nearly finished product. We were able to utilize Docker and docker-compose for simple and effective scalability with our servers.

As far as the development process goes, we simply used a github repository and communication to tackle the project as well as Dockerhub to store our docker images. This worked very well for this project.

This project was a lot of fun to work on and even as it comes to a close I am thinking of ways to better it... a load-balancer maybe? Thank you for a fantastic class and a fantastic learning experience :)

## Extras

Video Demonstration: [Video] (https://youtu.be/YK8DRq6XLSo)
