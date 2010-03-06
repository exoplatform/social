package org.exoplatform.social.space.lifecycle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A generic Lifecycle construct to dispatch events to listeners.
 * May represent an arbitrary lifecycle. lifecycle methods are typically added by subclasses in this form : 
 * <pre>
 * 
 * </pre>
 * @param T type of listener for this LifeCycle
 * @param E type of event to be broadcasted to the listeners
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractLifeCycle<T,E> {

  private Set<T> listeners = new HashSet<T>();
  protected ExecutorService executor = Executors.newSingleThreadExecutor();
  protected ExecutorCompletionService<E> ecs = new ExecutorCompletionService<E>(executor);
  
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
   * Broadcasts an event to the registered listeners.
   * The event is broadcasted asynchronously but sequentially.
   * @see #dispatchEvent(Object, Object)
   * @param event
   */
  protected void broadcast(final E event) {
    for (final T listener : listeners) {
      ecs.submit(new Callable<E>() {        
        public E call() throws Exception {
          dispatchEvent(listener,event);
          return event;
        }
      }); 
    }
  }

  /**
   * Called by {@link #broadcast(Object)} to delegate logic for calling the appropriate listener method based on the event.
   * Typically implemented like this : 
   * <pre>
   *  switch(event.getType()) { // or any other condition on event
   *   case XXX : 
   *     listener.callXXX(); 
   *     break;
   *   case YYY :  
   *     listener.callYYY();
   *   ...
   *  }
   * </pre>
   * @param listener listener
   * @param event
   * @return
   */
  protected abstract void dispatchEvent(final T listener, final E event);
  
}
