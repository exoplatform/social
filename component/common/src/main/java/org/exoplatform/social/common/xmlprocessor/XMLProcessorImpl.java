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

import java.util.Iterator;
import java.util.LinkedList;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Implementation of {@link XMLProcessor}.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class XMLProcessorImpl implements XMLProcessor {
  
  /**
   * The linked list for all filters.
   */
  private LinkedList<Filter> filters;

  /**
   * Constructor
   */
  public XMLProcessorImpl() {
    filters = new LinkedList<Filter>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFilter(Filter filter) {
    filters.add(filter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeFilter(Filter addedFilter) {
    filters.remove(addedFilter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFilterPlugin(BaseXMLFilterPlugin filterComponentPlugin) {
    addFilter(filterComponentPlugin);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeFilterPlugin(BaseXMLFilterPlugin filterComponentPlugin) {
    removeFilter(filterComponentPlugin);
  }

  /**
   * {@inheritDoc}
   */
  public Object process(Object input) {
    if (input == null) return input;
    for (Iterator<Filter> filterIterator = filters.iterator();
         filterIterator.hasNext();
         ) {
      Filter filter = filterIterator.next();
      input = filter.doFilter(input);
    }
    return input;
  }
}
