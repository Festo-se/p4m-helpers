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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Provides a set of standard command line options.
 */
public final class CommandLineOptions {

  private CommandLineOptions() {
    throw new AssertionError("Can't create instances.");
  }

  /**
   * Gets the default command line options to be used with an Apache Commons CLI
   * parser.
   *
   * @return The default command line options.
   */
  public static Options getDefaultOptions() {
    Options options = new Options();
    options.addOption(Option.builder("p")
        .hasArg()
        .argName("file")
        .longOpt("propertyFile")
        .type(String.class)
        .desc("Path to user properties file. Default: "
            + AasProperties.DEFAULT_USER_FILE_PATH)
        .build());

    options.addOption(Option.builder("l")
        .hasArg()
        .argName("file")
        .longOpt("logConfig")
        .type(String.class)
        .desc(
            "Path to logback configuration file. Do not name it 'logback.xml'! "
            + "If not provided, a default configuration is used.")
        .build());

    return options;
  }
}
