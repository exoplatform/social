package org.exoplatform.social.extras.injection;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;

import java.util.HashMap;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityInjector extends AbstractSocialInjector {

  /** . */
  private static final String NUMBER = "number";
  private static final String PREFIX = "prefix";

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String prefix = params.get(PREFIX);
    init(prefix, null);

    //
    for(int i = 0; i < number; ++i) {

      //
      String username = userName();
      User user = userHandler.createUserInstance(username);
      user.setEmail(username + "@" + DOMAIN);
      user.setFirstName(nameGenerator.compose(3));
      user.setLastName(nameGenerator.compose(4));
      user.setPassword(PASSWORD);

      try {

        //
        userHandler.createUser(user, true);
        identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);

        //
        ++userNumber;

      } catch (Exception e) {
        getLog().error(e);
      }

      //
      getLog().info("User " + username + " generated");

    }

  }

}
