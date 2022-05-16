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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueTypeHelper;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaHandler;

/**
 * Allows getting and setting submodel element values through custom handler
 * methods.
 *
 * <p>
 * Normally, submodel element values are stored in an internal data structure.
 * That's insufficient, when a value should be calculated at runtime.<br>
 * This class makes use of BaSyx' {@link VABLambdaHandler} to enable easy
 * setting of custom getter/setter methods. Once set, these methods are
 * automatically invoked by BaSyx when the submodel element is read or written.
 *
 * <h2>Example</h2>
 *
 * <p>
 * This example creates a property and installs a {@code ValueDelegate} on
 * it.
 * It then sets a getter method.<br>
 * When an AAS client reads this property, they would receive a random number
 * between 0 and 1000.
 *
 * <pre>{@code
 * Property myProp = new Property("myProp", ValueType.Int32);
 * ValueDelegate<Integer> delegate = ValueDelegate.installOn(myProp);
 * delegate.setGetHandler(() -> Math.round(Math.random() * 1000));
 * }</pre>
 */
public class ValueDelegate<T> {
  private final Supplier<T> defaultGetHandler = () -> {
    throw new UnsupportedOperationException();
  };
  private final Consumer<T> defaultSetHandler = (value) -> {
    throw new UnsupportedOperationException();
  };
  protected final Map<String, Object> lambdaMap;

  protected ValueDelegate() {
    lambdaMap = new HashMap<>();
    setGetHandler(defaultGetHandler);
    setSetHandler(defaultSetHandler);
  }

  /**
   * Create and install a new {@code ValueDelegate} on a submodel element.
   *
   * <p>
   * This delegate comes with default getters and setters which throw
   * {@link UnsupportedOperationException}. These default handlers should be
   * replaced using {@link setGetHandler} and/or {@link setSetHandler}.
   *
   * @param <T>             The value's data type. If {@code submodelElement} is a
   *                        {@link Property}, then this should be the Java type
   *                        matching its {@link ValueType}. See
   *                        {@link ValueTypeHelper} for how BaSyx maps
   *                        {@code ValueType} to JVM types.
   * @param submodelElement The submodel element whose value must be dynamically
   *                        derived.
   * @return The new delegate.
   */
  public static <T> ValueDelegate<T> installOn(SubmodelElement submodelElement) {
    ValueDelegate<T> delegate = new ValueDelegate<>();
    submodelElement.put(Property.VALUE, delegate.lambdaMap);
    return delegate;
  }

  /**
   * Create and install a new {@code ValueDelegate} on a submodel element
   * collection.
   *
   * <p>
   * This delegate comes with default getters and setters which throw
   * {@link UnsupportedOperationException}. These default handlers should be
   * replaced using {@link setGetHandler} and/or {@link setSetHandler}.
   *
   * <p>
   * This is a special case, where additional processing is done behind the scenes
   * in order to convert the {@code Collection<ISubmodelElement} type to something
   * BaSyx can understand.
   *
   * @param submodelElementCollection The submodel element collection whose
   *                                  content must be dynamically
   *                                  derived.
   * @return The new delegate.
   */
  public static ValueDelegate<Collection<ISubmodelElement>> installOn(
      SubmodelElementCollection submodelElementCollection) {
    CollectionDelegate delegate = new CollectionDelegate();
    submodelElementCollection.put(Property.VALUE, delegate.lambdaMap);
    return delegate;
  }

  /**
   * Sets a value getter on the delegate.
   *
   * @param getHandler A method supplying a value of type {@code T}.
   */
  public void setGetHandler(Supplier<T> getHandler) {
    lambdaMap.put(VABLambdaHandler.VALUE_GET_SUFFIX, getHandler);
  }

  /**
   * Sets a value setter on the delegate.
   *
   * @param setHandler A method consuming a value of type {@code T}.
   */
  public void setSetHandler(Consumer<T> setHandler) {
    lambdaMap.put(VABLambdaHandler.VALUE_SET_SUFFIX, setHandler);
  }
}
