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
package org.exoplatform.social.common.xmlprocessor;

/**
 * The filter actually does the work of filtering the input and returns the result. Many filters are added into
 * filter chains and are used by {@link XMLProcessor} to process input.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since  1.2.1
 */
public interface Filter {
  /**
   * Filters the input data.
   *
   * @param input the input data
   * @return an Object with the result after filtered
   */
  public Object doFilter(Object input);
}