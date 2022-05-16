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

import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnector;

public class HttpClientFactory {

    private HttpClientFactory() {
        throw new AssertionError("Cannot create instances.");
    }

    /**
     * Creates a new HTTPConnector for the given endpoint.
     *
     * @param endpointUrl The endpoint the client will connect to.
     *
     * @return A {@link HTTPConnector}.
     */
    public HTTPConnector createHttpClient(String endpointUrl) {
        return new HTTPConnector(endpointUrl);
    }
}
