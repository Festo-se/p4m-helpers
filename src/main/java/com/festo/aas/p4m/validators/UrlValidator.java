/*
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.festo.aas.p4m.validators;

/**
 * Validates that a string conforms to URL syntax.
 */
public class UrlValidator implements Validator<String> {
  private final org.apache.commons.validator.routines.UrlValidator validator =
      new org.apache.commons.validator.routines.UrlValidator(
          org.apache.commons.validator.routines.UrlValidator.ALLOW_LOCAL_URLS
          + org.apache.commons.validator.routines.UrlValidator.ALLOW_ALL_SCHEMES);

  public UrlValidator() {
    // Do nothing
  }

  @Override
  public boolean isValid(String value) {
    return validator.isValid(value);
  }
}
