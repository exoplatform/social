package org.exoplatform.social.extras.injection;

import java.util.HashMap;

import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityInjector extends AbstractSocialInjector {
  /** . */
  private static final String NUMBER = "number";
  private static final String PREFIX = "prefix";
  
  public IdentityInjector(PatternInjectorConfig pattern) {
    super(pattern);
  }
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String prefix = params.get(PREFIX);
    init(prefix, null, userSuffixValue, spaceSuffixValue);

    //
    for(int i = 0; i < number; ++i) {

      //
      String username = this.userName();
      User user = userHandler.createUserInstance(username);
      user.setEmail(username + "@" + DOMAIN);
      user.setFirstName(nameGenerator.compose(3));
      user.setLastName(nameGenerator.compose(4));
      user.setPassword(this.password);

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
