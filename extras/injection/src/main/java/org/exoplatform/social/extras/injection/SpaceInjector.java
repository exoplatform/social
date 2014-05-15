package org.exoplatform.social.extras.injection;

import java.util.HashMap;

import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceInjector extends AbstractSocialInjector {
  private final int FLUSH_LIMIT = 1;

  /** . */
  private static final String NUMBER = "number";

  /** . */
  private static final String FROM_USER = "fromUser";

  /** . */
  private static final String TO_USER = "toUser";

  /** . */
  private static final String USER_PREFIX = "userPrefix";

  /** . */
  private static final String SPACE_PREFIX = "spacePrefix";

  private static final String PATTERN = "pattern";
  
   /**
   * @param pattern
   * @param index 
   * @return userName with new pattern
   */
  private String userName(String pattern,int index){
      if (pattern == null){
          return userPrettyBase + index;
      }
      else {
          String nameAppend = new StringBuilder().append(pattern).append(index).toString();
          return userPrettyBase + nameAppend.substring(nameAppend.length() - pattern.length());
      }
  }
  
   /**
   * @param pattern
   * @return spaceName with new pattern
   */
  private String spaceName(String pattern){
      if (pattern == null){
          return spacePrettyBase + spaceNumber;
      }
      else {
          String nameAppend = new StringBuilder().append(pattern).append(spaceNumber).toString();
          return spacePrettyBase + nameAppend.substring(nameAppend.length() - pattern.length());
      }
  }

  @Override
  public void inject(HashMap<String, String> params) throws Exception {

    //
    int number = param(params, NUMBER);
    int from = param(params, FROM_USER);
    int to = param(params, TO_USER);
    String userPrefix = params.get(USER_PREFIX);
    String spacePrefix = params.get(SPACE_PREFIX);
    String pattern = params.get(PATTERN);

    init(userPrefix, spacePrefix);
    String userPrettyBase = userBase.replace(".", "");
    int spaceCounter = 0;

    try {
      //
      for(int i = from; i <= to; ++i) {
        for (int j = 0; j < number; ++j) {

          //create owner name with new pattern
          String owner = userName(pattern,i);
          //create space name with new pattern
          String spaceName = spaceName(pattern);

          Space space = new Space();
          space.setDisplayName(spaceName);
          space.setPrettyName(spaceName);
          space.setGroupId("/spaces/" + space.getPrettyName());
          space.setRegistration(Space.OPEN);
          space.setDescription(lorem.getWords(10));
          space.setType(DefaultSpaceApplicationHandler.NAME);
          space.setVisibility(Space.PRIVATE);
          space.setRegistration(Space.OPEN);
          space.setPriority(Space.INTERMEDIATE_PRIORITY);

          //
          spaceService.createSpace(space, owner);
          ++spaceNumber;
          if (++spaceCounter == FLUSH_LIMIT) {
            spaceCounter = 0;
            //
            SpaceUtils.endRequest();
            getLog().info("Flush session...");
          }
          //
          getLog().info("Space " + spaceName + " created by " + owner);

        }
      }
    } finally {
      SpaceUtils.endRequest();
    }
    
  }
}
