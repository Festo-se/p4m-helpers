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

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.Submodel;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaProvider;

/**
 * Wraps a {@link Submodel} object with convenience methods for reading or
 * writing elements backed by {@link ValueDelegate}.
 *
 * <p>
 * What Papyrus4Manufacturing names "dynamic elements" are elements which are in
 * fact backed by the {@link ValueDelegate} class. In practical terms that means
 * that these elements' values aren't statically stored but custom code is
 * invoked every time they are read or written.<br>
 * In technical terms it means that their value field is actually a map
 * containing a getter and setter function.<br>
 * When accessing such an element's value directly through the {@link Submodel}
 * class, these getter and setter functions aren't propertly resolved. The map
 * is simply returned to the user which is rarely wanted or even expected.
 *
 * <p>
 * This wrapper helps users read or write all elements, irrespective of whether
 * they are dynamic or static, through the {@link #getValue(String...)} and
 * {@link #setValue(Object, String...)} methods.
 *
 * <p>
 * If the user would like access to the submodel element itself, not it's value,
 * they can use {@link #getSubmodelElement(String...)}.
 *
 * <p>
 * To bypass the wrapper entirely, {@link #getSubmodel()} returns the wrapped
 * submodel.
 *
 * <h2>About idShorts</h2>
 *
 * <p>
 * Some methods in this class have a single varargs parameter named
 * {@code idShorts}. This is a sequence of idShorts which resolve to the element
 * you want to access. Think of it as the path from the submodel root to the
 * element.
 *
 * <p>
 * For elements which are contained directly in the submodel root, this is
 * simply their idShort. For elements which are contained in submodel element
 * collections, this is the parent collection's idShort followed by the
 * element's idShort. This chain can be as long as needed for nested
 * collections.
 *
 * <p>
 * <b>Example:</b><br>
 * Assume a submodel with these contents:<br>
 *
 * <pre>
 * MySubmodel
 *     - OuterCollection
 *     - InnerCollection
 *     - DeeplyNestedProperty
 *     - PropertyInOuter
 *     - PropertyInRoot
 * </pre>
 *
 * <p>
 * The {@code idShorts} parameter for these properties would be:<br>
 * For {@code PropertyInRoot}: <code>{"PropertyInRoot"}</code><br>
 * For {@code PropertyInOuter}: <code>{"OuterCollection",
 * "PropertyInOuter"}</code><br>
 * For {@code DeeplyNestedProperty}:
 * <code>{"OuterCollection", "PropertyInOuter", "DeeplyNestedProperty"}</code>
 */
public final class SubmodelWrapper {
  private final VABLambdaProvider lambdaProvider;
  private final Submodel submodel;

  /**
   * Creates a new wrapper for the given submodel.
   *
   * @param submodel The submodel to wrap.
   */
  public SubmodelWrapper(Submodel submodel) {
    this.submodel = submodel;
    this.lambdaProvider = new VABLambdaProvider(submodel);
  }

  /**
   * Gets the wrapped submodel. Use this to bypass the wrapper.
   *
   * @return The wrapped submodel.
   */
  public Submodel getSubmodel() {
    return submodel;
  }

  /**
   * Gets a submodel element object.
   *
   * <p>
   * <b>Note:</b> The returned object in no way benefits from this wrapper. It's
   * as if you got the element from the submodel directly and all the difficulties
   * related to dynamic elements apply.<br>
   * Use this when you either know the element isn't dynamic or you want to access
   * other attributes of the element, besides its value.
   *
   * @param  idShorts The path of idShorts to the element to get.
   *
   * @return          The {@link ISubmodelElement} object.
   */
  public ISubmodelElement getSubmodelElement(String... idShorts) {
    return submodel.getSubmodelElement(generatePath(idShorts));
  }

  /**
   * Gets the value of a submodel element.
   *
   * <p>
   * This method works for both dynamic and static elements, so it should be
   * preferred over getting the submodel element through
   * {@link #getSubmodelElement(String...)} and accessing its value directly.
   *
   * @param  idShorts The path of idShorts to the element.
   *
   * @return          The value of the element as an Object. Must be cast to the
   *                  correct type.
   */
  public Object getValue(String... idShorts) {
    String path = generatePath("submodelElements/", "/value", idShorts);
    return lambdaProvider.getValue(path);
  }

  /**
   * Sets the value of a submodel element.
   *
   * <p>
   * This method works for both dynamic and static elements, so it should be
   * preferred over getting the submodel element through
   * {@link #getSubmodelElement(String...)} and accessing its value directly.
   *
   * @param value    The new value to set.
   * @param idShorts The path of idShorts to the element.
   */
  public void setValue(Object value, String... idShorts) {
    String path = generatePath("submodelElements/", "/value", idShorts);
    lambdaProvider.setValue(path, value);
  }

  private String generatePath(String... idShorts) {
    return generatePath(null, null, idShorts);
  }

  private String generatePath(String prefix, String suffix, String... idShorts) {
    StringBuilder sb = new StringBuilder(prefix);
    sb.append(String.join("/", idShorts));
    if (suffix != null) {
      sb.append(suffix);
    }
    return sb.toString();
  }
}
