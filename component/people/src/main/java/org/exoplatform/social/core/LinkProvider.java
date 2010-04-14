package org.exoplatform.social.core;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;

public class LinkProvider {

  private IdentityManager identityManager;
  private static Log LOG = ExoLogger.getLogger(LinkProvider.class);

  public LinkProvider(InitParams params, IdentityManager identityManager) {
    this.identityManager = identityManager;
    init(params);
  }
  
  private void init(InitParams params) {
  
  }

  public String getProfileLink(String username) {
    
    String link = username;
      try {
      Identity identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME + ":" + username, true);
      if (identity == null) {
        throw new RuntimeException("could not find a user identity for " + username);
      }
      
      String container = PortalContainer.getCurrentPortalContainerName();
      String url = "/"+ container +"/private/classic/profile/" + identity.getRemoteId();
      link = "<a href=\"" + url + "\" class=\"link\" target=\"_parent\">" + identity.getProfile().getFullName() + "</a>";

      } catch (Exception e) {
        LOG.error("failed to substitute username for " + username + ": " + e.getMessage());
      }
      
      return link;
    } 
  
}
