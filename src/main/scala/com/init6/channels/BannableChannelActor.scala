package com.init6.channels

import akka.actor.{ActorRef, Address}
import com.init6.Constants._
import com.init6.channels.utils.BannedMap
import com.init6.coders.commands._
import com.init6.users.UserToChannelCommandAck

/**
  * Created by filip on 11/24/15.
  */
case class ShowBansResponse(chatEvent: ChatEvent) extends Command

trait BannableChannelActor extends ChannelActor {

  // Banned users
  val bannedUsers = BannedMap(limit)

  // Unbans existing names if server drops
  // (probably shouldn't do that)
  override protected def onServerDead(address: Address) = {
    remoteUsersMap
      .get(address)
      .foreach(actors => {
        actors
          .map(users)
          .foreach(bannedUsers -= _.name)
      })

    super.onServerDead(address)
  }

  override def receiveEvent = ({
    case command @ GetChannelUsers =>
      sender() ! ReceivedBannedUsers(bannedUsers.toImmutable)
      super.receiveEvent(command)

    case ReceivedBannedUsers(names) =>
      // Prune received bans. Remove local dead actors
      bannedUsers ++= names
        .filter {
          case (operator, _) => isRemote(operator) || localUsers.contains(operator)
        }

    case command: UserToChannelCommandAck =>
      if (isRemote() || users.contains(sender())) {
        command.command match {
          case KickCommand(kicked, message) =>
            kickAction(sender(), command.userActor, message)
          case BanCommand(banned, message) =>
            banAction(sender(), command.userActor, command.realUsername, message)
          case UnbanCommand(unbanned) =>
            unbanAction(sender(), command.realUsername)
          case ShowUserBans(username) =>
            showBans(command.userActor, command.realUsername)
          case _ => super.receiveEvent(command)
        }
      }
    case ShowChannelBans(channel) =>
      showChannelBans()
  }: Receive)
    .orElse(super.receiveEvent)

  override def add(actor: ActorRef, user: User): User = {
    if (isLocal(actor) && !Flags.isAdmin(user)) {
      val bannedBy = bannedUsers(user.name)
        .filter(actor => {
          if (users.contains(actor)) {
            true
          } else {
            bannedUsers -= actor
            false
          }
        })

      if (bannedBy.nonEmpty) {
        if (isLocal()) {
          sender() ! UserError(YOU_BANNED)
        }
        user
      } else {
        super.add(actor, user)
      }
    } else {
      super.add(actor, user)
    }
  }

  // remove users from banned when server reconnects if they are already in the channel
  // the ops on other servers will have to ban them again.
  override def remoteIn(remoteUserActor: ActorRef, user: User) = {
    bannedUsers -= user.name

    super.remoteIn(remoteUserActor, user)
  }

  override def rem(actor: ActorRef): Option[User] = {
    val userOpt = super.rem(actor)

    bannedUsers -= actor

    userOpt
  }

  def kickAction(kickingActor: ActorRef, kickedActor: ActorRef, message: String) = {
    users.get(kickedActor).fold(
      kickingActor ! UserError(INVALID_USER)
    )(kickedUser => {
      if (Flags.canBan(kickedUser)) {
        if (isLocal(kickingActor)) {
          kickingActor ! UserError(CANNOT_KICK_OPERATOR)
        }
      } else {
        val kicking = users(kickingActor).name

        localUsers ! UserInfo(USER_KICKED(kicking, kickedUser.name, message))
        if (isLocal(kickedActor)) {
          kickedActor ! KickCommand(kicking)
        }
      }
    })
  }

  def banAction(banningActor: ActorRef, bannedActor: ActorRef, banned: String, message: String) = {
    log.info("banAction " + banningActor + " - " + bannedActor + " - " + banned + " - " + sender())
    users.get(banningActor)
      .map(_.name)
      .foreach(banning => {
        users.get(bannedActor).fold({
          bannedUsers += banningActor -> banned
          localUsers ! UserInfo(USER_BANNED(banning, banned, message))
        })(bannedUser => {
          if (Flags.canBan(bannedUser)) {
            if (isLocal(banningActor)) {
              banningActor ! UserError(CANNOT_BAN_OPERATOR)
            }
          } else {
            bannedUsers += banningActor -> banned
            if (isLocal(bannedActor)) {
              bannedActor ! BanCommand(banning)
            }
            localUsers ! UserInfo(USER_BANNED(banning, banned, message))
          }
        })
      })
  }

  def unbanAction(unbanningActor: ActorRef, unbanned: String) = {
    users.get(unbanningActor)
      .map(_.name)
      .foreach(unbanning => {
        if (bannedUsers(unbanned).nonEmpty) {
          bannedUsers -= unbanned
          localUsers ! UserInfo(USER_UNBANNED(unbanning, unbanned))
        } else {
          if (isLocal(unbanningActor)) {
            unbanningActor ! UserError(NOT_BANNED)
          }
        }
      })
  }

  def showChannelBans() = {
    if (bannedUsers.isEmpty) {
      sender() ! ShowBansResponse(UserInfo(s"Nobody is banned in $name."))
    } else {
      sender() ! ShowBansResponse(UserInfoArray(bannedUsers.map {
        case (actor, bannedUsers) =>
          users.get(actor).map(_.name).getOrElse(actor.toString) + ": " + bannedUsers.mkString(", ")
      }.toArray))
    }
  }

  def showBans(banningActor: ActorRef, banning: String) = {
    sender() ! ShowBansResponse(UserInfo(
      bannedUsers.get(banningActor).fold(banning + " has not banned anybody.")(banned => {
        banning + " has banned: " + banned.mkString(", ")
      }))
    )
  }

  override def whoCommand(actor: ActorRef, user: User, opsOnly: Boolean) = {
    if (Flags.isAdmin(user) || bannedUsers(user.name).isEmpty) {
      super.whoCommand(actor, user, opsOnly)
    } else {
      actor ! WhoCommandError(NOT_ALLOWED_TO_VIEW)
    }
  }
}
