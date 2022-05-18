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

package com.festo.aas.p4m.validators;

/**
 * Validates that an integer is greater than zero.
 */
public class PositiveIntegerValidator implements Validator<Integer> {

  public PositiveIntegerValidator() {
    // Do nothing
  }

  @Override
  public boolean isValid(Integer value) {
    return value > 0;
  }

}
