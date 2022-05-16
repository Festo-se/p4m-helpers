/*
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.festo.aas.p4m.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link KeyStore} instances from a file.
 */
public final class KeyStoreFactory {
  private static final Logger logger = LoggerFactory.getLogger(KeyStoreFactory.class);

  // Empty private constructor to prevent other code from instantiating this
  // class.
  private KeyStoreFactory() {
    throw new AssertionError("Cannot create instances.");
  }

  /**
   * Loads a key store from the given file or creates a new one if the files
   * doesn't exist.
   *
   * <p>
   * The default key store type of <i>PKCS12</i> is assumed. Another type can be
   * selected with the
   * {@link #openFromFileOrCreate(File, String, String)} overload.
   *
   * @param file     The key store file object.
   * @param password The password to access the key store.
   *
   * @return The key store loaded from the file or the newly created one.
   *
   * @throws GeneralSecurityException will be either a
   *                                  {@link CertificateException} if the key
   *                                  store
   *                                  contains certificates which can't be loaded,
   *                                  or a
   *                                  {@link NoSuchAlgorithmException} if the
   *                                  integrity of the key
   *                                  store can't be checked.
   * @throws IOException              if an I/O error occurs while accessing the
   *                                  key store file.
   * @throws NullPointerException     if {@code file} is {@code null}.
   * @throws IllegalArgumentException if {@code file} is a directory.
   */
  public static KeyStore openFromFileOrCreate(File file, String password)
      throws GeneralSecurityException, IOException {
    return openFromFileOrCreate(file, password, "PKCS12");
  }

  /**
   * Loads a key store from the given file or creates a new one if the files
   * doesn't exist.
   *
   * @param file     The key store file object.
   * @param type     The type of key store to expect in that file. See
   *                 {@link java.security.KeyStore}.
   * @param password The password to access the key store.
   *
   * @return The key store loaded from the file or the newly created one.
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
   * @throws IOException              if an I/O error occurs while accessing the
   *                                  key store file.
   * @throws NullPointerException     if {@code file} is {@code null}.
   * @throws IllegalArgumentException if {@code file} is a directory.
   */
  public static KeyStore openFromFileOrCreate(File file, String password, String type)
      throws IOException, GeneralSecurityException {
    Objects.requireNonNull(file, "File must not be null");
    if (file.isDirectory()) {
      throw new IllegalArgumentException("file must not be a directory.");
    }

    KeyStore store = KeyStore.getInstance(type);

    if (file.exists()) {
      // Load the existing key store.
      try (FileInputStream inputStream = new FileInputStream(file)) {
        store.load(inputStream, password.toCharArray());
      } catch (Exception e) {
        logger.error("Failed to load key store at '{}'", file, e);
        throw e;
      }
    } else {
      // Create the file's parent directories, if necessary.
      File parentDir = file.getParentFile();
      if (parentDir != null) {
        Files.createDirectories(parentDir.toPath(), (FileAttribute[]) null);
      }

      // Create a blank key store and save it to the file.
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        store.load(null, password.toCharArray());
        store.store(outputStream, password.toCharArray());
      } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
        logger.error("Failed to save key store at '{}'.", file, e);
        throw e;
      }
    }

    return store;
  }
}
