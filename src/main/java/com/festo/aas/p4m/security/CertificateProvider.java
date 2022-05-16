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

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

/**
 * Abstract base for classes that provide an application certificate and a matching key pair.
 *
 * <p>
 * Certificate providers should be created through the static factory methods in this class. After
 * creation, the {@link #load()} method must be invoked on them before the certificate and key pair
 * can be retrieved.
 *
 * <p>
 * A fallback provider can be set which will be invoked automatically, if this provider fails to
 * produce a key pair and certificate.
 */
public abstract class CertificateProvider {
    protected X509Certificate certificate;
    protected KeyPair keyPair;
    protected CertificateProvider fallback;
    protected boolean loaded;

    /**
     * Gets the previously loaded certificate.
     *
     * @return The certificate.
     *
     * @throws IllegalStateException if this method is called before {@link #load()}.
     */
    public final X509Certificate getCertificate() {
        if (!loaded) {
            throw new IllegalStateException("Must call load() first.");
        }

        return certificate;
    }

    /**
     * Gets the previously loaded key pair.
     *
     * @return The key pair.
     *
     * @throws IllegalStateException if this method is called before {@link #load()}.
     */
    public final KeyPair getKeyPair() {
        if (!loaded) {
            throw new IllegalStateException("Must call load() first.");
        }

        return keyPair;
    }

    /**
     * Loads the key pair and certificate into memory, so they can be retrieved from this provider.
     *
     * <p>
     * This method must be called before either {@link #getCertificate()} or {@link #getKeyPair()}.
     *
     * @throws GeneralSecurityException If this provider or its fallback can't provide the key pair and
     *                                  certificate for whatever reason.
     */
    public final void load() throws GeneralSecurityException {
        try {
            loadInternal();
        } catch (GeneralSecurityException e) {
            if (fallback != null) {
                fallback.load();
                setKeyPairAndCertificate(fallback.getKeyPair(), fallback.getCertificate());
                handleFallbackSuccessful();
            } else {
                throw e;
            }
        }

        loaded = true;
    }

    /**
     * Sets a fallback provider which will be used if this one fails to load.
     *
     * <p>
     * Aside from possible log entries or other side effects, usage of the fallback happens
     * transparently to the caller during {@link #load()}. Its key pair and certificate will the
     * available for retrieval from this provider.
     *
     * @param fallback Another certificate provider.
     */
    public void setFallback(CertificateProvider fallback) {
        this.fallback = fallback;
    }

    /**
     * Implementations must override to load the key pair and certificate.
     *
     * <p>
     * This method is called by {@link CertificateProvider#load()}. It should invoke
     * {@link #setKeyPairAndCertificate(KeyPair, X509Certificate)} to set both objects.
     *
     * <p>
     * If it fails with a {@link GeneralSecurityException} and a fallback has been set on this provider
     * the fallback will be automatically loaded and its results stored in this provider.
     *
     * @throws GeneralSecurityException if the provider can't provide the key pair or certificate for
     *                                  any reason.
     */
    protected abstract void loadInternal() throws GeneralSecurityException;

    /**
     * Implementations must override to handle fallbacks.
     *
     * <p>
     * This method is called by {@link CertificateProvider#load()} in case {@link #loadInternal()}
     * failed and the fallback has succeeded. It allows an implementation to store the fallback's
     * results for future use.
     */
    protected abstract void handleFallbackSuccessful();

    /**
     * Sets the key pair and certificate.
     *
     * @param keyPair     The key pair.
     * @param certificate The certificate.
     *
     * @throws NullPointerException if either <i>keyPair</i> or <i>certificate</i> are
     *                              <code>null</code>.
     */
    protected final void setKeyPairAndCertificate(KeyPair keyPair, X509Certificate certificate) {
        this.keyPair = Objects.requireNonNull(keyPair, "keyPair must not be null");
        this.certificate = Objects.requireNonNull(certificate, "certificate must not be null");
    }

    /**
     * Creates a new certificate provider which retrieves keys and certificate from a key store stored
     * in a file.
     *
     * <p>
     * If the file doesn't exist, a new key store is automatically created and saved to that file.
     *
     * @param keyStoreFile     The file where the key store is stored.
     * @param keyStoreType     The type of the key store. If its the default "PKCS12", consider using
     *                         {@link #fromKeyStore(File, String, String, String)}, instead.
     * @param keyStorePassword The password used for opening and saving the key store to disk.
     * @param alias            The alias under which the private key and certificate are stored.
     * @param entryPassword    The password which protects the private key and certificate.
     *
     * @return A new {@link KeyStoreCertificateProvider}.
     *
     * @throws GeneralSecurityException will be either a {@link CertificateException} if the key store
     *                                  contains certificates which can't be loaded, or a
     *                                  {@link NoSuchAlgorithmException} if the integrity of the key
     *                                  store can't be checked.
     * @throws IOException              if there is an error accessing the file.
     */
    public static CertificateProvider fromKeyStore(File keyStoreFile, String keyStoreType, String keyStorePassword,
            String alias,
            String entryPassword) throws IOException, GeneralSecurityException {
        return new KeyStoreCertificateProvider(keyStoreFile, keyStoreType, keyStorePassword, alias, entryPassword);
    }

    /**
     * Creates a new certificate provider which retrieves keys and certificate from a key store stored
     * in a file.
     *
     * <p>
     * If the file doesn't exist, a new key store is automatically created and saved to that file.
     *
     * <p>
     * The key store is assumed to have the type "PKCS12". To specify another type, please use
     * {@link #fromKeyStore(File, String, String, String, String)}.
     *
     * @param keyStoreFile     The file where the key store is stored.
     * @param keyStorePassword The password used for opening and saving the key store to disk.
     * @param alias            The alias under which the private key and certificate are stored.
     * @param entryPassword    The password which protects the private key and certificate.
     *
     * @return A new {@link KeyStoreCertificateProvider}.
     *
     * @throws GeneralSecurityException will be either a {@link CertificateException} if the key store
     *                                  contains certificates which can't be loaded, or a
     *                                  {@link NoSuchAlgorithmException} if the integrity of the key
     *                                  store can't be checked.
     * @throws IOException              if there is an error accessing the file.
     */
    public static CertificateProvider fromKeyStore(File keyStoreFile, String keyStorePassword, String alias,
            String entryPassword) throws IOException, GeneralSecurityException {
        return new KeyStoreCertificateProvider(keyStoreFile, keyStorePassword, alias, entryPassword);
    }

    /**
     * Creates a new certificate provider which retrieves keys and certificate from a key store object.
     *
     * @param keyStore      The key store to search.
     * @param alias         The alias under which the private key and certificate are stored.
     * @param password The password which protects the private key and certificate.
     *
     * @return A new {@link KeyStoreCertificateProvider}.
     */
    public static CertificateProvider fromKeyStore(KeyStore keyStore, String alias, String password) {
        return new KeyStoreCertificateProvider(keyStore, alias, password);
    }

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
     *
     * @return A new {@link SelfSignedCertificateProvider}.
     */
    public static CertificateProvider createSelfSigned(String commonName, String organization,
            String organizationalUnit, String countryCode, String locality, String state, String applicationUri) {
        return new SelfSignedCertificateProvider(commonName, organization, organizationalUnit, countryCode, locality,
                state, applicationUri);
    }

    /**
     * Creates a simple provider which holds the given keys and certificate in memory for later
     * retrieval.
     *
     * @param keyPair     The key pair.
     * @param certificate The certificate.
     *
     * @return A new {@link DirectCertificateProvider}.
     */
    public static CertificateProvider ofKeyPairAndCertificate(KeyPair keyPair, X509Certificate certificate) {
        return new DirectCertificateProvider(keyPair, certificate);
    }
}
