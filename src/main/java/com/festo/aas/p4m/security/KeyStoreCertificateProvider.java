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

package com.festo.aas.p4m.security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves a key pair and certificate from a key store.
 *
 * <p>
 * The key store can either be set directly as an object or a file can be
 * specified where the key
 * store resides on disk. <br>
 * An alias is used to identify the correct private key and certificate in the
 * key store.
 *
 * <p>
 * If the key store doesn't contain a private key and certificate under the
 * given alias and the
 * fallback succeeds, its results are then stored in the key store under that
 * alias. If the key
 * store is loaded from a file, it is even saved to disk. Future attempts to
 * retrieve the keys and
 * certificate from this same key store should succeed.
 */
public final class KeyStoreCertificateProvider extends CertificateProvider {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String alias;
  private final char[] keyStorePassword;
  private final char[] entryPassword;
  private final File backingFile;

  private final KeyStore keyStore;

  /**
   * Creates a new provider for a key store object.
   *
   * @param keyStore      The key store to search.
   * @param alias         The alias under which the private key and certificate
   *                      are stored.
   * @param entryPassword The password which protects the private key and
   *                      certificate.
   */
  public KeyStoreCertificateProvider(KeyStore keyStore, String alias, String entryPassword) {
    super();

    this.keyStore = keyStore;
    backingFile = null;
    keyStorePassword = null;
    this.alias = alias;
    this.entryPassword = entryPassword.toCharArray();
  }

  /**
   * Creates a new provider for a key store file.
   *
   * <p>
   * If the file doesn't exist, a new key store is automatically created and saved
   * to that file.
   *
   * <p>
   * This constructors defaults to the key store type "PKCS12". To specify another
   * type, please use
   * {@link #KeyStoreCertificateProvider(File, String, String, String, String)}.
   *
   * @param keyStoreFile     The file where the key store is stored.
   * @param keyStorePassword The password used for opening and saving the key
   *                         store to disk.
   * @param alias            The alias under which the private key and certificate
   *                         are stored.
   * @param entryPassword    The password which protects the private key and
   *                         certificate.
   *
   * @throws GeneralSecurityException will be either a
   *                                  {@link CertificateException} if the key
   *                                  store
   *                                  contains certificates which can't be loaded,
   *                                  or a
   *                                  {@link NoSuchAlgorithmException} if the
   *                                  integrity of the key
   *                                  store can't be checked.
   * @throws IOException              if there is an error accessing the file.
   */
  public KeyStoreCertificateProvider(File keyStoreFile, String keyStorePassword, String alias, String entryPassword)
      throws GeneralSecurityException, IOException {
    super();

    keyStore = KeyStoreFactory.openFromFileOrCreate(keyStoreFile, keyStorePassword);
    backingFile = keyStoreFile;
    this.keyStorePassword = keyStorePassword.toCharArray();
    this.alias = alias;
    this.entryPassword = entryPassword.toCharArray();
  }

  /**
   * Creates a new provider for a key store file.
   *
   * <p>
   * If the file doesn't exist, a new key store is automatically created and saved
   * to that file.
   *
   * @param keyStoreFile     The file where the key store resides on disk.
   * @param keyStoreType     The type of the key store. See {@link KeyStore} for
   *                         more information.
   * @param keyStorePassword The password used for opening and saving the key
   *                         store to disk.
   * @param alias            The alias under which the private key and certificate
   *                         are stored.
   * @param entryPassword    The password which protects the private key and
   *                         certificate.
   *
   * @throws GeneralSecurityException will be either a {@link KeyStoreException}
   *                                  if {@code type}
   *                                  isn't supported, or a
   *                                  {@link CertificateException} if the key
   *                                  store contains certificates which can't be
   *                                  loaded, or a
   *                                  {@link NoSuchAlgorithmException} if the
   *                                  integrity of the key
   *                                  store can't be checked.
   * @throws IOException              if there is an error accessing the file.
   */
  public KeyStoreCertificateProvider(File keyStoreFile, String keyStoreType, String keyStorePassword, String alias,
      String entryPassword) throws GeneralSecurityException, IOException {
    super();

    keyStore = KeyStoreFactory.openFromFileOrCreate(keyStoreFile, keyStorePassword, keyStoreType);
    backingFile = keyStoreFile;
    this.keyStorePassword = keyStorePassword.toCharArray();
    this.alias = alias;
    this.entryPassword = entryPassword.toCharArray();
  }

  @Override
  public void loadInternal() throws GeneralSecurityException {
    if (!keyStore.containsAlias(alias)) {
      throw new KeyStoreException("Alias '" + alias + "' not found in key store.");
    } else {
      Certificate cert = keyStore.getCertificate(alias);
      if (!(cert instanceof X509Certificate)) {
        throw new ClassCastException("Certificate under alias '" + alias + "' is not an X.509 certificate.");
      }

      Key key = keyStore.getKey(alias, entryPassword);
      if (!(key instanceof PrivateKey)) {
        throw new ClassCastException("Key under alias '" + alias + "' is not a private key.");
      }
      PrivateKey privateKey = (PrivateKey) key;
      PublicKey publicKey = cert.getPublicKey();

      keyPair = new KeyPair(publicKey, privateKey);
      certificate = (X509Certificate) cert;
    }
  }

  @Override
  protected void handleFallbackSuccessful() {
    // If the key pair and certificate have been provided by a fallback, store them
    // in the
    // key store for future use.
    if (backingFile != null) {
      try (OutputStream os = new FileOutputStream(backingFile)) {
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), entryPassword, new Certificate[] { certificate });
        keyStore.store(os, keyStorePassword);
      } catch (FileNotFoundException e) {
        logger.error("Key store file not found. Has it been deleted while the process was running?", e);
      } catch (IOException e) {
        logger.error("Failed to save updated key store to file.", e);
      } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
        logger.error("Failed to save key pair and certificate to key store", e);
      }
    }
  }
}
