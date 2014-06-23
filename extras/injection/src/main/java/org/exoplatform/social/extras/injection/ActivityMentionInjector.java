/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.extras.injection;

import java.util.HashMap;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.extras.injection.utils.LoremIpsum4J;

public class ActivityMentionInjector extends AbstractSocialInjector {

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String MENTIONER = "mentioner";
  
  public ActivityMentionInjector(PatternInjectorConfig pattern) {
    super(pattern);
  }


  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    
    //
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    String mentioner = params.get(MENTIONER);
    init(userPrefix, null, userSuffixValue, spaceSuffixValue);


    // Init provider and base name
    String provider = OrganizationIdentityProvider.NAME;;
    String base = userBase;
    
    Identity identityMentioner = identityManager.getOrCreateIdentity(provider, mentioner, false);
    String toMentioner = null;
    if (identityMentioner != null) {
      toMentioner = "@" + identityMentioner.getRemoteId();
    } else {
      getLog().info("'" + mentioner + "' is a wrong value for mentioner's remoteId parameter. Please set it correctly. Aborting injection ..." );
    }
    

    for(int i = from; i <= to; ++i) {

      //
      String fromUser = this.userNameSuffixPattern(i);
      Identity identity = identityManager.getOrCreateIdentity(provider, fromUser, false);

      for (int j = 0; j < number; ++j) {

        //
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        lorem = new LoremIpsum4J();
        activity.setBody(lorem.getWords(10));
        activity.setTitle(toMentioner + " " + lorem.getParagraphs());
        activityManager.saveActivity(identity, "DEFAULT_ACTIVITY", activity.getTitle());

        //
        getLog().info("Activity for " + fromUser + " generated");

      }
    }

  }
}