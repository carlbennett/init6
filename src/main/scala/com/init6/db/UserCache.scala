package com.init6.db

import java.util.concurrent.{Executors, TimeUnit}

import com.init6.Config
import com.init6.utils.CaseInsensitiveHashMap

import scala.collection.mutable
import scala.util.Try

/**
  * Created by filip on 12/13/15.
  */
private object UserCache {

  private val cache = CaseInsensitiveHashMap[DbUser]()
  private val inserted = mutable.HashSet[String]()
  private val updated = mutable.HashSet[String]()

  private val executorService = Executors.newSingleThreadScheduledExecutor()

  private val dbUpdateThread = new Runnable {
    override def run() = {
      Try {
        DAO.saveInserted(cache.filterKeys(inserted.contains).values.toSet)
        inserted.clear()
      }
      Try {
        DAO.saveUpdated(cache.filterKeys(updated.contains).values.toSet)
        updated.clear()
      }
    }
  }

  def apply(dbUsers: List[DbUser]) = {
    cache ++= dbUsers.map(dbUser => dbUser.username -> dbUser)

    val updateInterval = Config().Database.batchUpdateInterval
    executorService.scheduleWithFixedDelay(dbUpdateThread, updateInterval, updateInterval, TimeUnit.SECONDS)
  }

  def close() = {
    executorService.shutdown()
    dbUpdateThread.run()
  }

  def get(username: String) = cache.get(username)

  def insert(username: String, password_hash: Array[Byte]) = {
    val newUser = username.toLowerCase
    cache += newUser -> DbUser(username = newUser, password_hash = password_hash)
    inserted += username.toLowerCase
  }

  def update(username: String, dbUser: DbUser) = {
    get(username).foreach(originalDbUser => {
      if (originalDbUser != dbUser) {
        cache += username -> dbUser
        updated += username.toLowerCase
      }
    })
  }
}
