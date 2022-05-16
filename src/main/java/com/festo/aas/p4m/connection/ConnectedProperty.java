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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.Property;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetype.ValueType;

/**
 * This subclass of Property can be used as a drop-in replacement when the
 * property must be connected to an asset. It registers two handlers which get
 * invoked by the BaSyx SDK whenever the property value is read or written.
 *
 * <p>This concept is related to {@link ValueDelegate} but the implementation
 * is very different. Use this class, when only the value of a single property
 * must be generated dynamically (often by reading OPC UA variables and
 * performing some calculations on them). Use <code>ValueDelegate</code> when
 * the value of any other type of <i>SubmodelElement</i> must be computed at
 * runtime.
 *
 * <p>
 * If the AAS property doesn't map one-to-one to an identical asset property,
 * then filter functions can be provided for both get and set operations. The
 * <i>supply filter</i> maps from one or multiple asset properties to this
 * property's value. Conversely, the <i>consume filter</i> maps an updated
 * property value to one or more asset properties.
 *
 * <p>
 * If more than one {@link PropertyValueSupplier} is added, a <i>supply
 * filter</i> must be set, as well. The same is true for
 * {@link PropertyValueConsumer} and the <i>consume filter</i>.
 */
public class ConnectedProperty extends Property {
    private final Map<String, PropertyValueSupplier> suppliers = new HashMap<>();
    private final Map<String, PropertyValueConsumer> consumers = new HashMap<>();
    private SupplyFilter supplyFilter;
    private ConsumeFilter consumeFilter;

    /**
     * Create a new empty ConnectedProperty.
     */
    public ConnectedProperty() {
        super();
        initializeHandlers();
    }

    /**
     * Create a <code>ConnectedProperty</code> with the given <i>idShort</i>
     * and value type.
     *
     * @param idShort The <i>idShort</i> of this property.
     * @param valueType The data type of the property's value.
     */
    public ConnectedProperty(String idShort, ValueType valueType) {
        super(idShort, valueType);
        initializeHandlers();
    }

    @Override
    public synchronized Object getValue() {
        if (suppliers.size() == 0) {
            throw new IllegalStateException("Must specify at least one connection.");
        }

        if (suppliers.size() > 1 && supplyFilter == null) {
            throw new IllegalStateException("Must provide a transformer function when adding multiple connections.");
        }

        Map<String, Object> newValues = fetchConnectedValues();

        Object newValue;
        if (supplyFilter == null) {
            newValue = newValues.values().iterator().next();
        } else {
            newValue = supplyFilter.filter(newValues);
        }

        return newValue;
    }

    @Override
    public synchronized void setValue(Object value) {
        if (consumers.size() == 0) {
            throw new IllegalStateException("Must specify at least one connection.");
        }

        if (consumers.size() > 1 && consumeFilter == null) {
            throw new IllegalStateException("Must provide a transformer function when adding multiple connections.");
        }

        if (consumeFilter == null) {
            consumers.values().iterator().next().applyValue(value);
        } else {
            Map<String, Object> valuesByConsumer = new HashMap<>(consumers.size(), 1);
            consumeFilter.filter(value, valuesByConsumer);
            valuesByConsumer.forEach((name, val) -> applyValueToConsumer(name, val));
        }
    }

    /**
     * Adds a value supplier to this property.
     *
     * <p>
     * If this property is to be read later, at least one value supplier must be added.
     *
     * @param name Uniquely identifies this value supplier among all suppliers added to this property.
     * @param supplier The value supplier to add.
     */
    public void addPropertyValueSupplier(String name, PropertyValueSupplier supplier) {
        Objects.requireNonNull(name);
        if (suppliers.containsKey(name)) {
            throw new IllegalArgumentException("A PropertyValueSupplier with that name already exists.");
        }

        suppliers.put(name, supplier);
    }

    /**
     * Adds a value consumer to this property.
     *
     * <p>
     * If this property is to be written later, at least one value consumer must be added.
     *
     * @param name Uniquely identifies this value consumer among all consumers added to this property.
     * @param consumer The value consumer to add.
     */
    public void addPropertyValueConsumer(String name, PropertyValueConsumer consumer) {
        Objects.requireNonNull(name);
        if (suppliers.containsKey(name)) {
            throw new IllegalArgumentException("A PropertyValueConsumer with that name already exists.");
        }

        consumers.put(name, consumer);
    }

    /**
     * Sets a filter function to transform the supplier values into the value of
     * this property.
     *
     * @param filter The filter function.
     */
    public void setSupplyFilter(SupplyFilter filter) {
        supplyFilter = filter;
    }

    /**
     * Sets a filter function to transform this property's value into consumer
     * values.
     *
     * @param filter The filter function.
     */
    public void setConsumeFilter(ConsumeFilter filter) {
        consumeFilter = filter;
    }

    private void initializeHandlers() {
        ValueDelegate<Object> delegate = ValueDelegate.installOn(this);
        delegate.setGetHandler(this::getValue);
        delegate.setSetHandler(this::setValue);
    }

    private Map<String, Object> fetchConnectedValues() {
        Map<String, Object> valuesBySupplier = new HashMap<>();

        for (Map.Entry<String, PropertyValueSupplier> s : suppliers.entrySet()) {
            valuesBySupplier.put(s.getKey(), s.getValue().getValue());
        }

        return valuesBySupplier;
    }

    private void applyValueToConsumer(String name, Object value) {
        if (!consumers.containsKey(name)) {
            throw new IllegalArgumentException("'" + name + "' is not a known PropertyValueConsumer.");
        }

        consumers.get(name).applyValue(value);
    }
}
