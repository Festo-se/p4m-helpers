/*
 * Copyright (C) 2021 Festo Didactic SE
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package com.festo.aas.p4m.logging;

import org.apache.juli.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter to redirect logging calls to Juli to SLF4J.
 */
public class DelegateToSlf4jLogger implements Log {
  private final Logger logger;

  // constructor required by ServiceLoader
  public DelegateToSlf4jLogger() {
    logger = null;
  }

  public DelegateToSlf4jLogger(String name) {
    logger = LoggerFactory.getLogger(name);
  }

  @Override
  public boolean isDebugEnabled() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public boolean isFatalEnabled() {
    return logger.isErrorEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return logger.isInfoEnabled();
  }

  @Override
  public boolean isTraceEnabled() {
    return logger.isTraceEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return logger.isWarnEnabled();
  }

  @Override
  public void trace(Object message) {
    logger.debug(String.valueOf(message));
  }

  @Override
  public void trace(Object message, Throwable t) {
    logger.debug(String.valueOf(message), t);
  }

  @Override
  public void debug(Object message) {
    logger.debug(String.valueOf(message));
  }

  @Override
  public void debug(Object message, Throwable t) {
    logger.debug(String.valueOf(message), t);
  }

  @Override
  public void info(Object message) {
    logger.info(String.valueOf(message));
  }

  @Override
  public void info(Object message, Throwable t) {
    logger.info(String.valueOf(message), t);
  }

  @Override
  public void warn(Object message) {
    logger.warn(String.valueOf(message));
  }

  @Override
  public void warn(Object message, Throwable t) {
    logger.warn(String.valueOf(message), t);
  }

  @Override
  public void error(Object message) {
    logger.error(String.valueOf(message));
  }

  @Override
  public void error(Object message, Throwable t) {
    logger.error(String.valueOf(message), t);
  }

  @Override
  public void fatal(Object message) {
    logger.error(String.valueOf(message));
  }

  @Override
  public void fatal(Object message, Throwable t) {
    logger.error(String.valueOf(message), t);
  }
}
