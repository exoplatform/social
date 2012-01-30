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

package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ScopeCacheKey implements CacheKey {

  private final String scope;

  public ScopeCacheKey() {
    scope = getCurrentRepositoryName();
  }

  public String getScope() {
    return scope;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ScopeCacheKey)) {
      return false;
    }

    ScopeCacheKey that = (ScopeCacheKey) o;

    if (scope != null ? !scope.equals(that.scope) : that.scope != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return scope != null ? scope.hashCode() : 0;
  }

  public static String getCurrentRepositoryName() {
    RepositoryService repositoryService = (RepositoryService)
                                          PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
    try {
      return repositoryService.getCurrentRepository().getConfiguration().getName();
    }
    catch (RepositoryException e) {
      throw new RuntimeException(e);
    }
  }

}
