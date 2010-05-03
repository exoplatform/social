package org.exoplatform.social.application;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

public class ApplicationsIdentityProvider extends IdentityProvider<Application> {

  /** The Constant NAME. */
  public final static String  NAME = "apps";
  
  
  private static Map<String,Application> appsByUrl = new HashMap<String,Application>();
  
  @Override
  public Application findByRemoteId(String remoteId) {
    return appsByUrl.get(remoteId);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Identity populateIdentity(Application app) {
    Identity identity = new Identity(NAME, app.getId());
    Profile profile = identity.getProfile();
    profile.setProperty(Profile.AVATAR, app.getIcon());
    profile.setProperty(Profile.USERNAME, app.getName());
    profile.setProperty(Profile.FIRST_NAME, app.getName());
    return identity;
  }
  
  public void addApplication(Application app) {
    appsByUrl.put(app.getId(), app);
  }

}
