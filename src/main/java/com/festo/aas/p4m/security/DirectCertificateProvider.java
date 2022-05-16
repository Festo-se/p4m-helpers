/*******************************************************************************
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.festo.aas.p4m.security;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

/**
 * A very simple provider that holds a given key pair and certificate in memory for later retrieval.
 */
public final class DirectCertificateProvider extends CertificateProvider {

    /**
     * Creates a new provider for the given key pair and certificate.
     *
     * @param keyPair     The key pair.
     * @param certificate The certificate.
     */
    public DirectCertificateProvider(KeyPair keyPair, X509Certificate certificate) {
        super();

        setKeyPairAndCertificate(keyPair, certificate);
    }

    @Override
    public void loadInternal() throws GeneralSecurityException {
        // do nothing
    }

    @Override
    protected void handleFallbackSuccessful() {
        // do nothing
    }
}
