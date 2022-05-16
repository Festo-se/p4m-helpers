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

import java.util.Map;

/**
 * A function which transforms a single property value to individual consumer
 * values. For use with {@link ConnectedProperty}.
 *
 * <p>
 * This function receives a single value as well as an empty map. It should
 * compute a new value for each consumer and put it in the map using the
 * consumer's name as key.
 */
@FunctionalInterface
public interface ConsumeFilter {
	void filter(Object newValue, Map<String, Object> valuesByConsumer);
}
