package org.exoplatform.social.core.identity.lifecycle;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent;
import org.exoplatform.social.core.identity.spi.ProfileListener;
import org.exoplatform.social.core.identity.spi.ProfileLifeCycleEvent.Type;

import org.exoplatform.social.lifecycle.AbstractLifeCycle;


/**
 * Lifecycle of a Profile.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileLifeCycle extends AbstractLifeCycle<ProfileListener, ProfileLifeCycleEvent> {

  @Override
  protected void dispatchEvent(ProfileListener listener, ProfileLifeCycleEvent event) {
    switch(event.getType()) {
    case AVATAR_UPDATED :
      listener.avatarUpdated(event);
      break;
    case BASIC_UPDATED: 
      listener.basicInfoUpdated(event);
      break;
    case CONTACT_UPDATED: 
      listener.contactSectionUpdated(event);
     break;
    case EXPERIENCE_UPDATED  :
      listener.experienceSectionUpdated(event);
      break;
    case HEADER_UPDATED:
      listener.headerSectionUpdated(event);
      break;      
    default:
      break;      
    }
  }
  
  public void avatarUpdated(String username, Profile profile) {
    broadcast(new ProfileLifeCycleEvent(Type.AVATAR_UPDATED, username, profile));
  }
  
  public void basicUpdated(String username, Profile profile) {
    broadcast(new ProfileLifeCycleEvent(Type.BASIC_UPDATED, username, profile));
  }
  
  public void contactUpdated(String username, Profile profile) {
    broadcast(new ProfileLifeCycleEvent(Type.CONTACT_UPDATED, username, profile));
  }

  public void experienceUpdated(String username, Profile profile) {
    broadcast(new ProfileLifeCycleEvent(Type.EXPERIENCE_UPDATED, username, profile));
  }
  
  public void headerUpdated(String username, Profile profile) {
    broadcast(new ProfileLifeCycleEvent(Type.HEADER_UPDATED, username, profile));
  }  
  
}
