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
package org.exoplatform.commons.search.service;

import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.State;
import org.exoplatform.portal.mop.description.DescriptionService;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 29, 2013  
 */
public class MockDescriptionService implements DescriptionService {

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#resolveDescription(java.lang.String, java.util.Locale)
   */
  @Override
  public State resolveDescription(String id, Locale locale) throws NullPointerException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#resolveDescription(java.lang.String, java.util.Locale, java.util.Locale)
   */
  @Override
  public State resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#getDescription(java.lang.String)
   */
  @Override
  public State getDescription(String id) throws NullPointerException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#setDescription(java.lang.String, org.exoplatform.portal.mop.Described.State)
   */
  @Override
  public void setDescription(String id, State description) throws NullPointerException {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#getDescription(java.lang.String, java.util.Locale)
   */
  @Override
  public State getDescription(String id, Locale locale) throws NullPointerException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#setDescription(java.lang.String, java.util.Locale, org.exoplatform.portal.mop.Described.State)
   */
  @Override
  public void setDescription(String id, Locale locale, State description) throws NullPointerException, IllegalArgumentException {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#getDescriptions(java.lang.String)
   */
  @Override
  public Map<Locale, State> getDescriptions(String id) throws NullPointerException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.description.DescriptionService#setDescriptions(java.lang.String, java.util.Map)
   */
  @Override
  public void setDescriptions(String id, Map<Locale, State> descriptions) throws NullPointerException, IllegalArgumentException {

  }

}

