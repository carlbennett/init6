package com.vilenet.coders.commands

import com.vilenet.channels.User

/**
  * Created by filip on 12/16/15.
  */
case class JoinUserCommand(override val fromUser: User, override val message: String) extends MessageCommand with ChannelCommand
