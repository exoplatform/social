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
  private static final String PATTERN = "pattern";

    /**
     *
     * @param pattern
     * @return name with suffix pattern
     */
  public String userName(String pattern){
      if (pattern == null){
          return userPrettyBase + userNumber;
      }
      else {
          String nameAppend = new StringBuilder().append(pattern).append(userNumber).toString();
          return userPrettyBase + nameAppend.substring(nameAppend.length() - pattern.length());
      }
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    String prefix = params.get(PREFIX);
    String pattern = params.get(PATTERN);
    init(prefix, null);

    //
    for(int i = 0; i < number; ++i) {

      //

      String username = userName(pattern);
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
