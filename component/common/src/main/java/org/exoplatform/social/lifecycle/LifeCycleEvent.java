package org.exoplatform.social.lifecycle;


/**
 * An event fired at different stages of the lifecycle
 * 
 * @see {@link LifeCycleListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice  Lamarque</a>
 * @version $Revision$
 */
public class LifeCycleEvent<S,P> {

  /**
   * space where the event occurs
   */
  protected P  payload;

  /**
   * source of the event. 
   */
  protected S source;

  public LifeCycleEvent(S source, P payload) {
    this.payload = payload;
    this.source = source;
  }


  public P getPayload() {
    return payload;
  }

  public S getSource() {
    return source;
  }

 

}
