/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service.utils;

import org.exoplatform.social.common.service.AsyncCallback;
import org.exoplatform.social.common.service.AsyncProcessor;
import org.exoplatform.social.common.service.ProcessContext;

public class AsyncProcessorTool {
  /**
   * Calls the async of the processor's process method and waits
   * for it to complete before returning. This can be used by {@link AsyncProcessor}
   * objects to implement their sync version of the process method.
   *
   * @param processor the processor
   * @param processContext  the context
   * @throws Exception can be thrown if waiting is interrupted
   */
  public static void process(final AsyncProcessor processor, final ProcessContext processContext) throws Exception {
      processor.start(processContext);
      processor.process(processContext, new AsyncCallback() {
          public void done(ProcessContext processContext) {}

          @Override
          public String toString() {
              return "Done " + processor;
          }
      });
      
      processor.end(processContext);
  }
}
