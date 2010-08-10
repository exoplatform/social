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
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  private Set<T>      listeners = new HashSet<T>();

  protected ExecutorService                        executor  = Executors.newSingleThreadExecutor();

  protected ExecutorCompletionService<E> ecs;

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
    if (ecs == null) {
      ecs = new ExecutorCompletionService<E>(executor);
    }

    for (final T listener : listeners) {
      ecs.submit(new Callable<E>() {
        public E call() throws Exception {
          dispatchEvent(listener, event);
          return event;
        }
      });
    }
  }

  protected abstract void dispatchEvent(final T listener, final E event);

}
