package org.exoplatform.social.core.identity.spi;

import org.exoplatform.social.lifecycle.LifeCycleListener;

/**
 * Listen to updates on profiles.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface ProfileListener extends LifeCycleListener<ProfileLifeCycleEvent> {

  /**
   * avatar picture of the profile is updated
   * @param event
   */
  public void avatarUpdated(ProfileLifeCycleEvent event) ;
  
  /**
   * basic account info of the profile are updated
   * @param event
   */
  public void basicInfoUpdated(ProfileLifeCycleEvent event);
  
  /**
   * contact information of the profile is updated
   * @param event
   */
  public void contactSectionUpdated(ProfileLifeCycleEvent event) ;
  
  /**
   * experience section of the profile is updated
   * @param event
   */
  public void experienceSectionUpdated(ProfileLifeCycleEvent event);
  
  /**
   * header section of the profile is updated
   * @param event
   */
  public void headerSectionUpdated(ProfileLifeCycleEvent event) ;
  
}
