/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;

/**
 * The Realtime list access interface to provide more facility to list access for easier real-time access.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  1.2.0-GA
 * @since  Apr 4, 2011
 */
public interface RealtimeListAccess<E> extends ListAccess<E> {

  /**
   * Loads items as list instead of array as from {@link #load(int, int)}.
   *
   * @param index the index
   * @param limit the number to load
   * @return a list
   */
  List<E> loadAsList(int index, int limit);

  /**
   * Overrides its parent interface to avoid checked-exception.
   * @param index the index
   * @param limit the maximum of elements to return
   * @return array of elements
   */
  E[] load(int index, int limit);

  /**
   * Overrides its parent interface to avoid checked-exception.
   *
   * @return the number of elements.
   */
  int getSize();

  /**
   * Loads newer elements based on the provided element.
   *
   * @param e the based element
   * @param length number of newer elements to load
   * @return an array of newer elements
   */
  List<E> loadNewer(E e, int length);

  /**
   * Gets the number of newer elements based on the provided element.
   *
   * @param e the provided element
   * @return number of newer elements if any
   */
  int getNumberOfNewer(E e);

  /**
   * Loads older elements based on the provided element.
   *
   * @param e the based element
   * @param length number of older elements to load
   * @return an array of older elements
   */
  List<E> loadOlder(E e, int length);

  /**
   * Gets the number of older elements based on the provided element.
   *
   * @param e the provided element
   * @return number of older elements if any
   */
  int getNumberOfOlder(E e);

  /**
   * Gets the number of newer elements based on the postedTime.
   * 
   * @param sinceTime the postedTime
   * @return number of newer elements if any
   */
  int getNumberOfNewer(Long sinceTime);
  
}
