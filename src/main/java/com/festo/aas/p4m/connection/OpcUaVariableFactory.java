/*
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.festo.aas.p4m.connection;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.basyx.vab.protocol.opcua.connector.IOpcUaClient;
import org.eclipse.basyx.vab.protocol.opcua.types.NodeId;

/**
 * Creates and caches {@link OpcUaVariable} instances.
 *
 * <p>
 * The cache is indexed by {@link OpcUaClient} and {@link NodeId}. The node's
 * data type or cache
 * duration are not taken into account for calculating cache hits or misses.
 */
public class OpcUaVariableFactory {
  private static final Map<OpcUaClient, Map<NodeId, OpcUaVariable>> cache = new HashMap<>();

  /**
   * Retrieves an {@link OpcUaVariable} matching the given client and nodeId from
   * the cache or creates
   * and caches a new one.
   *
   * @param client        The client used to retrieve this variable.
   * @param nodeId        The nodeId of the variable.
   * @param dataType      The variable's type. See {@link IOpcUaClient} for
   *                      details.
   * @param cacheDuration The duration for which the variable's value will be
   *                      cached.
   *
   * @return Either a new or cached {@code OpcUaVariable}.
   */
  public static OpcUaVariable createIfNonexistent(OpcUaClient client, NodeId nodeId, Class<?> dataType,
      Duration cacheDuration) {
    return cache
        .computeIfAbsent(client, k -> new HashMap<>())
        .computeIfAbsent(nodeId, k -> create(client, nodeId, dataType, cacheDuration));
  }

  /**
   * Creates a new {@link OpcUaVariable}. It will never be taken from cache nor
   * will the created
   * instance be cached for future use.
   *
   * @param client        The client used to retrieve this variable.
   * @param nodeId        The nodeId of the variable.
   * @param dataType      The variable's type. See {@link IOpcUaClient} for
   *                      details.
   * @param cacheDuration The duration for which the variable's value will be
   *                      cached.
   *
   * @return A new {@code OpcUaVariable}.
   */
  public static OpcUaVariable create(OpcUaClient client, NodeId nodeId, Class<?> dataType, Duration cacheDuration) {
    return new OpcUaVariable(client, nodeId, dataType, cacheDuration);
  }
}
