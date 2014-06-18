package org.exoplatform.social.extras.injection;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.extras.injection.utils.LoremIpsum4J;

import java.util.HashMap;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityInjector extends AbstractSocialInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String TYPE = "type";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String SPACE_PREFIX = "spacePrefix";
  
  public ActivityInjector(PatternInjectorConfig pattern) {
    super(pattern);
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    
    //
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);
    String type = params.get(TYPE);
    String userPrefix = params.get(USER_PREFIX);
    String spacePrefix = params.get(SPACE_PREFIX);
    init(userPrefix, spacePrefix, userSuffixValue, spaceSuffixValue);

    if (!"space".equals(type) && !"user".equals(type)) {
      getLog().info("'" + type + "' is a wrong value for type parameter. Please set it to 'user' or 'space'. Aborting injection ..." );
      return;
    }

    // Init provider and base name
    String provider = null;
    if ("space".equals(type)) {
      provider = SpaceIdentityProvider.NAME;
    }
    else if ("user".equals(type)) {
      provider = OrganizationIdentityProvider.NAME;
    }

    String fromUser;
    
    for(int i = from; i <= to; ++i) {
      //
      if (provider.equalsIgnoreCase(OrganizationIdentityProvider.NAME)) {
        fromUser = this.userNameSuffixPattern(i);
      } else {
        fromUser = this.spaceNameSuffixPattern(i);
        fromUser = fromUser.replace(".", "");
      }
      
      Identity identity = identityManager.getOrCreateIdentity(provider, fromUser, false);

      for (int j = 0; j < number; ++j) {
        //
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        lorem = new LoremIpsum4J();
        activity.setBody(lorem.getWords(10));
        activity.setTitle(lorem.getParagraphs());
        activityManager.saveActivity(identity, "DEFAULT_ACTIVITY", activity.getTitle());        //
        getLog().info("Activity for " + fromUser + " generated");

      }
    }

  }
}
