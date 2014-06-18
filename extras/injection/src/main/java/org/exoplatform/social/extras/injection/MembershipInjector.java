package org.exoplatform.social.extras.injection;

import java.util.HashMap;

import org.apache.poi.hslf.record.CurrentUserAtom;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;

public class MembershipInjector extends AbstractSocialInjector {

  /** . */
  private static final String MEMBERSHIP_TYPE = "type";
  
  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";
  
  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String FROM_SPACE = "fromSpace";

  /** . */
  private static final String TO_SPACE = "toSpace";
  
  /** . */
  private static final String SPACE_PREFIX = "spacePrefix";
  
  /** . */
  private String identitiesInfo = "";
  
  public MembershipInjector(PatternInjectorConfig pattern) {
    super(pattern);
  }
  
  @Override
  public void inject(HashMap<String, String> params) throws Exception {
    
    //
    String type = params.get(MEMBERSHIP_TYPE);
    
    //
    int fromUser = param(params, FROM_USER);
    int toUser = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    //
    int fromSpace = param(params, FROM_SPACE);
    int toSpace = param(params, TO_SPACE);
    String spacePrefix = params.get(SPACE_PREFIX);
    
    if (!"manager".equals(type) && !"member".equals(type)) {
      getLog().info("'" + type + "' is a wrong value for membership type parameter. Please set it to 'member' or 'manager'. Aborting injection ..." );
      return;
    }
    
    init(userPrefix, spacePrefix, userSuffixValue, spaceSuffixValue);

   
    getLog().info("About to inject Space Members :");
    getLog().info("" + (toSpace - fromSpace + 1) + " space(s) has prefix '" + spacePrefix + " ' with " + (toUser - fromUser + 1) + " member(s) has prefix '" + userPrefix + "'");
      
    int floor = fromSpace;
    for (int i = floor; i <= toSpace; ++i) {
      generate(i, type, fromUser, toUser);
    }
    
    getLog().info("completed to inject Space Members");
  }
  
   
  
  private void generate(int spaceIdx, String type, int from, int to) {
    String spaceName = this.spaceNameSuffixPattern(spaceIdx);
    String spacePrettyBaseName = SpaceUtils.cleanString(spaceName);

    Space space = spaceService.getSpaceByPrettyName(spacePrettyBaseName);
    
    if (space == null) {
      getLog().info("space with display name: " + spaceName + "is not existing");
      return;
    }
    
    //space.setEditor(ConversationState.getCurrent().getIdentity().getUserId());
    
    Identity identity = null;
    for(int i = from; i <= to; ++i) {
      String username = this.userNameSuffixPattern(i);
      identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
      space.setEditor(username);
      getLog().info("added user(s) " + identity.getRemoteId() + " to " + type + " of '" + spaceName + "' space.");
      if ("member".endsWith(type)) {
        spaceService.addMember(space, identity.getRemoteId());
      } else if ("manager".endsWith(type)) {
        spaceService.setManager(space, identity.getRemoteId(), true);
      }
      
    }

  }
}
