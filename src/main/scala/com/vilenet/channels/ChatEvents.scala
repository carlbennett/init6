package com.vilenet.channels

import akka.actor.ActorRef
import akka.io.Tcp.Event
import com.vilenet.coders.telnet.{ReturnableCommand, Command}

/**
 * Created by filip on 9/20/15.
 */
trait ChatEvent extends Event with Command

case class UserIn(user: User) extends ChatEvent
case class UserJoined(user: User) extends ChatEvent
case class UserLeft(user: User) extends ChatEvent
case class UserWhisperedFrom(user: User, message: String) extends ChatEvent
case class UserTalked(user: User, message: String) extends ChatEvent
case class UserBroadcast(message: String) extends ChatEvent
case class UserChannel(user: User, channelName: String) extends ChatEvent
case class UserFlags(user: User) extends ChatEvent
case class UserWhisperedTo(user: User, message: String) extends ChatEvent
case class UserInfo(message: String) extends ChatEvent with ReturnableCommand
case class UserError(message: String) extends ChatEvent
case object UserNull extends ChatEvent
case class UserName(name: String) extends ChatEvent
case class UserEmote(user: User, message: String) extends ChatEvent

case class UserSentChat(user: String, message: String) extends ChatEvent
case class UserSentEmote(user: String, message: String) extends ChatEvent
case class Designate(user: String, mesasge: String) extends ChatEvent
case class UserSwitchedChat(actor: ActorRef, user: User, channel: String) extends ChatEvent
case class ChatEmptied(channel: String) extends ChatEvent