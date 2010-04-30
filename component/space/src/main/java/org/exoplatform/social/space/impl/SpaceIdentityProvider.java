package org.exoplatform.social.space.impl;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;

/**
 * provides identity for a space
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceIdentityProvider extends IdentityProvider<Space> {

  private static final Log LOG = ExoLogger.getExoLogger(SpaceIdentityProvider.class);

  public static final String NAME = "space";

  private SpaceService spaceService;

  
  public SpaceIdentityProvider(SpaceService spaceService) {
    this.spaceService = spaceService;
  }
  
  public Space findByRemoteId(String spaceId) {
    Space space;
    try {
      space = spaceService.getSpaceById(spaceId);
      
      // attempt to find by name
      if (space ==null) {
        String name = spaceId;
        if (spaceId.contains(":")) {
          name = spaceId.split(":")[1];
        }
        
        List<Space> spaces = spaceService.getSpacesByName(name, false);
        if (spaces != null && spaces.size()>0) {
          return spaces.get(0); // first match
        }
      }
    } catch (Exception e) {
      LOG.error("Could not find space " + spaceId, e);
      return null;
    }
    return space;
  }

  @Override
  public String getName() {
    return NAME;
  }

 

  @Override
  public Identity populateIdentity(Space space) {
    Identity identity = new Identity(NAME, space.getId());
    Profile profile = identity.getProfile();
    profile.setProperty(Profile.FIRST_NAME, space.getName());
    profile.setProperty(Profile.USERNAME, space.getGroupId());
    return identity;
  }

}
