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

import org.eclipse.basyx.vab.exception.provider.ProviderException;

/**
 * Gets the current value of some property from the asset.
 */
public interface PropertyValueSupplier {
  /**
   * Gets the latest value from the asset.
   *
   * <p>
   * The implementation is allowed to cache previously fetched values. See the
   * documentation of the implementing class for information on caching behavior.
   *
   * @return The current value.
   * @throws ProviderException If the communication with the asset failed.
   */
  Object getValue() throws ProviderException;
}
