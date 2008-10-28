/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.space;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public interface SpaceService {
  
  /**
   * Gets all space
   * @return get All space of Portal
   * @throws Exception
   */
  public List<Space> getAllSpaces() throws Exception;
  
  /**
   * Gets a space by its id
   * @param id Id of space
   * @return space with id specified
   * @throws Exception
   */
  public Space getSpace(String id) throws Exception;
 
  /**
   * Creates new space or saves when edit it
   * @param space space is saved
   * @param isNew is true if create new space, false if edit existed space
   * @throws Exception
   */
  public void saveSpace(Space space, boolean isNew) throws Exception;
}