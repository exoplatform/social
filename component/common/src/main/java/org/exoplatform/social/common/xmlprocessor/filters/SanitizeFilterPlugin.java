/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.common.xmlprocessor.filters;

import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;


/**
 * The filter escapes all the DOMTree to make sure it cleaned.
 * <b>Note:</b> this filter cannot detect that content escaped or not so make sure that you don't use it twice or
 * using it with escaped content.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class SanitizeFilterPlugin extends BaseXMLFilterPlugin {

  private static final Log LOG = ExoLogger.getLogger(SanitizeFilterPlugin.class);

  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if (input instanceof String) {
      try {
        return HTMLSanitizer.sanitize((String) input);
      } catch (Exception e) {
        LOG.error("Error while sanitizing input : " + e.getMessage(), e);
      }
    }
    return input;
  }
}
