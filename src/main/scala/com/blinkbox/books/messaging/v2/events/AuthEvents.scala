package com.blinkbox.books.messaging.v2.events

import com.blinkbox.books.messaging.MediaType

object AuthEvents {
  val `application/vnd.blinkbox.books.events.client.deregistered.v2+json` = MediaType("application/vnd.blinkbox.books.events.client.deregistered.v2+json")
  val `application/vnd.blinkbox.books.events.client.registered.v2+json` = MediaType("application/vnd.blinkbox.books.events.client.registered.v2+json")
  val `application/vnd.blinkbox.books.events.client.updated.v2+json` = MediaType("application/vnd.blinkbox.books.events.client.updated.v2+json")
  val `application/vnd.blinkbox.books.events.user.authenticated.v2+json` = MediaType("application/vnd.blinkbox.books.events.user.authenticated.v2+json")
  val `application/vnd.blinkbox.books.events.user.registered.v2+json` = MediaType("application/vnd.blinkbox.books.events.user.registered.v2+json")
  val `application/vnd.blinkbox.books.events.user.updated.v2+json` = MediaType("application/vnd.blinkbox.books.events.user.updated.v2+json")

  case class Client(id: Int, name: String, brand: String, model: String, os: String)
  case class User(id: Int, username: String, firstName: String, lastName: String, allowMarketing: Boolean)

  case class ClientDeregistered(client: Client)
  case class ClientRegistered(client: Client)
  case class ClientUpdated(client: Client, previousDetails: Client)
  case class UserAuthenticated(user: User, client: Option[Client])
  case class UserRegistered(user: User)
  case class UserUpdated(user: User, previousDetails: User)
}
