package org.exoplatform.social.core.identity.lifecycle;


import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent;
import org.exoplatform.social.core.identity.spi.ProfileListener;
import org.exoplatform.social.lifecycle.AbstractListenerPlugin;

/**
 * Convenience class to write and wire {@link ProfileListener} plugin. <br/>
 * This base class is a valid {@link ComponentPlugin} and implements {@link ProfileListener}.
 * @see IdentityManager#registerProfileListener(ProfileListener)
 */
public abstract class ProfileListenerPlugin extends AbstractListenerPlugin implements ProfileListener {

  /**
   * {@inheritDoc}
   */
  public abstract void avatarUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void basicInfoUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void contactSectionUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void experienceSectionUpdated(ProfileLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void headerSectionUpdated(ProfileLifeCycleEvent event);

}
