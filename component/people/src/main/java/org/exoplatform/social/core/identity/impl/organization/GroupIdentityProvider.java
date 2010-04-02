package org.exoplatform.social.core.identity.impl.organization;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * A provider for identity of groups. based on OrganizationService's groups
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class GroupIdentityProvider extends IdentityProvider<Group> {

  /** The organization service. */
  private OrganizationService organizationService;

  /** The Constant NAME. */
  public final static String  NAME = "group";

  private static final Log    LOG  = ExoLogger.getExoLogger(GroupIdentityProvider.class);

  public GroupIdentityProvider(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Group findByRemoteId(String remoteId) {
    Group group;
    try {
      GroupHandler groupHandler = organizationService.getGroupHandler();
      group = groupHandler.findGroupById(remoteId);
    } catch (Exception e) {
      LOG.error("Could not find group " + remoteId);
      return null;
    }
    return group;
  }

  @Override
  public Identity populateIdentity(Group group) {
    Identity identity = new Identity(NAME, group.getId());
    Profile profile = identity.getProfile();
    profile.setProperty(Profile.FIRST_NAME, group.getLabel());
    profile.setProperty(Profile.USERNAME, group.getGroupName());
    return identity;
  }

}
