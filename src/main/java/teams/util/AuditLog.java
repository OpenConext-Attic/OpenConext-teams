/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teams.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for audit logging
 */
public class AuditLog {

  private static final Logger LOG = LoggerFactory.getLogger("teams.audit");

  /**
   * Same interface as SLF4J's logging methods
   * @param msg the message
   * @param args arguments
   */
  public static void log(String msg, Object... args) {
    LOG.info(msg, args);
  }

  /**
   * Same interface as SLF4J's logging methods
   * @param msg the message
   * @param arg0 single argument
   */
  public static void log(String msg, Object arg0) {
    LOG.info(msg, arg0);
  }

  /**
   * Same interface as SLF4J's logging methods
   * @param msg the message
   * @param arg0 first argument
   * @param arg1 second argument
   */
  public static void log(String msg, Object arg0, Object arg1) {
    LOG.info(msg, arg0, arg1);
  }
}
