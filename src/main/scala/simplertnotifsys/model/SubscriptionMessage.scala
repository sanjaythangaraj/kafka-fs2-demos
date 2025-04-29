package simplertnotifsys.model

final case class SubscriptionMessage(userId: String, eventType: String, action: String)
