package org.exoplatform.social.core.identity.spi;

import org.exoplatform.social.core.identity.lifecycle.ProfileLifeCycle;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.lifecycle.LifeCycleEvent;

/**
 * event propagated along the {@link ProfileLifeCycle}
 * @see {@link ProfileListener}
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ProfileLifeCycleEvent extends LifeCycleEvent<String, Profile> {

  public enum Type {AVATAR_UPDATED, BASIC_UPDATED, CONTACT_UPDATED, EXPERIENCE_UPDATED, HEADER_UPDATED}
  
  private Type type;
  
  public ProfileLifeCycleEvent(Type type, String user, Profile profile) {
    super(user, profile);
    this.type = type;
  }

  public Type getType() {
    return type;
  }
  
  /**
   * username of the profile updated
   * @return
   */
  public String getUsername() {
    return source;
  }
  
  /**
   * actual profile section;
   * @return
   */
  public Profile getProfile() {
    return payload;
  }

}
