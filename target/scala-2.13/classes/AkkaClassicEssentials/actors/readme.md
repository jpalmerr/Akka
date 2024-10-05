# Actors

### with traditional objects
    - we store their state as data
    - we call their methods 

### with actors

    - we store their state as data
    - we send messages to them, asynchronously

Actors are objects we can't access directly, but only send messages to

# How Akka workds

- Akka has a thread pool that it shares with actors

Actor has a 
    - message handler
    - message queue
its passive -> needs to run on a thread

Akka schedules actors for execution

## Guarantees

Only one thread operates on an actor at any time
- actors are effectively single threaded
- no locks needed

Message delivery guarantees
- at most once deliver (never receive duplicates)
- for any sender-receiver pair, the message order is maintained

If Alice sends Bob message A followed by B
- Bob will never receive duplicates of A or B
- Bob will always receive A before B