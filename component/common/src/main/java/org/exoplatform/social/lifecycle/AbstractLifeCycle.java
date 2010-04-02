package org.exoplatform.social.lifecycle;

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
