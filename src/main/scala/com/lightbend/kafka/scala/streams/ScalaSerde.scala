/*
 * Copyright 2018 OpenShine SL <https://www.openshine.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.kafka.scala.streams

import org.apache.kafka.common.serialization.{Deserializer => JDeserializer, Serde => JSerde, Serializer => JSerializer}

trait Serde[T] extends JSerde[T] {
  override def deserializer(): JDeserializer[T]

  override def serializer(): JSerializer[T]

  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()
}

trait StatelessScalaSerde[T >: Null] extends JSerde[T] with Serde[T] {
  def serialize(data: T): Array[Byte]
  def deserialize(data: Array[Byte]): Option[T]

  override def deserializer(): Deserializer[T] =
    (data: Array[Byte]) => deserialize(data)

  override def serializer(): Serializer[T] =
    (data: T) => serialize(data)
}

trait Deserializer[T >: Null] extends JDeserializer[T] {
  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def deserialize(topic: String, data: Array[Byte]): T =
    Option(data).flatMap(deserialize).orNull

  def deserialize(data: Array[Byte]): Option[T]
}

trait Serializer[T] extends JSerializer[T] {
  override def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = ()

  override def close(): Unit = ()

  override def serialize(topic: String, data: T): Array[Byte] =
    Option(data).map(serialize).orNull

  def serialize(data: T): Array[Byte]
}
