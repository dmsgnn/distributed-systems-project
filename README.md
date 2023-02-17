# Distributed Systems project

This project has been developed for the "Distributed Systems" course, attended during my Master of Science (A.Y. 2021/22) at the Polytechnic University of Milan. The highest possible final grade has been achieved: 4.0/4.0.

## Description

Aim of this project was to implement a distributed transactional key-value store, offering sequential replication consistency and serializable transactional isolation. 
The store has been implemented in Java, using a design able to maximize system performance, in terms of query latency and throughput.

### Group
The project has been developed by me and one other student: Davide Baroffio.
## Requirements
Each transaction is a list of read and write operations. The store is internally partitioned and replicated using a configuration parameter.
Clients can interact with the store by contacting one or more nodes. The store should offer sequential replication consistency and serializable transactional isolation.
Further details can be found in the [project specification](/projects_specification.pdf).

## Assumptions

The following assumptions were made for the development of the project:
1. process and links are reliable
2. client is a trusted element (it does not act maliciously)
3. the [file of configuration](/DS/src/config.xml) is configured in a proper way before launching the system

## Testing

Different tests have been developed in order to verify the fulfillment of sequential replication consistency and serializable transactional isolation requirements and also to do some stress tests of the system. 

## Jars
[Jars](/JAR) allow the launch of Servers and Clients. Details on how to launch them will be defined in the __How to run JAR__ section.\
Be aware that all the servers must be run before clients in order to the system to work properly, and also that the file of configuration must be properly configured and located in the `src` folder, using the following path `/DS/src/config.xml`, being `DS` in the same folder of the JAR .

### How to run JAR

#### Server.jar
Run the file from the terminal by typing:
```
java -jar Server.jar
```
By default the program will use the ID `0`.
You can specify the ID of the server to run by typing it after the file path, like this:
```
java -jar Server.jar ID
```
e.g. `java -jar Server.jar 2`.

#### Client.jar
Run the file from the terminal by typing
```
java -jar Client.jar
```

