/*******************************************************************************
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.festo.aas.p4m.validators;

import org.apache.commons.validator.routines.RegexValidator;

public class IdShortValidator implements Validator<String> {
	private final RegexValidator validator = new RegexValidator("^[a-zA-Z]\\w*$");

	public IdShortValidator() {
	    // Do nothing
	}

	@Override
	public boolean isValid(String value) {
		return validator.isValid(value);
	}

}
