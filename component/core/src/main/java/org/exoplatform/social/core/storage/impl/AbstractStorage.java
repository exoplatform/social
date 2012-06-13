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

package org.exoplatform.social.core.storage.impl;

import java.util.Iterator;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceRootEntity;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractStorage {

  //
  protected final SocialChromatticLifeCycle lifeCycle;

  //
  protected static final String NS_JCR = "jcr:";

  //
  protected static final String NODETYPE_PROVIDERS = "soc:providers";
  protected static final String NODETYPE_SPACES = "soc:spaces";

  //
  protected static final String SENDER = "sender";
  protected static final String RECEIVER = "receiver";

  private CachedActivityStorage cachedActivityStorage;

  protected AbstractStorage() {

    this.lifeCycle = lifecycleLookup();

  }

  protected ChromatticSession getSession() {
    return lifeCycle.getSession();
  }

  private <T> T getRoot(String nodetypeName, Class<T> t) {
    T got = getSession().findByPath(t, nodetypeName);
    if (got == null) {
      got = getSession().insert(t, nodetypeName);
    }
    return got;
  }

  protected ProviderRootEntity getProviderRoot() {
    if (lifeCycle.getProviderRoot().get() == null) {
      lifeCycle.getProviderRoot().set(getRoot(NODETYPE_PROVIDERS, ProviderRootEntity.class));
    }
    return (ProviderRootEntity) lifeCycle.getProviderRoot().get();
  }

  protected SpaceRootEntity getSpaceRoot() {
    if (lifeCycle.getSpaceRoot().get() == null) {
      lifeCycle.getSpaceRoot().set(getRoot(NODETYPE_SPACES, SpaceRootEntity.class));
    }
    return (SpaceRootEntity) lifeCycle.getSpaceRoot().get();
  }

  protected <T> T _findById(final Class<T> clazz, final String nodeId) throws NodeNotFoundException {

    if (nodeId == null) {
      throw new NodeNotFoundException("null id cannot be found");
    }

    //
    T got = getSession().findById(clazz, nodeId);

    //
    if (got == null) {
      throw new NodeNotFoundException(nodeId + " doesn't exists");
    }

    return got;
  }

  protected <T> T _findByPath(final Class<T> clazz, final String nodePath) throws NodeNotFoundException {
    if (nodePath == null) {
      throw new NodeNotFoundException("null nodePath cannot be found");
    }

    //
    T got = getSession().findByPath(clazz, nodePath, true);

    //
    if (got == null) {
      throw new NodeNotFoundException(nodePath + " doesn't exists");
    }

    return got;
  }

  protected void _removeById(final Class<?> clazz, final String nodeId) {
    getSession().remove(getSession().findById(clazz, nodeId));
  }

  protected boolean isJcrProperty(String name) {
    return !name.startsWith(NS_JCR);
  }

  protected void _skip(Iterator<?> it, long offset) {

    // TODO : use JCR skip

    while (it.hasNext()) {
      if (offset == 0) {
        return;
      }
      else {
        it.next();
        --offset;
      }
    }
  }

  public static boolean startSynchronization() {

    SocialChromatticLifeCycle lc = lifecycleLookup();

    if (lc.getManager().getSynchronization() == null) {
      lc.getManager().beginRequest();
      return true;
    }
    return false;
  }

  public static void stopSynchronization(boolean requestClose) {

    SocialChromatticLifeCycle lc = lifecycleLookup();
    if (requestClose) {
      lc.getManager().endRequest(true);
    }
  }

  private static SocialChromatticLifeCycle lifecycleLookup() {

    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    return (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

  }

}
