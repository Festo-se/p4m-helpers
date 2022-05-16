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
 * A function which transforms supplier values to a single output value. For use
 * with {@link ConnectedProperty}.
 *
 * <p>
 * This function receives a map of supplier names and the values supplied by
 * them. It should compute the result from these values and return it.
 */
@FunctionalInterface
public interface SupplyFilter {
	Object filter(Map<String, Object> values);
}
