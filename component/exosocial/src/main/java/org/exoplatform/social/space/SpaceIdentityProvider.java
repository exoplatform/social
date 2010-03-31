package org.exoplatform.social.space;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;

/**
 * provides identity for a space
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceIdentityProvider extends IdentityProvider {
  
  private static final Log LOG = ExoLogger.getExoLogger(SpaceIdentityProvider.class);

  public static final String NAME = "space";
  private SpaceService spaceService;
  /** The storage. */
  private JCRStorage identityStorage;
  
  
  public SpaceIdentityProvider(SpaceService spaceService, JCRStorage storage) {
    this.spaceService = spaceService;
    this.identityStorage = storage;
  }
  
  @Override
  public Identity getIdentityByRemoteId(String remoteId) throws Exception {
    String spaceId = remoteId;
    Space space;
    try {
      space = spaceService.getSpaceById(spaceId);
    } catch (Exception e) {
      LOG.error("Could not find space " + spaceId, e);
      return null;
    }

    // space not found
    if (space == null) {
      return null;
    }
    
    // get space identity from identity storage
    Identity identity = new Identity(NAME, space.getId());//identityStorage.findIdentity();

    return identity;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void saveProfile(Profile p) throws Exception {
 // no profile for spaces
  }

}
