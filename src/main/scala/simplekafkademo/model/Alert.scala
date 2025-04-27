package simplekafkademo.model

final case class Alert (sensor_id: String, timeStamp: String, alert: String)