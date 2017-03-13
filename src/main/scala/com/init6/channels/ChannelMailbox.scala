package com.init6.channels

import akka.actor.ActorSystem
import akka.dispatch.{PriorityGenerator, UnboundedStablePriorityMailbox}
import com.init6.coders.commands.{ChannelsCommand, ChatCommand, EmoteCommand, WhoCommand}
import com.typesafe.config.Config

/**
  * Created by filip on 11/7/15.
  */
class ChannelMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedStablePriorityMailbox(
    PriorityGenerator {
      case ChatCommand | EmoteCommand => 1
      case _ => 2
    }
  )

class ChannelsMailbox(settings: ActorSystem.Settings, config: Config)
  extends UnboundedStablePriorityMailbox(
    PriorityGenerator {
      case ChannelsCommand | WhoCommand => 1
      case MrCleanChannelEraser => 3
      case _ => 2
    }
  )
