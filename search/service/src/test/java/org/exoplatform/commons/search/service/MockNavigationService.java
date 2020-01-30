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

import java.util.List;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.navigation.NodeChangeListener;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 29, 2013  
 */
public class MockNavigationService implements NavigationService {

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#loadNavigation(org.exoplatform.portal.mop.SiteKey)
   */
  @Override
  public NavigationContext loadNavigation(SiteKey key) throws NullPointerException, NavigationServiceException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#saveNavigation(org.exoplatform.portal.mop.navigation.NavigationContext)
   */
  @Override
  public void saveNavigation(NavigationContext navigation) throws NullPointerException,
                                                          IllegalArgumentException,
                                                          NavigationServiceException {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#destroyNavigation(org.exoplatform.portal.mop.navigation.NavigationContext)
   */
  @Override
  public boolean destroyNavigation(NavigationContext navigation) throws NullPointerException,
                                                                IllegalArgumentException,
                                                                NavigationServiceException {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#loadNode(org.exoplatform.portal.mop.navigation.NodeModel, org.exoplatform.portal.mop.navigation.NavigationContext, org.exoplatform.portal.mop.navigation.Scope, org.exoplatform.portal.mop.navigation.NodeChangeListener)
   */
  @Override
  public <N> NodeContext<N> loadNode(NodeModel<N> model,
                                     NavigationContext navigation,
                                     Scope scope,
                                     NodeChangeListener<NodeContext<N>> listener) throws NullPointerException,
                                                                                 NavigationServiceException {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#saveNode(org.exoplatform.portal.mop.navigation.NodeContext, org.exoplatform.portal.mop.navigation.NodeChangeListener)
   */
  @Override
  public <N> void saveNode(NodeContext<N> context, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException,
                                                                                               NavigationServiceException {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#updateNode(org.exoplatform.portal.mop.navigation.NodeContext, org.exoplatform.portal.mop.navigation.Scope, org.exoplatform.portal.mop.navigation.NodeChangeListener)
   */
  @Override
  public <N> void updateNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException,
                                                                                                              IllegalArgumentException,
                                                                                                              NavigationServiceException {

  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.mop.navigation.NavigationService#rebaseNode(org.exoplatform.portal.mop.navigation.NodeContext, org.exoplatform.portal.mop.navigation.Scope, org.exoplatform.portal.mop.navigation.NodeChangeListener)
   */
  @Override
  public <N> void rebaseNode(NodeContext<N> context, Scope scope, NodeChangeListener<NodeContext<N>> listener) throws NullPointerException,
                                                                                                              NavigationServiceException {

  }

  @Override
  public List<NavigationContext> loadNavigations(SiteType type)
      throws NullPointerException, NavigationServiceException {
    // TODO Auto-generated method stub
    return null;
  }

}
