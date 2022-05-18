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

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElement;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection;
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaHandler;

/**
 * A special {@link ValueDelegate} for use with
 * {@link SubmodelElementCollection}.
 *
 * <p>
 * This class performs some additional behind-the-scenes conversions, to enable
 * the user to get/set a simple {@code Collection<ISubmodelElement} as the SMC's
 * value.
 */
public class CollectionDelegate extends ValueDelegate<Collection<ISubmodelElement>> {
  @Override
  public void setGetHandler(Supplier<Collection<ISubmodelElement>> getHandler) {
    Supplier<Map<String, ISubmodelElement>> supplier = () -> {
      Collection<ISubmodelElement> collection = getHandler.get();
      return collection.stream().collect(Collectors.toMap(sme -> sme.getIdShort(), sme -> sme));
    };

    lambdaMap.put(VABLambdaHandler.VALUE_GET_SUFFIX, supplier);
  }

  @Override
  public void setSetHandler(Consumer<Collection<ISubmodelElement>> setHandler) {
    Consumer<Map<String, ISubmodelElement>> consumer = (map) -> {
      setHandler.accept(map.values());
    };

    lambdaMap.put(VABLambdaHandler.VALUE_SET_SUFFIX, consumer);
  }
}
