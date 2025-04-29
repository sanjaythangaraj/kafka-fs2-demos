This project illustrates the use of `fs2` streams to consume data from `kafka` topics, 
process them concurrently, manage state, and enable integration with external services.

## Notification System

### Producer Application

`KafkaProducerApp` under `simplertnotifsys.producer` package
- generates random event messages every 5 seconds to the `events` kafka topic.
  - **Message Format**:
  
    ```json
    {
      "eventId":"event307",
      "eventType":"type0",
      "payload":"Event Payload 64"
    }    
    ```

- generates random subscription messages every 10 seconds to the `subscriptions` kafka topic
  - **Message Format**:
  
    ```json
      {
        "userId":"user2",
        "eventType":"type0",
        "action":"subscribe"
      }
    ```
### Notification System (Consumer)

`NotificationSystem` under `simplertnotifsys`
- the subscription stream consumes messages from the `subscriptions` topic and continuously
updates the `SubscriptionState` (which is a `Map`, `userId -> Set[eventType]`)
- the event stream consumes messages from the `events` topic, checks the current `SubscriptionState`, and 
simulates sending notification to the subscribed users (in parallel using `IO.parSequenceN`).
- both the streams run in parallel, with the shared state managed safely via `Ref`.

## Requirements

1. Docker
2. JDK 23
3. sbt 

## Setup

### Run Kafka

```bash
docker run -d --name=kafka -p 9092:9092 apache/kafka
```

### Run `KafkaProducerApp`

In a new terminal,

```bash
sbt "runMain simplertnotifsys.producer.KafkaProducerApp"
```

will produce messages to the `"events"` and `"subscriptions"` topic

Output

```
{"eventId":"event307","eventType":"type0","payload":"Event Payload 64"}
{"eventId":"event611","eventType":"type1","payload":"Event Payload 70"}
{"userId":"user2","eventType":"type0","action":"subscribe"}
{"eventId":"event322","eventType":"type2","payload":"Event Payload 50"}
{"eventId":"event603","eventType":"type2","payload":"Event Payload 1"}
{"userId":"user2","eventType":"type2","action":"unsubscribe"}
{"eventId":"event902","eventType":"type1","payload":"Event Payload 40"}
{"userId":"user3","eventType":"type0","action":"subscribe"}
{"eventId":"event847","eventType":"type0","payload":"Event Payload 96"}
{"eventId":"event773","eventType":"type1","payload":"Event Payload 65"}
{"eventId":"event194","eventType":"type2","payload":"Event Payload 51"}
{"userId":"user2","eventType":"type1","action":"subscribe"}
{"eventId":"event332","eventType":"type1","payload":"Event Payload 68"}
{"eventId":"event624","eventType":"type0","payload":"Event Payload 14"}
{"userId":"user1","eventType":"type1","action":"subscribe"}
{"eventId":"event222","eventType":"type1","payload":"Event Payload 97"}
```

### Run `NotificationSystem`

In a new terminal,

```bash
sbt "runMain simplertnotifsys.NotificationSystem"
```

Output

```
{"eventId":"event307","eventType":"type0","payload":"Event Payload 64"}
{"eventId":"event611","eventType":"type1","payload":"Event Payload 70"}
{"userId":"user2","eventType":"type0","action":"subscribe"}
{"eventId":"event322","eventType":"type2","payload":"Event Payload 50"}
{"eventId":"event603","eventType":"type2","payload":"Event Payload 1"}
{"userId":"user2","eventType":"type2","action":"unsubscribe"}
{"eventId":"event902","eventType":"type1","payload":"Event Payload 40"}
{"userId":"user3","eventType":"type0","action":"subscribe"}
{"eventId":"event847","eventType":"type0","payload":"Event Payload 96"}
Sending notification to user3: Event Payload 96
Sending notification to user2: Event Payload 96
{"eventId":"event773","eventType":"type1","payload":"Event Payload 65"}
{"eventId":"event194","eventType":"type2","payload":"Event Payload 51"}
{"userId":"user2","eventType":"type1","action":"subscribe"}
{"eventId":"event332","eventType":"type1","payload":"Event Payload 68"}
Sending notification to user2: Event Payload 68
{"eventId":"event624","eventType":"type0","payload":"Event Payload 14"}
Sending notification to user2: Event Payload 14
Sending notification to user3: Event Payload 14
{"userId":"user1","eventType":"type1","action":"subscribe"}
{"eventId":"event222","eventType":"type1","payload":"Event Payload 97"}
Sending notification to user1: Event Payload 97
Sending notification to user2: Event Payload 97
```

will consume messages from `"events"` and `"subscriptions"` topic, and simulate sending notifications to users.
