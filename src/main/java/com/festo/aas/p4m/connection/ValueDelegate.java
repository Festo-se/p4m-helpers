/*******************************************************************************
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
import org.eclipse.basyx.vab.modelprovider.lambda.VABLambdaHandler;

public class ValueDelegate<T> {
    private final Supplier<T> defaultGetHandler = () -> { throw new UnsupportedOperationException(); };
    private final Consumer<T> defaultSetHandler = (value) -> { throw new UnsupportedOperationException(); };
    protected final Map<String, Object> lambdaMap;

    protected ValueDelegate() {
        lambdaMap = new HashMap<>();
        setGetHandler(defaultGetHandler);
        setSetHandler(defaultSetHandler);
    }

    public static <T> ValueDelegate<T> installOn(SubmodelElement submodelElement) {
        ValueDelegate<T> delegate = new ValueDelegate<>();
        submodelElement.put(Property.VALUE, delegate.lambdaMap);
        return delegate;
    }

    public static ValueDelegate<Collection<ISubmodelElement>> installOn(SubmodelElementCollection submodelElement) {
        CollectionDelegate delegate = new CollectionDelegate();
        submodelElement.put(Property.VALUE, delegate.lambdaMap);
        return delegate;
    }

    public void setGetHandler(Supplier<T> getHandler) {
        lambdaMap.put(VABLambdaHandler.VALUE_GET_SUFFIX, getHandler);
    }

    public void setSetHandler(Consumer<T> setHandler) {
        lambdaMap.put(VABLambdaHandler.VALUE_SET_SUFFIX, setHandler);
    }
}
