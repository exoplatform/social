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
 * The Processor is responsible for processing the input by pushing the input through filter chains and returns
 * result. This processor also allows configuring external filter component plugin.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since  1.2.1
 */
public interface XMLProcessor {

  /**
   * Adds a defined filter to this processor.
   *
   * @param filter a defined filter
   */
  void addFilter(Filter filter);

  /**
   * Removes a defined filter which is added.
   *
   * @param addedFilter the added filter
   */
  void removeFilter(Filter addedFilter);

  /**
   * Adds a defined filter component plugin to this processor.
   *
   * @param filterComponentPlugin the filter component plugin
   */
  void addFilterPlugin(BaseXMLFilterPlugin filterComponentPlugin);

  /**
   * Removes the existing filter component plugin from this processor.
   *
   * @param filterComponentPlugin the existing filter component plugin.
   */
  void removeFilterPlugin(BaseXMLFilterPlugin filterComponentPlugin);

  /**
   * Processes an input object though filter chains.
   *
   * @param input the input object
   * @return new processed object by added filters.
   */
  Object process(Object input);

}
