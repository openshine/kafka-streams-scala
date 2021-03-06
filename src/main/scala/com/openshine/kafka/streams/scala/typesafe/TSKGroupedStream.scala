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
package com.openshine.kafka.streams.scala.typesafe

import com.openshine.kafka.streams.scala.FunctionConversions._
import com.openshine.kafka.streams.scala.typesafe.ImplicitConverters._
import org.apache.kafka.streams.kstream._

/** Wraps the Java class [[KGroupedStream]] and delegates method calls to the
  * underlying Java object. Makes use of implicit parameters for the
  * [[util.Materialized]] and [[org.apache.kafka.common.serialization.Serde]]
  * instances.
  */
class TSKGroupedStream[K, V](
    override protected[typesafe] val unsafe: KGroupedStream[K, V]
) extends AnyVal
    with TSKType[KGroupedStream, K, V] {

  def aggregate[VR](initializer: => VR, aggregator: (K, V, VR) => VR)(
      implicit materialized: Materialized[K, VR, kvs]
  ): TSKTable[K, VR] =
    unsafe
      .aggregate(
        initializer.asInitializer,
        aggregator.asAggregator,
        materialized
      )
      .safe

  def count(
      implicit materialized: Materialized[K, java.lang.Long, kvs]
  ): TSKTable[K, Long] =
    unsafe
      .count(materialized)
      .mapValues[scala.Long]({ l: java.lang.Long =>
        Long2long(l)
      }.asValueMapper)
      .safe

  def reduce(
      reducer: (V, V) => V
  )(implicit materialized: Materialized[K, V, kvs]): TSKTable[K, V] =
    unsafe
      .reduce(reducer.asReducer, materialized)
      .safe

  def windowedBy(windows: SessionWindows): TSSessionWindowedKStream[K, V] =
    unsafe
      .windowedBy(windows)
      .safe

  def windowedBy[W <: Window](
      windows: Windows[W]
  ): TSTimeWindowedKStream[K, V] =
    unsafe
      .windowedBy(windows)
      .safe
}
