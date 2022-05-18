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

package com.festo.aas.p4m.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.InvalidPropertiesFormatException;
import java.util.NoSuchElementException;
import java.util.Properties;

import com.festo.aas.p4m.validators.IdShortValidator;
import com.festo.aas.p4m.validators.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives access to application settings in a Java properties file.
 *
 * <p>
 * This class loads and validates configuration settings from a properties file.
 * This file must be located in the working directory. When executing an
 * application from a JAR file that will usually be the directory where the JAR
 * file is located. When running the application straight from an IDE, it is
 * typically the project's root directory.
 *
 * <p>
 * Only pre-defined settings are supported. They are exposed as individual
 * public fields. Any other arbitrarily named properties in that file cannot be
 * accessed through this class.
 *
 * <p>
 * Defaults for all settings can be provided in the
 * {@link #DEFAULT_PROPERTIES_RESOURCE} file which must be located somewhere in
 * the class path. When using this class in the context of a maven project, the
 * correct path by default is {@code src/main/resources}.
 */
public abstract class AasProperties {
  private static final String DEFAULT_PROPERTIES_RESOURCE = "default.properties";
  static final String DEFAULT_USER_FILE_PATH = "application.properties";
  private static final Logger logger = LoggerFactory.getLogger(AasProperties.class);

  private final Properties properties;
  private final String userFilePath;

  /**
   * Creates a new instance which loads properties from
   * {@value #DEFAULT_USER_FILE_PATH}.
   *
   * <p>
   * Default values are automatically loaded from the resource file
   * {@link #DEFAULT_PROPERTIES_RESOURCE} within this constructor. User-defined
   * values are loaded in
   * {@link #load()} instead, which must be called, before any property values can
   * be accessed.
   *
   * @throws IOException if the resource file with the default values can't be
   *                     loaded.
   */
  protected AasProperties() throws IOException {
    this(DEFAULT_USER_FILE_PATH);
  }

  /**
   * Creates a new instance with loads properties from the specified file path.
   *
   * <p>
   * Default values are automatically loaded from the resource file
   * {@link #DEFAULT_PROPERTIES_RESOURCE} within this constructor. User-defined
   * values are loaded in
   * {@link #load()} instead, which must be called, before any property values can
   * be accessed.
   *
   * @param userFilePath The path to the properties file to load.
   *
   * @throws IOException if the resource file with the default values can't be
   *                     loaded.
   */
  protected AasProperties(String userFilePath) throws IOException {
    this.userFilePath = userFilePath;
    Properties defaultProps = loadDefaults();
    properties = new Properties(defaultProps);
  }

  /**
   * The AAS' name. In AAS terms, this is its idShort.
   *
   * <p>
   * Because this serves as the AAS' idShort, it must adhere to these formatting
   * rules:
   * <ul>
   * <li>Can only contain letters from the English alphabet, digits and
   * underscores.
   * <li>Must start with a letter.
   * </ul>
   */
  @LoadableProperty
  public final StringProperty aasName = new StringProperty("aas.name", new IdShortValidator());

  /**
   * The URI identifying this AAS. This serves as the AAS' globally unique id.
   *
   * <p>
   * This can be a URL (e.g. {@code http://www.example.com/myAAS1234}), a URN
   * (e.g.
   * {@code urn:uuid:6e8bc430-9c3a-11d9-9669-0800200c9a66}) or some other
   * form of URI.
   *
   * <p>
   * This is merely an identifier. Even if an URL is specified, this URL isn't
   * required to be
   * accessible on any network.
   */
  @LoadableProperty
  public final UriProperty aasUri = new UriProperty("aas.uri");

  /**
   * The asset's name. In AAS terms, this is its idShort.
   *
   * <p>
   * Because this serves as the asset's idShort, it must adhere to the formatting
   * rules listed
   * {@link #aasName here}.
   */
  @LoadableProperty
  public final StringProperty assetName = new StringProperty("asset.name", new IdShortValidator());

  /**
   * The URI identifying the asset. This serves as the asset's globally unique id.
   *
   * <p>
   * This can be a URL (e.g. {@code http://www.example.com/myAAS1234}), a URN
   * (e.g.
   * {@code urn:uuid:6e8bc430-9c3a-11d9-9669-0800200c9a66}) or some other
   * form of URI.
   *
   * <p>
   * This is merely an identifier. Even if an URL is specified, this URL isn't
   * required to be
   * accessible on any network.
   */
  @LoadableProperty
  public final UriProperty assetUri = new UriProperty("asset.uri");

  /**
   * The TCP port at which this AAS listens for network requests.
   */
  @LoadableProperty
  public final IntegerProperty applicationPort = new IntegerProperty("aas.listening_port",
      val -> val > 0);

  /**
   * The hostname (or IP) under which the AAS listens for network requests.
   */
  @LoadableProperty
  public final StringProperty aplicationHostname = new StringProperty("aas.hostname");

  /**
   * Loads the customized properties from the configured file and parses all
   * values. Must be called
   * before accessing properties.
   *
   * @throws InvalidPropertiesFormatException if any property in either the
   *                                          defaults or the user
   *                                          properties file has an invalid
   *                                          value.
   */
  public void load() throws InvalidPropertiesFormatException {
    loadUserProperties();

    // Iterates over all fields of this class with the LoadableProperty annotation.
    // Calls the load() method on each such property.
    Field[] fields = getClass().getFields();

    for (Field field : fields) {
      if (field.isAnnotationPresent(LoadableProperty.class)) {
        try {
          Property<?> prop = (Property<?>) field.get(this);
          prop.load();
        } catch (IllegalArgumentException e) {
          throw new AssertionError("impossible");
        } catch (IllegalAccessException e) {
          logger.warn("Failed to load property '{}'", field.getName(), e);
        } catch (ClassCastException e) {
          logger.warn("LoadableProperty shouldn't be applied to field '{}' because it is not a Property<T>.",
              field.getName());
        }
      }
    }
  }

  /**
   * Gets a string-valued property.
   *
   * @param key The key of the property to get.
   *
   * @return The value of the property.
   *
   * @throws NoSuchElementException if the given key isn't found.
   */
  private String getStringProperty(String key) {
    String val = properties.getProperty(key);
    if (val == null) {
      throw new NoSuchElementException("Property '" + key + "' is required but is not set.");
    }
    return val;
  }

  /**
   * Gets an int-valued property.
   *
   * <p>
   * The property's value must follow the formatting rules of
   * {@link Integer#parseInt(String)}
   *
   * @param key The key of the property to get.
   *
   * @return The value of the property.
   *
   * @throws NumberFormatException if the value can't be converted to an integer.
   */
  private int getIntProperty(String key) {
    return Integer.parseInt(getStringProperty(key));
  }

  /**
   * Gets an boolean-valued property.
   *
   * <p>
   * The property's value must follow the formatting rules of
   * {@link Boolean#parseBoolean(String)}
   *
   * @param key The key of the property to get.
   *
   * @return The value of the property.
   */
  private boolean getBooleanProperty(String key) {
    return Boolean.parseBoolean(getStringProperty(key));
  }

  private void loadUserProperties() {
    try (FileInputStream inputStream = new FileInputStream(userFilePath)) {
      properties.load(inputStream);
      logger.info("Settings loaded from '{}'.", userFilePath);
    } catch (FileNotFoundException e) {
      logger.info("No settings file found at '{}'. Continuing with default config.", userFilePath);
    } catch (IOException e) {
      logger.warn("Failed to load settings file at '{}'. Continuing with default settings.",
          userFilePath, e);
    }
  }

  /**
   * Loads the default property values from the resource file.
   *
   * @return A new property object containing the default values.
   *
   * @throws IOException if the resource file can't be read.
   */
  private Properties loadDefaults() throws IOException {
    Properties defaults = new Properties();

    InputStream inputStream = AasProperties.class.getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_RESOURCE);
    if (inputStream != null) {
      defaults.load(inputStream);
      logger.debug("Default settings loaded from '{}'.", DEFAULT_PROPERTIES_RESOURCE);
    }

    return defaults;
  }

  /**
   * Represents a single validatable, generic property in the properties file.
   *
   * @param <T> The data type of the property.
   */
  private abstract class Property<T> {
    private final String key;
    private final Validator<T> validator;

    private T value;
    private boolean loaded;

    Property(String key) {
      this(key, null);
    }

    Property(String key, Validator<T> validator) {
      this.key = key;
      this.validator = validator;
    }

    void load() throws InvalidPropertiesFormatException {
      value = loadFromProperties(key);

      if (validator != null && !validator.isValid(value)) {
        String msg = "Setting '" + key + "' has an invalid value: " + value;
        logger.warn(msg);
        throw new InvalidPropertiesFormatException(msg);
      }

      loaded = true;
    }

    protected abstract T loadFromProperties(String key) throws InvalidPropertiesFormatException;

    /**
     * Gets the value of this property.
     *
     * @return The value.
     *
     * @throws IllegalStateException if accessed before {@link AasProperties#load()}
     *                               is called.
     */
    public T get() {
      if (!loaded) {
        throw new IllegalStateException("Property hasn't been loaded.");
      }

      return value;
    }
  }

  /**
   * Represents a single validatable, integer-valued property in the properties
   * file.
   */
  public final class IntegerProperty extends Property<Integer> {

    public IntegerProperty(String key) {
      super(key);
    }

    public IntegerProperty(String key, Validator<Integer> validator) {
      super(key, validator);
    }

    @Override
    protected Integer loadFromProperties(String key) {
      return getIntProperty(key);
    }
  }

  /**
   * Represents a single validatable, string-valued property in the properties
   * file.
   */
  public final class StringProperty extends Property<String> {

    public StringProperty(String key) {
      super(key);
    }

    public StringProperty(String key, Validator<String> validator) {
      super(key, validator);
    }

    @Override
    protected String loadFromProperties(String key) {
      return getStringProperty(key);
    }
  }

  /**
   * Represents a single validatable, enum-valued property in the properties file.
   *
   * @param <E> The enum type of this property.
   */
  public final class EnumProperty<E extends Enum<E>> extends Property<E> {
    private Class<E> enumType;

    public EnumProperty(Class<E> enumType, String key) {
      super(key);
      this.enumType = enumType;
    }

    public EnumProperty(Class<E> enumType, String key, Validator<E> validator) {
      super(key, validator);
      this.enumType = enumType;
    }

    @Override
    protected E loadFromProperties(String key) throws InvalidPropertiesFormatException {
      String stringVal = getStringProperty(key);

      try {
        return Enum.valueOf(enumType, stringVal);
      } catch (IllegalArgumentException e) {
        String msg = "Setting '" + key + "' must be one of " + enumType.getEnumConstants();
        logger.warn(msg);
        throw new InvalidPropertiesFormatException(msg);
      }
    }
  }

  /**
   * Represents a single validatable, URI-valued property in the properties file.
   */
  public final class UriProperty extends Property<URI> {

    public UriProperty(String key) {
      super(key);
    }

    public UriProperty(String key, Validator<URI> validator) {
      super(key, validator);
    }

    @Override
    protected URI loadFromProperties(String key) throws InvalidPropertiesFormatException {
      String prop = getStringProperty(key);
      try {
        return new URI(prop);
      } catch (URISyntaxException e) {
        throw new InvalidPropertiesFormatException(e);
      }
    }
  }

  /**
   * Represents a single validatable, boolean-valued property in the properties
   * file.
   */
  public final class BooleanProperty extends Property<Boolean> {

    public BooleanProperty(String key) {
      super(key);
    }

    @Override
    protected Boolean loadFromProperties(String key) throws InvalidPropertiesFormatException {
      return getBooleanProperty(key);
    }
  }
}
