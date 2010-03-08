package org.exoplatform.social.core.identity.impl.organization;

import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * A provider for identity of groups. based on OrganizationService's groups
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class GroupIdentityProvider extends IdentityProvider {

  /** The storage. */
  private JCRStorage storage;
  
  /** The organization service. */
  private OrganizationService organizationService;
  
  /** The Constant NAME. */
  public final static String NAME = "group";
  
  private static final Log LOG = ExoLogger.getExoLogger(GroupIdentityProvider.class);
  
  public GroupIdentityProvider(JCRStorage storage, OrganizationService organizationService) {
    this.storage = storage;
    this.organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
  }
  
  @Override
  public Identity getIdentityByRemoteId(Identity identity) throws Exception {

    Group group = null;
    String groupId = identity.getRemoteId();

      try {
        GroupHandler groupHandler = organizationService.getGroupHandler();
        group = groupHandler.findGroupById(groupId);
      } catch (Exception e) {
        LOG.error("Could not find group " + groupId, e);
        return null;
      }

    if (group == null) {
      return null;
    }

    
    identity = storage.getIdentityByRemoteId(NAME, group.getId()); 
  
    return identity;
  }
  
  
  public List<String> getAllUserId() throws Exception {
    throw new RuntimeException("getAllUserId() is not implemented for " + getClass());
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void saveProfile(Profile p) throws Exception {
    this.storage.saveProfile(p); 
  }
  
  
  private Identity loadIdentity(Group group, Identity identity) throws Exception {
    Profile profile = identity.getProfile();
    profile.setProperty("firstName", group.getLabel());
    profile.setProperty("username", group.getGroupName());
    storage.loadProfile(profile);
    return identity;
  }

}
