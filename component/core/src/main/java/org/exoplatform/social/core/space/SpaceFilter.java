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
package org.exoplatform.social.core.space;

import org.exoplatform.social.core.search.Sorting;

/**
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.0-GA
 */
public class SpaceFilter {
  /** The first character of the space name. */
  private char firstCharacterOfSpaceName;
  
  /** The space name search condition. */
  private String spaceNameSearchCondition;
  
  /** The default value for char type. */
  private static char CHAR_DEFAULT_VALUE = '\u0000';

  private Sorting sorting;
  
  /**
   * The constructor.
   */
  public SpaceFilter() {
    this.firstCharacterOfSpaceName = CHAR_DEFAULT_VALUE;
    this.spaceNameSearchCondition = null;
  }
  
  /**
   * The constructor.
   * 
   * @param firstCharacterOfSpaceName
   */
  public SpaceFilter(char firstCharacterOfSpaceName) {
    this.firstCharacterOfSpaceName = firstCharacterOfSpaceName;
    this.spaceNameSearchCondition = null;
  }
  
  /**
   * The constructor.
   * 
   * @param spaceNameSearchCondition
   */
  public SpaceFilter(String spaceNameSearchCondition) {
    this.firstCharacterOfSpaceName = CHAR_DEFAULT_VALUE;
    this.spaceNameSearchCondition = SpaceUtils.removeSpecialCharacterInSpaceFilter(spaceNameSearchCondition);
  }
  
  /**
   * The constructor.
   * 
   * @param firstCharacterOfSpaceName
   * @param spaceNameSearchCondition
   */
  public SpaceFilter(char firstCharacterOfSpaceName, String spaceNameSearchCondition) {
    this.firstCharacterOfSpaceName = firstCharacterOfSpaceName;
    this.spaceNameSearchCondition = SpaceUtils.removeSpecialCharacterInSpaceFilter(spaceNameSearchCondition);
  }
  
  /**
   * Gets the first character of space name.
   * 
   * @return the first character of space name
   */
  public char getFirstCharacterOfSpaceName() {
    return firstCharacterOfSpaceName;
  }

  /**
   * Sets the first character of space name.
   * 
   * @param firstCharacterOfSpaceName
   */
  public void setFirstCharacterOfSpaceName(char firstCharacterOfSpaceName) {
    this.firstCharacterOfSpaceName = firstCharacterOfSpaceName;
  }

  /**
   * Gets the space name search condition.
   * 
   * @return the space name search condition
   */
  public String getSpaceNameSearchCondition() {
    return spaceNameSearchCondition;
  }

  /**
   * Sets the space name search condition.
   * 
   * @param spaceNameSearchCondition
   */
  public void setSpaceNameSearchCondition(String spaceNameSearchCondition) {
    this.spaceNameSearchCondition = SpaceUtils.removeSpecialCharacterInSpaceFilter(spaceNameSearchCondition);
  }

  public Sorting getSorting() {
     if (sorting == null) {
       return sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
     }
     return sorting;
   }

   public void setSorting(Sorting sorting) {
     this.sorting = sorting;
   }

}
