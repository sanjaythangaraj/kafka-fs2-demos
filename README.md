# Kafka + Fs2 Demos

1. **Temperature Alert System**

This project demonstrates how to consume data from a `kafka` topic,
process it using `fs2`, and produces results to other kafka topics.

- project package [`src/main/scala/simplekafkademo`](src/main/scala/simplekafkademo)
- refer to project's [`README.md`](src/main/scala/simplekafkademo/README.md) for instructions to run. 

2. **Notification System**

This project illustrates the use of `fs2` streams to consume data from `kafka` topics,
process them concurrently, manage state, and enable integration with external services.

- project package [`src/main/scala/simplertnotifsys`](src/main/scala/simplertnotifsys)
- refer to project's [`README.md`](src/main/scala/simplertnotifsys/README.md) for instructions to run. 