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
import java.security.cert.CertificateException;

import org.eclipse.basyx.vab.protocol.opcua.CertificateHelper;

/**
 * A provider which generated a new key pair and self-signed X.509 certificate with a custom subject
 * DN.
 */
public final class SelfSignedCertificateProvider extends CertificateProvider {
    private final String commonName;
    private final String organization;
    private final String organizationalUnit;
    private final String countryCode;
    private final String locality;
    private final String state;
    private final String applicationUri;

    /**
     * Creates a new provider which generated a certificate with the given subject info.
     *
     * @param commonName         The common name (CN) of the certificate's subject. Makes up part of its
     *                           distinguished name (DN).
     * @param organization       The organization (O) of the certificate's subject. Makes up part of its
     *                           distinguished name (DN).
     * @param organizationalUnit The organizational unit (OU) of the certificate's subject. Makes up
     *                           part of its distinguished name (DN).
     * @param countryCode        The country code (C) of the certificate's subject. Makes up part of its
     *                           distinguished name (DN).
     * @param locality           The locality (L) of the certificate's subject. Makes up part of its
     *                           distinguished name (DN).
     * @param state              The state (ST) of the certificate's subject. Makes up part of its
     *                           distinguished name (DN).
     * @param applicationUri     The application URI (CN) of the certificate's subject. Will be added as
     *                           a <i>subject alternative name</i>.
     */
    public SelfSignedCertificateProvider(String commonName, String organization, String organizationalUnit,
            String countryCode,
            String locality, String state, String applicationUri) {
        super();

        this.commonName = commonName;
        this.organization = organization;
        this.organizationalUnit = organizationalUnit;
        this.countryCode = countryCode;
        this.locality = locality;
        this.state = state;
        this.applicationUri = applicationUri;
    }

    @Override
    public void loadInternal() throws GeneralSecurityException {
        CertificateHelper helper = new CertificateHelper();
        if (commonName != null) {
            helper.setCommonName(commonName);
        }

        if (organization != null) {
            helper.setOrganization(commonName);
        }

        if (organizationalUnit != null) {
            helper.setOrganizationalUnit(organizationalUnit);
        }

        if (countryCode != null) {
            helper.setCountryCode(countryCode);
        }

        if (locality != null) {
            helper.setLocality(locality);
        }

        if (state != null) {
            helper.setState(state);
        }

        if (applicationUri != null) {
            helper.setApplicationUri(applicationUri);
        }

        try {
            helper.build();
        } catch (CertificateException e) {

        }

        keyPair = helper.getKeyPair();
        certificate = helper.getCertificate();
    }

    @Override
    protected void handleFallbackSuccessful() {
        // do nothing
    }

}
