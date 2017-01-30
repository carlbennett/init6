package com.init6.db

import akka.actor.Props
import com.init6.Constants._
import com.init6.{Init6Component, Init6RemotingActor}
import com.init6.coders.commands.Command
import com.init6.servers.Remotable

/**
  * Created by filip on 12/21/15.
  */
object DAOActor extends Init6Component {
  def apply() = system.actorOf(Props[DAOActor], INIT6_DAO_PATH)
}

case class GetAccount(username: String) extends Command
case class CreateAccount(username: String, passwordHash: Array[Byte]) extends Remotable
case class UpdateAccountPassword(username: String, passwordHash: Array[Byte]) extends Remotable
case class CloseAccount(username: String, reason: String = "") extends Remotable
case class OpenAccount(username: String) extends Remotable
case class DAOCreatedAck(username: String, passwordHash: Array[Byte]) extends Command
case class DAOUpdatedPasswordAck(username: String, passwordHash: Array[Byte]) extends Command
case class DAOClosedAccountAck(username: String, reason: String) extends Command
case class DAOOpenedAccountAck(username: String) extends Command

case class DAOAliasCommand(aliasFrom: String, aliasTo: String) extends Command

class DAOActor extends Init6RemotingActor {

  override val actorPath = INIT6_DAO_PATH

  override def receive: Receive = {
    case CreateAccount(username, passwordHash) =>
      DAO.createUser(username, passwordHash)
      if (isLocal()) {
        sender() ! DAOCreatedAck(username, passwordHash)
      }

    case UpdateAccountPassword(username, passwordHash) =>
      DAO.updateUser(username, password_hash = Some(passwordHash))
      if (isLocal()) {
        sender() ! DAOUpdatedPasswordAck(username, passwordHash)
      }

    case CloseAccount(username, reason) =>
      DAO.updateUser(username, closed = Some(true), closed_reason = Some(reason))
      if (isLocal()) {
        sender() ! DAOClosedAccountAck(username, reason)
      }

    case OpenAccount(username) =>
      DAO.updateUser(username, closed = Some(false), closed_reason = Some(""))
      if (isLocal()) {
        sender() ! DAOOpenedAccountAck(username)
      }

    case channelJoin: DbChannelJoin =>
      DAO.saveChannelJoin(channelJoin)

    case DAOAliasCommand(aliasFrom, aliasTo) =>
      DAO.getUser(aliasFrom)
        .foreach(dbUser => {
          DAO.updateUser(
            username = aliasTo,
            account_id =
          )

        })
  }
}
