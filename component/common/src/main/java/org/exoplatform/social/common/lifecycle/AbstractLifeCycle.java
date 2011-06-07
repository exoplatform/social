/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.common.lifecycle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.chromattic.SynchronizationListener;
import org.exoplatform.commons.chromattic.SynchronizationStatus;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Generic implementation a Lifecycle<br/>
 * Events are dispatched asynchronously but sequentially to their listeners
 * according to their type. <br/>
 * Listeners may fail, this is safe for the lifecycle, subsequent listeners will
 * still be called.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractLifeCycle<T extends LifeCycleListener<E>, E extends LifeCycleEvent<?,?>> {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(AbstractLifeCycle.class);

  protected Set<T> listeners = new HashSet<T>();

  protected final PortalContainer container;

  protected LifeCycleCompletionService completionService;

  protected ChromatticManager manager;

  protected ChromatticLifeCycle lifeCycle;

  protected AbstractLifeCycle() {

    this.container = PortalContainer.getInstance();
    this.completionService = (LifeCycleCompletionService) container.getComponentInstanceOfType(LifeCycleCompletionService.class);
    this.manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);

    if (manager != null) {
      this.lifeCycle = manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addListener(T listener) {
    listeners.add(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void removeListener(T listener) {
    listeners.remove(listener);
  }

  /**
   * Broadcasts an event to the registered listeners. The event is broadcasted
   * asynchronously but sequentially.
   *
   * @see #dispatchEvent(LifeCycleListener, LifeCycleEvent)
   * @param event
   */
  protected void broadcast(final E event) {

    //
    SessionContext ctx = lifeCycle.getContext();
    ctx.addSynchronizationListener(new SynchronizationListener() {

      public void beforeSynchronization() {}

      public void afterSynchronization(SynchronizationStatus status) {
        if (status == SynchronizationStatus.SAVED) {

          addTasks(event);

        }
      }

    });
    
  }

  protected void addTasks(final E event) {
    for (final T listener : listeners) {
      completionService.addTask(new Callable<E>() {
        public E call() throws Exception {
          try {
            begin();
            dispatchEvent(listener, event);
          }
          catch(Exception e) {
            LOG.debug(e.getMessage(), e);
          }
          finally {
            end();
          }

          return event;
        }
      });
    }
  }

  protected void begin() {
    manager.beginRequest();
    lifeCycle.getChromattic().openSession();
  }

  protected void end() {
    manager.endRequest(true);
  }

  protected abstract void dispatchEvent(final T listener, final E event);

}
