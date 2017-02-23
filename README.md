# saga-lagom

An example of using Lagom's Persistent Entities + Message Broker to implement a Saga Pattern.

Saga:
   
    "A process that coordinates and routes messages between bounded contexts and aggregates,
     also an alternative to using a distributed transaction for managing
     a long-running business process."


Saga Service will aggregate the results of two other Lagom Services:

  * ExOneService
  * ExTwoService
  
  
  
Saga Service communicates with these services via a Message Broker (Kafka). The State of the process is
  maintained in the SagaEntity.
  
  
  
  
  


