# CS455 p2-identityserver

* Authors: Nate St. George, Tanner Purves
* Class: CS455 or CS555 [Distributed Systems] Section #1 Spring 2020

## Overview

IdentityServer which accepts account requests from users.

## Files
    * IdClient.java     - Client entrypoint file
    * IdServer.java     - Server entrypoint file
    * LoginRequest.java - Source file
    * UserData.java     - Source file
    * mysecurity.policy - Java security policy file
    * pom.xml           - Maven config file
    * README.md         - This file

## Building the code

From within the root directory of the project:

    $ mvn package
    $ java -cp target/p2-1.0-SNAPSHOT-jar-with-dependencies.jar -Djava.security.policy=./mysecurity.policy IdServer [--numport <port#>] [--verbose]

Then in another terminal:
    $ java -cp target/p2-1.0-SNAPSHOT-jar-with-dependencies.jar IdClient --server <serverhost> [--numport <port#] <query>

## Testing

Testing this code was fairly simple. Through simply interacting with the client application and adding debug statements throughout the code, we were able to simply and swiftly test and debug the code. We ran through every feature that was listed on the assignment documentation when testing the program by hand so that we could ensure we had implemented the necessary features.

## Reflection

Throughout the development process, we communicated well as a team. This allowed for easy distribution of the workload. I believe we split up the work on this project quite equally, all while getting to dabble in both sides of this project (server and client).

As far as the development process goes, we simply used a github repository and communication to tackle the project. This was a system that worked quite well in this scenario.

We decided to go with Apache Maven as our build system after including one of the apache cli arg parsing packages. This allowed for us to simply handle dependencies and make building and running the project quite simple.

## Extras

Video Demonstration: TBD
