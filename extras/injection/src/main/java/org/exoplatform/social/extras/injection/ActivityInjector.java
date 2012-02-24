package org.exoplatform.social.extras.injection;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.SpaceStorage;
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

  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    
    //
    init();
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);
    String type = params.get(TYPE);

    if (!"space".equals(type) && !"user".equals(type)) {
      getLog().info("'" + type + "' is a wrong value for type parameter. Please set it to 'user' or 'space'. Aborting injection ..." );
      return;
    }

    // Init provider and base name
    String provider = null;
    String base = null;
    if ("space".equals(type)) {
      provider = SpaceIdentityProvider.NAME;
      base = SPACE_BASE_PRETTY_NAME;
    }
    else if ("user".equals(type)) {
      provider = OrganizationIdentityProvider.NAME;
      base = USER_BASE;
    }

    for(int i = from; i <= to; ++i) {

      //
      String fromUser = base + i;
      Identity identity = identityManager.getOrCreateIdentity(provider, fromUser, false);

      for (int j = 0; j < number; ++j) {

        //
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        lorem = new LoremIpsum4J();
        activity.setBody(lorem.getWords(10));
        activity.setTitle(lorem.getParagraphs());
        activityManager.saveActivity(identity, "DEFAULT_ACTIVITY", activity.getTitle());

        //
        getLog().info("Activity for " + fromUser + " generated");

      }
    }

  }
}
