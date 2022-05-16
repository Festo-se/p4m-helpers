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

import java.util.List;

import org.eclipse.basyx.vab.protocol.opcua.connector.IOpcUaClient;
import org.eclipse.basyx.vab.protocol.opcua.exception.OpcUaException;
import org.eclipse.basyx.vab.protocol.opcua.types.NodeId;

/**
 * Wrapper around BaSyx' OPC UA client with a simpler API.
 *
 * <h2>Introduction</h2>
 *
 * <p>
 * This class is intended for novice users who are not very experienced with
 * Java programming or the OPC UA protocol. More advanced users may want to use
 * {@link IOpcUaClient} directly.
 *
 * <p>
 * You can use this class to perform three basic operations from OPC UA's
 * feature set:
 * <ul>
 * <li>Read variable nodes
 * <li>Write variable nodes
 * <li>Invoke methods
 * </ul>
 *
 * <p>
 * All of these actions require you to specify so-called <i>NodeIds</i>. This is
 * an OPC UA concept used to uniquely identify a single node on an OPC UA
 * server.
 *
 * <h2>How to work with NodeIds</h2>
 *
 * <p>
 * A <i>NodeId</i> is a structure made up of two components:
 * <ul>
 * <li>A <b>namespace index</b> which is a number that identifies the namespace
 * (think of this like a group) that the node belongs to.
 * <li>An <b>identifier</b> which must be unique within the namespace. The
 * identifier can be a number, a {@code String}, a {@link java.util.UUID} or an
 * array of {@code byte}.
 * </ul>
 *
 * <p>
 * Sometimes, when a NodeId is presented to a human audience, it is written in a
 * special format which is easy to copy &amp; paste. This <b>string-format</b>
 * looks like this: {@code ns=1;i=4211}, where the <b>n</b>ame<b>s</b>pace index
 * is {@code 1} and the identifier is an <b>i</b>nteger with the value
 * {@code 4211}.
 *
 * <p>
 * Often, the easiest way to find out the NodeId of the node you're interested
 * in is to use a generic OPC UA client (a quick web search will help you find a
 * free solution if you don't have one already) that allows you to connect to
 * any OPC UA server, browse it and display the attributes of all of its nodes.
 * It should display the NodeId.
 *
 * <p>
 * Once you have the NodeId, it is a simple matter to create a {@link NodeId}
 * object for use with this client. You can either use one of the constructors
 * {@link NodeId#NodeId} or {@link NodeId#parse(String)} if you have the NodeId
 * in string-format.
 *
 * <pre>{@code
 * NodeId nodeId1 = new NodeId(1, 4211);
 * NodeId nodeId2 = NodeId.parse("ns=1;i=4211");
 * }</pre>
 */
public final class OpcUaClient {

  /**
   * The BaSyx OPC UA client object. Use this if you need more advanced OPA UA
   * features than this class provides.
   */
  public final IOpcUaClient baSyxClient;

  /**
   * The endpoint this client connects to.
   */
  public final String endpoint;

  /**
   * Creates a new wrapper for the given BaSyx OPC UA client.
   *
   * <p>
   * Most users will typically prefer
   * {@link OpcUaClientFactory#createClient(String,
   * org.eclipse.basyx.vab.protocol.opcua.connector.ClientConfiguration)}
   * over this constructor.
   *
   * @param baSyxClient The raw OPC UA client object to wrap.
   */
  public OpcUaClient(IOpcUaClient baSyxClient) {
    this.baSyxClient = baSyxClient;
    endpoint = baSyxClient.getEndpointUrl();
  }

  /**
   * Reads a value from the OPC UA server.
   *
   * <p>
   * Using this method you can read a value from a single variable node on an OPC
   * UA server. You need to know the NodeId and the expected data type.
   *
   * <h2>Example</h2>
   *
   * <p>
   * This example shows how to read an integer value from the server.<br>
   * The node id has the namespace index {@code 1} and the String identifier
   * {@code SomeIntegerNode}. For more information on NodeIds, see
   * {@link OpcUaClient here}.
   *
   * <pre>{@code
   * NodeId integerNodeId = new NodeId(1, "SomeIntegerNode");
   * Object readValue = opcUaClient.readValue(integerNodeId);
   * Integer myInteger = (Integer) readValue;
   * }</pre>
   *
   * @param nodeId The id of the variable to read.
   *
   * @return The value which was read. You have to know what data type to expect.
   *         See
   *         {@link IOpcUaClient here} for details on types.
   *
   * @throws OpcUaException if there is any error during the OPC UA communication.
   *                        If you don't catch
   *                        this exception, it will simply be returned to the
   *                        client which called this
   *                        AAS.
   */
  public Object readValue(NodeId nodeId) {
    return baSyxClient.readValue(nodeId);
  }

  /**
   * Writes a value to the OPC UA server.
   *
   * <p>
   * Using this method you can set the value of a single variable node on an OPC
   * UA server. You need to know the NodeId and must provide a value of the
   * correct data type.
   *
   * <h2>Example</h2>
   *
   * <p>
   * This example shows how to write an integer value on the server.<br>
   * The node id has the namespace index {@code 1} and the String identifier
   * {@code SomeIntegerNode}. For more information on NodeIds, see
   * {@link OpcUaClient here}.
   *
   * <pre>{@code
   * NodeId integerNodeId = new NodeId(1, "SomeIntegerNode");
   * opcUaClient.writeValue(integerNodeId, 5);
   * }</pre>
   *
   * @param nodeId The id of the variable to write.
   * @param value  The new value to write.
   *
   * @throws OpcUaException if there is any error during the OPC UA communication.
   *                        If you don't catch
   *                        this exception, it will simply be returned to the
   *                        client which called this
   *                        AAS.
   */
  public void writeValue(NodeId nodeId, Object value) {
    baSyxClient.writeValue(nodeId, value);
  }

  /**
   * Invokes a method on the OPC UA server.
   *
   * <p>
   * Using this method you can invoke an OPC UA method on a server. You need to
   * know the NodeId of the method itself as well as the one of the object on
   * which to invoke the method.<br>
   * Additionally, should the method require any arguments, must provide a value
   * of the correct data type for each of those.<br>
   * Lastly, if the method returns some data, you should know the data type to
   * expect.
   *
   * <h2>Example</h2>
   *
   * <p>
   * This example shows how to invoke a method which takes an integer and a
   * boolean value and returns a string.<br>
   * The method itself has namespace index 2 and the UUID identifier
   * {@code 3ac02706-286a-4bd8-b0ec-3917a26fe589}.<br>
   * It will be invoked on the object with the namespace index {@code 2} and the
   * integer identifier {@code 300}.<br>
   * For more information on NodeIds, see {@link OpcUaClient here}.
   *
   * <pre>{@code
   * NodeId objectNodeId = new NodeId(2, 300);
   * NodeId methodNodeId = new NodeId(2, UUID.fromString("3ac02706-286a-4bd8-b0ec-3917a26fe589"));
   * int firstArgument = 5;
   * boolean secondArgument = false;
   * List<Object> returnedList = opcUaClient.invokeMethod(objectNodeId, methodNodeId,
   *     firstArgument, secondArgument);
   * String returnedString = (String) returnedList.get(0);
   * }</pre>
   *
   * @param ownerId    The id of the object on which to invoke the method.
   * @param methodId   The id of the method itself.
   * @param parameters The input arguments to the method.
   *
   * @return The values returned by the OPC UA method.
   *
   * @throws OpcUaException if there is any error during the OPC UA communication.
   *                        If you don't catch
   *                        this exception, it will simply be returned to the
   *                        client which called this
   *                        AAS.
   */
  public List<Object> invokeMethod(NodeId ownerId, NodeId methodId, Object... parameters) {
    return baSyxClient.invokeMethod(ownerId, methodId, parameters);
  }
}
