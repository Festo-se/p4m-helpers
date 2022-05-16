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

import java.security.GeneralSecurityException;

import org.eclipse.basyx.vab.protocol.opcua.connector.ClientConfiguration;
import org.eclipse.basyx.vab.protocol.opcua.connector.IOpcUaClient;
import com.festo.aas.p4m.security.CertificateProvider;
import com.festo.aas.p4m.security.SelfSignedCertificateProvider;

/**
 * Creates instances of {@link OpcUaClient}.
 */
public final class OpcUaClientFactory {

    private OpcUaClientFactory() {
        throw new AssertionError("Cannot create instances.");
    }

    /**
     * Creates a new OPC UA client for the given endpoint.
     *
     * @param endpointUrl   The endpoint the client will connect to.
     * @param configuration The configuration for the OPC UA client.
     *
     * @return A preconfigured {@link IOpcUaClient}.
     */
    public static OpcUaClient createClient(String endpointUrl, ClientConfiguration configuration) {
        IOpcUaClient client = IOpcUaClient.create(endpointUrl);
        client.setConfiguration(configuration);

        return new OpcUaClient(client);
    }

    /**
     * Generates a default client configuration for the OPC UA client with the application certificate
     * provided by the given <code>CertificateProvider</code>.
     *
     * @param certificateProvider A {@link CertificateProvider} provides the certificates and keys
     *                            required for OPC UA client identification. For a simple self-signed
     *                            certificate, use {@link SelfSignedCertificateProvider}.
     *
     * @return A new client configuration which can be applied to {@link IOpcUaClient} instances.
     *
     * @throws GeneralSecurityException if the <code>certificateProvider</code> fails to load.
     */
    public static ClientConfiguration createConfiguration(CertificateProvider certificateProvider)
            throws GeneralSecurityException {
        certificateProvider.load();

        ClientConfiguration config = new ClientConfiguration();
        config.setKeyPairAndCertificate(certificateProvider.getKeyPair(), certificateProvider.getCertificate());

        return config;
    }
}
