package com.init6.utils

import scala.collection.mutable

/**
 * Created by filip on 10/11/15.
 */
object CaseInsensitiveHashMap {
  def apply[B]() = new CaseInsensitiveHashMap[B]()
}

sealed class CaseInsensitiveHashMap[B] extends mutable.HashMap[String, B] {
  override def +=(kv: (String, B)): this.type = super.+=(kv.copy(kv._1.toLowerCase, kv._2))
  override def -=(key: String): this.type = super.-=(key.toLowerCase)
  override def contains(key: String): Boolean = super.contains(key.toLowerCase)
  override def get(key: String): Option[B] = super.get(key.toLowerCase)
  override def remove(key: String): Option[B] = super.remove(key.toLowerCase)
}

object RealKeyedCaseInsensitiveHashMap {
  def apply[B]() = new RealKeyedCaseInsensitiveHashMap[B]
}

sealed class RealKeyedCaseInsensitiveHashMap[B] extends CaseInsensitiveHashMap[(String, B)] {
  def +=(kv: (String, B)): this.type = +=(kv._1 -> kv)
}

object CaseInsensitiveMultiMap {
  def apply[B]() = new CaseInsensitiveMultiMap[B]
}

sealed class CaseInsensitiveMultiMap[B] extends CaseInsensitiveHashMap[mutable.Set[B]] with mutable.MultiMap[String, B] {

  def +=(kv: (String, B)): this.type = addBinding(kv._1.toLowerCase, kv._2)
  def foreach[C](key: String, f: ((String, B)) => C): Unit = get(key).foreach(_.foreach(f(key, _)))
}
