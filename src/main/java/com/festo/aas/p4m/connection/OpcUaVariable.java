/*-
 * #%L
 * Papyrus4Manufacturing helpers
 * %%
 * Copyright (C) 2021 - 2022 Festo Didactic SE
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

package com.festo.aas.p4m.connection;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.protocol.opcua.connector.IOpcUaClient;
import org.eclipse.basyx.vab.protocol.opcua.exception.OpcUaException;
import org.eclipse.basyx.vab.protocol.opcua.types.NodeId;
import org.eclipse.basyx.vab.protocol.opcua.types.UnsignedByte;
import org.eclipse.basyx.vab.protocol.opcua.types.UnsignedInteger;
import org.eclipse.basyx.vab.protocol.opcua.types.UnsignedLong;
import org.eclipse.basyx.vab.protocol.opcua.types.UnsignedShort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supplies the current value from a single remote OPC UA variable or
 * writes its value.
 *
 * <p>
 * The value can optionally be cached for a specified amount of time after
 * fetching to increase
 * performance. The cache duration is set during object initialization and can
 * not be changed
 * afterwards.
 */
public class OpcUaVariable implements PropertyValueConsumer, PropertyValueSupplier {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final OpcUaClient client;
  private final NodeId nodeId;
  private final Duration cacheDuration;
  private final Class<?> dataType;

  private Instant cacheTimestamp = Instant.MIN;
  private Object cachedValue;

  /**
   * Creates a new OPC UA variable connecting to the given node using the given
   * client.
   *
   * <p>
   * When using this constructor, the caching functionality is disabled.
   *
   * @param client   The client object to use for communication.
   * @param nodeId   The node whose value to read or write.
   * @param dataType The class matching the type of the OPC UA variable. See table
   *                 at
   *                 {@link IOpcUaClient}.
   */
  public OpcUaVariable(OpcUaClient client, NodeId nodeId, Class<?> dataType) {
    this(client, nodeId, dataType, Duration.ZERO);
  }

  /**
   * Creates a new OPC UA variable connecting to the given node using the given
   * client.
   *
   * @param client        The client object to use for communication.
   * @param nodeId        The node whose value to read or write.
   * @param dataType      The class matching the type of the OPC UA variable. See
   *                      table at
   *                      {@link IOpcUaClient}.
   * @param cacheDuration The maximum age of the cached value before it will be
   *                      refetched during the
   *                      next call to {@link #getValue()}.
   */
  public OpcUaVariable(OpcUaClient client, NodeId nodeId, Class<?> dataType, Duration cacheDuration) {
    this.client = client;
    this.nodeId = nodeId;
    this.cacheDuration = cacheDuration;
    this.dataType = dataType;
  }

  /**
   * Gets the node id of the variable that this object reads or writes.
   *
   * @return The variable's node id.
   */
  public NodeId getNodeId() {
    return nodeId;
  }

  @Override
  public Object getValue() throws ProviderException {
    if (!cacheValid()) {
      logger.debug("Variable '{}' not cached.", nodeId);
      fetchValue();
    } else {
      logger.debug("Variable '{}' read from cache", nodeId);
    }

    return cachedValue;
  }

  @Override
  public void applyValue(Object value) throws ProviderException {
    logger.debug("Writing '{}' to '{}' on {}.", value, nodeId, client.endpoint);

    if (!isCorrectType(value)) {
      String exceptionMessage = String.format(
          "Mismatch between configured type (%s) and type of given value (%s)", dataType, value.getClass());
      throw new IllegalArgumentException(exceptionMessage);
    }

    value = mapBaSyxToUnsigned(value);

    client.writeValue(nodeId, value);
    cacheTimestamp = Instant.now();
  }

  private boolean cacheValid() {
    return cacheTimestamp.plus(cacheDuration).isAfter(Instant.now());
  }

  private void fetchValue() throws OpcUaException {
    logger.debug("Reading value for '{}' from {}.", nodeId, client.endpoint);
    Object value = client.readValue(nodeId);

    if (!isCorrectType(value)) {
      String exceptionMessage = String.format(
          "Mismatch between configured type (%s) and type received from OPC UA server (%s)", dataType, value
              .getClass());
      throw new ProviderException(exceptionMessage);
    }

    cachedValue = mapUnsignedToBaSyx(value);
    cacheTimestamp = Instant.now();
  }

  private Object mapUnsignedToBaSyx(Object value) {
    if (dataType == UnsignedByte.class) {
      return ((UnsignedByte) value).toShort();
    } else if (dataType == UnsignedShort.class) {
      return ((UnsignedShort) value).toInt();
    } else if (dataType == UnsignedInteger.class) {
      return ((UnsignedInteger) value).toLong();
    } else if (dataType == UnsignedLong.class) {
      return ((UnsignedLong) value).toBigInteger();
    } else {
      return value;
    }
  }

  private Object mapBaSyxToUnsigned(Object value) {
    if (dataType == UnsignedByte.class) {
      return ((UnsignedByte) value).toShort();
    } else if (dataType == UnsignedShort.class) {
      return ((UnsignedShort) value).toInt();
    } else if (dataType == UnsignedInteger.class) {
      return ((UnsignedInteger) value).toLong();
    } else if (dataType == UnsignedLong.class) {
      return ((UnsignedLong) value).toBigInteger();
    } else {
      return value;
    }
  }

  private boolean isCorrectType(Object value) {
    return value.getClass() == dataType;
  }
}
