This project demonstrates how to consume data from a `kafka` topic,
process it using `fs2`, and produces results to other kafka topics.

## Temperature Alert System

### Producer Application

`SensorDataProducer` under `simplekafkademo.producer` package
- generates random temperature readings and sends them to the `"sensor-data"` topic every second.

### Temperature Alert System (Consumer)

`TemperatureAlertSystem` under `simplekafkademo`
- consumes messages from `"sensor-data"` kafka topic.
- parses JSON messages using `circe` to extract temperature data.
- if parsing fails, sends the message to the `"invalid-messages"` topic.
- if the temperature exceeds `35`°C, sends an alert to the "alters" topic.

#### Input

- **Kafka Topic**: `"sensor-data"`
- **Message Format**:

```json
{
  "sensor_id":"sensor2",
  "timestamp":"2025-04-27T14:29:33.350033600Z",
  "temperature":40.10743488305519
}
```

#### Output

- **Kafka Topic**: `alerts`
- **Message Format**:

```json
{
  "sensor_id":"sensor7",
  "timeStamp":"2025-04-27T16:32:00.523324Z",
  "alert":"High temperature: 45.93655979378537"
}
```

## Requirements

1. Docker
2. JDK 23
3. sbt 

## Setup

### Run Kafka

```bash
docker run -d --name=kafka -p 9092:9092 apache/kafka
```

### Run `SensorDataProducer`

In a new terminal,

```bash
sbt "runMain simplekafkademo.producer.SensorDataProducer"
```

will produce messages to the `"sensor-data"` topic

Output

```
Chunk((ProducerRecord(topic = sensor-data, key = sensor5, value = {"sensor_id":"sensor5","timestamp":"2025-04-27T16:45:11.759291600Z","temperature":13.430657673306968}),sensor-data-0@943))
Chunk((ProducerRecord(topic = sensor-data, key = sensor2, value = {"sensor_id":"sensor2","timestamp":"2025-04-27T16:45:12.755456500Z","temperature":7.751780814765697}),sensor-data-0@944))
Chunk((ProducerRecord(topic = sensor-data, key = sensor3, value = {"sensor_id":"sensor3","timestamp":"2025-04-27T16:45:13.757083300Z","temperature":38.1838392040746}),sensor-data-0@945))
Chunk((ProducerRecord(topic = sensor-data, key = sensor5, value = {"sensor_id":"sensor5","timestamp":"2025-04-27T16:45:14.758707200Z","temperature":34.62785675828995}),sensor-data-0@946))
Chunk((ProducerRecord(topic = sensor-data, key = sensor4, value = {"sensor_id":"sensor4","timestamp":"2025-04-27T16:45:15.744974600Z","temperature":42.80561298168626}),sensor-data-0@947))
Chunk((ProducerRecord(topic = sensor-data, key = sensor2, value = {"sensor_id":"sensor2","timestamp":"2025-04-27T16:45:16.747095300Z","temperature":0.12018022192111233}),sensor-data-0@948))
```

### Run `TemperatureAlertSystem`

In a new terminal,

```bash
sbt "runMain simplekafkademo.TemperatureAlertSystem"
```

Output

```
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1050, key = sensor1, value = {"sensor_id":"sensor1","timestamp":"2025-04-27T16:46:58.747453400Z","temperature":1.8573564755802963}, timestamp = Timestamp(createTime = 1745772418747), serializedKeySize = 7, serializedValueSize = 101, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1051, key = sensor1, value = {"sensor_id":"sensor1","timestamp":"2025-04-27T16:46:59.748596600Z","temperature":21.003449917280214}, timestamp = Timestamp(createTime = 1745772419748), serializedKeySize = 7, serializedValueSize = 101, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1052, key = sensor0, value = {"sensor_id":"sensor0","timestamp":"2025-04-27T16:47:00.750827300Z","temperature":35.70370312015055}, timestamp = Timestamp(createTime = 1745772420750), serializedKeySize = 7, serializedValueSize = 100, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1053, key = sensor6, value = {"sensor_id":"sensor6","timestamp":"2025-04-27T16:47:01.752392500Z","temperature":37.95596232643677}, timestamp = Timestamp(createTime = 1745772421752), serializedKeySize = 7, serializedValueSize = 100, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1054, key = sensor1, value = {"sensor_id":"sensor1","timestamp":"2025-04-27T16:47:02.753645900Z","temperature":42.55365344372205}, timestamp = Timestamp(createTime = 1745772422753), serializedKeySize = 7, serializedValueSize = 100, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1055, key = sensor8, value = {"sensor_id":"sensor8","timestamp":"2025-04-27T16:47:03.761769500Z","temperature":0.24536510531064093}, timestamp = Timestamp(createTime = 1745772423761), serializedKeySize = 7, serializedValueSize = 102, leaderEpoch = 0)
ConsumerRecord(topic = sensor-data, partition = 0, offset = 1056, key = sensor4, value = {"sensor_id":"sensor4","timestamp":"2025-04-27T16:47:04.748067700Z","temperature":13.499329559395006}, timestamp = Timestamp(createTime = 1745772424748), serializedKeySize = 7, serializedValueSize = 101, leaderEpoch = 0)
```

will consume messages from `"sensor-data"` topic, and produce messages to `alert` and  `invalid-messages` topic.

- Consume `alerts` topic,

In a new terminal, 

```bash
docker exec -ti kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server :9092 --topic alerts
```

Output (alerts for temp > 35)

```
{"sensor_id":"sensor5","timeStamp":"2025-04-27T16:31:52.522456200Z","alert":"High temperature: 42.97299828436973"}
{"sensor_id":"sensor9","timeStamp":"2025-04-27T16:31:53.506317100Z","alert":"High temperature: 40.78015004385646"}
{"sensor_id":"sensor0","timeStamp":"2025-04-27T16:31:54.508385900Z","alert":"High temperature: 36.59446029141696"}
{"sensor_id":"sensor1","timeStamp":"2025-04-27T16:31:58.519392600Z","alert":"High temperature: 35.99941858641162"}
{"sensor_id":"sensor7","timeStamp":"2025-04-27T16:32:00.523324Z","alert":"High temperature: 45.93655979378537"}
```