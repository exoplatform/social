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

import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;

/**
 * This Filter repace native line break character in String input and replace it with HTML /<br/> tag.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class LineBreakerFilterPlugin extends BaseXMLFilterPlugin {

  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if (input instanceof String) {
      String inputString = (String) input;
      input = inputString.replaceAll("\\r?\\n", "<br />");
    }
    return input;
  }
}
