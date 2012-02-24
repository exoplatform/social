package org.exoplatform.social.extras.injection;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;

import java.util.HashMap;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipInjector extends AbstractSocialInjector {

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

    if (number <= 0 || number > to - from) {
      getLog().error("Number have to be positive and lesser than the range. Value '" + number + "' incorrect. Aborting injection ...");
      return;
    }

    // Check if possible and adjust number if needed.
    boolean possible = isPossible(from, to, number);
    if (!possible) {
      int found = findPossible(from, to, number);
      if (found > 0) {
        getLog().info("Unable to generate exactly " + number + " connections for each users. Injector we will use " + found + " instead.");
        number = found;
      }
      else {
        getLog().error("Unable to find better value. Aborting injection ...");
        return;
      }
    }
    
    for(int i = from; i <= to; ++i) {

      //
      String fromUser = USER_BASE + i;
      Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, fromUser, false);
 
      //
      int target = number + from;
      for (int j = from; j < target; ++j) {

        // Cannot create relationship with himself
        if (i == j) {
          ++target;
          continue;
        }

        //
        String toUser = USER_BASE + j;
        Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, toUser, false);

        boolean exists = relationshipManager.get(identity1, identity2) != null;
        if (!exists) {
          
          int size = relationshipManager.getConnections(identity2).getSize();
          if (size >= number) {
            ++target;
            continue;
          }

          //
          Relationship r = relationshipManager.inviteToConnect(identity1, identity2);
          r.setStatus(Relationship.Type.CONFIRMED);
          relationshipManager.update(r);

          //
          getLog().info("Relationship between " + fromUser + " and " + toUser + " generated");

        }
        else {

          //
          getLog().info("Relationship between " + fromUser + " and " + toUser + " already exists");

        }


      }
    }

  }

  /**
   * @param a begin range
   * @param b end range
   * @param c number
   */
  public boolean isPossible(int a, int b, int c) {

    return (b - a + 1F) % (c + 1F) == 0;

  }

  public int findPossible(int a, int b, int c) {

    for (int i = c - 1; i > 0; --i) {
      if (isPossible(a, b, i)) {
        return i;
      }
    }

    for (int i = c + 1; i < (b - a); ++i) {
      if (isPossible(a, b, i)) {
        return i;
      }
    }

    return b - a;

  }

}
