## Threaded Server
A Server that takes input from a client and returns answers to basic arithmetic. Server connects clients and Worker threads process the input and give a response to the client. Thread count is dynamically adjusted based on number of clients connecting. This was an assignment for CSCD 467 - Parallel & Cloud Computing at Eastern Washington University. 

## Getting Started
Install Java and compile the files. Run Server.java to start the server, then run Client.java. Alternatively, run ParallelTest.java to simulate 500 clients connecting at once. 

## Tests
ParallelTest.java is a GUI free test that simulates 500 clients connecting to the server through port 9898. After all client threads are finished, it prints statistics about how many clients were accepted or rejected and if any clients were dropped by the server. 

## Contributors

**Robin Deskins** - [rdeskins](https://github.com/rdeskins)
