package org.exoplatform.social.extras.injection;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.SpaceStorage;

import java.util.HashMap;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceInjector extends AbstractSocialInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    init();
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);

    //
    for(int i = from; i <= to; ++i) {
      for (int j = 0; j < number; ++j) {

        //
        String owner = USER_BASE + i;
        String spaceName = spaceName();

        Space space = new Space();
        space.setDisplayName(spaceName);
        space.setPrettyName(spaceName);
        space.setGroupId("/spaces/" + space.getPrettyName());
        space.setRegistration(Space.OPEN);
        space.setDescription(lorem.getWords(10));
        space.setType(DefaultSpaceApplicationHandler.NAME);
        space.setVisibility(Space.PUBLIC);
        space.setPriority(Space.INTERMEDIATE_PRIORITY);

        //
        spaceService.createSpace(space, owner);
        ++spaceNumber;

        //
        getLog().info("Space " + spaceName + " created by " + owner);



      }
    }
    
  }
}
