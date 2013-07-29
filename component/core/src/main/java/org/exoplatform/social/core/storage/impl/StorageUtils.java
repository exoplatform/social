package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.Synchronization;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.QueryFunction;
import org.exoplatform.social.core.storage.query.WhereExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class StorageUtils {

  //
  public static final String ASTERISK_STR = "*";
  public static final String PERCENT_STR = "%";
  public static final char   ASTERISK_CHAR = '*';
  public static final String SPACE_STR = " ";
  public static final String EMPTY_STR = "";
  public static final String SLASH_STR = "/";
  public static final String SOC_ACTIVITY_INFO = "soc:activityInfo";
  public static final String SOC_PREFIX = "soc:";
  
  //
  private static List<String> userInPlatformGroups = null;
  
  //
  private static final Log LOG = ExoLogger.getLogger(StorageUtils.class.getName());
  
  public static void applyFilter(final WhereExpression whereExpression, final ProfileFilter profileFilter) {
    //
    String inputName = profileFilter.getName().replace(ASTERISK_STR, PERCENT_STR);
    processUsernameSearchPattern(inputName.trim());
    String position = addPositionSearchPattern(profileFilter.getPosition().trim()).replace(ASTERISK_STR, PERCENT_STR);
    inputName = inputName.isEmpty() ? ASTERISK_STR : inputName;
    String nameForSearch = inputName.replace(ASTERISK_STR, SPACE_STR);
    char firstChar = profileFilter.getFirstCharacterOfName();
    String skills = profileFilter.getSkills();

    //
    if (firstChar != '\u0000') {
      whereExpression.and().like(
          whereExpression.callFunction(QueryFunction.LOWER, ProfileEntity.lastName),
          String.valueOf(firstChar).toLowerCase() + PERCENT_STR
      );
    }
    else if (nameForSearch.trim().length() != 0) {
      whereExpression.and().like(
          whereExpression.callFunction(QueryFunction.LOWER, ProfileEntity.fullName),
          PERCENT_STR + nameForSearch.toLowerCase() + PERCENT_STR
      );
    }

    if (position.length() != 0) {
      whereExpression.and().like(
          whereExpression.callFunction(QueryFunction.LOWER, ProfileEntity.position),
          PERCENT_STR + position.toLowerCase() + PERCENT_STR
      );
    }

    if (skills.length() != 0) {
      whereExpression.and().like(
          whereExpression.callFunction(QueryFunction.LOWER, ProfileEntity.skills),
          PERCENT_STR + skills.toLowerCase() + PERCENT_STR
      );
    }

    if (profileFilter.getAll().length() != 0) {
      String value = profileFilter.getAll();

      whereExpression.and().startGroup()
          .contains(ProfileEntity.fullName, value.toLowerCase())
          .or().contains(ProfileEntity.firstName, value.toLowerCase())
          .or().contains(ProfileEntity.lastName, value.toLowerCase())
          .or().contains(ProfileEntity.position, value.toLowerCase())
          .or().contains(ProfileEntity.skills, value.toLowerCase())
          .or().contains(ProfileEntity.positions, value.toLowerCase())
          .or().contains(ProfileEntity.organizations, value.toLowerCase())
          .or().contains(ProfileEntity.jobsDescription, value.toLowerCase())
          .endGroup();
    }

  }

  public static void applyExcludes(final WhereExpression whereExpression, final List<Identity> excludedIdentityList) {

    if (excludedIdentityList != null & excludedIdentityList.size() > 0) {
      for (Identity identity : excludedIdentityList) {
        whereExpression.and().not().equals(ProfileEntity.parentId, identity.getId());
      }
    }
  }

  public static void applyWhereFromIdentity(final WhereExpression whereExpression, final List<Identity> identities) {

    //
    whereExpression.startGroup();
    for (int i = 0; identities.size() > i; ++i) {
      Identity current = identities.get(i);
      whereExpression.equals(JCRProperties.id, current.getProfile().getId());
      if (i + 1 < identities.size()) {
        whereExpression.or();
      }
    }
    whereExpression.endGroup();
    
  }

  public static String processUsernameSearchPattern(final String userName) {
    String modifiedUserName = userName;
    if (modifiedUserName.length() > 0) {
      modifiedUserName =
          ((EMPTY_STR.equals(modifiedUserName)) || (modifiedUserName.length() == 0))
              ? ASTERISK_STR
              : modifiedUserName;

      modifiedUserName =
          (modifiedUserName.charAt(0) != ASTERISK_CHAR) ? ASTERISK_STR + modifiedUserName : modifiedUserName;

      modifiedUserName =
          (modifiedUserName.charAt(modifiedUserName.length() - 1) != ASTERISK_CHAR)
              ? modifiedUserName += ASTERISK_STR
              : modifiedUserName;

      modifiedUserName =
          (modifiedUserName.indexOf(ASTERISK_STR) >= 0)
              ? modifiedUserName.replace(ASTERISK_STR, "." + ASTERISK_STR)
              : modifiedUserName;

      modifiedUserName =
          (modifiedUserName.indexOf(PERCENT_STR) >= 0)
              ? modifiedUserName.replace(PERCENT_STR, "." + ASTERISK_STR)
              : modifiedUserName;

      Pattern.compile(modifiedUserName);
    }
    return userName;
  }

  public static String addPositionSearchPattern(final String position) {
    if (position.length() != 0) {
      if (position.indexOf(ASTERISK_STR) == -1) {
        return ASTERISK_STR + position + ASTERISK_STR;
      }
      return position;
    }
    return EMPTY_STR;
  }

  /**
   * Encodes Url to conform to the generated Url of WEBDAV.
   * Currently, Could not load data from generated url that contain dot character (.) cause by not consist with WEBDAV.
   * This method replace any percent character (%) by (%25) to solve this problem. 
   * @param avatar
   * @return
   */
  public static String encodeUrl(String path) {
    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle)
                                          manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    ChromatticSession chromatticSession = lifeCycle.getSession();
    StringBuilder encodedUrl = new StringBuilder(); 
    encodedUrl = encodedUrl.append("/").append(container.getRestContextName()).append("/jcr/").
                              append(lifeCycle.getRepositoryName()).append("/").
                              append(chromatticSession.getJCRSession().getWorkspace().getName()).
                              append(path.replaceAll("%", "%25"));
    return encodedUrl.toString();
  }
  
  /**
   * Checks Identity in Social is activated or not
   * @param identity
   * @return TRUE activated otherwise FALSE
   * @throws IdentityStorageException
   */
  public static boolean isUserActivated(String remoteId) throws IdentityStorageException {

    try {
      //
      if (userInPlatformGroups == null) {
        OrganizationService orgService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
        
        //
        ListAccess<User> listAccess = orgService.getUserHandler()
                                                .findUsersByGroupId(SpaceUtils.PLATFORM_USERS_GROUP);

        int offset = 0;
        int limit = 100;
        int totalSize = listAccess.getSize();

        userInPlatformGroups = new ArrayList<String>();
        limit = Math.min(limit, totalSize);
        int loaded = 0;
        
        loaded = loadUserRange(listAccess, offset, limit, userInPlatformGroups);
        
        if (limit != totalSize) {
          while (loaded == 100) {
            offset += limit;
            
            //prevent to over totalSize
            if (offset + limit > totalSize) {
              limit = totalSize - offset;
            }
            
            //
            loaded = loadUserRange(listAccess, offset, limit, userInPlatformGroups);
          }
        }
      }
      
      //
      return userInPlatformGroups.contains(remoteId);
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_BY_PROFILE_FILTER,
                                         e.getMessage());
    }
  }
  
  /**
   * Gets User range for given group
   * @param listAccess
   * @param offset
   * @param limit
   * @param userList
   * @return
   * @throws Exception
   */
  private static int loadUserRange(ListAccess<User> listAccess, int  offset, int limit, List<String> userList) throws Exception {
    User[] gotList = listAccess.load(offset, limit);
    for(User item : gotList) {
      userList.add(item.getUserName());
    }
    //
    return gotList.length;
  }
  
  /**
   * there is any update in platform/users group, we need to take care.
   */
  public static void clearUsersPlatformGroup() {
    userInPlatformGroups = null;
  }
  
  /**
   * Gets common item number from two list
   * @param m the first list
   * @param n the second list
   * @return number of common item
   */
  public static <T> int getCommonItemNumber(final List<T> m, final List<T> n) {
    if (m == null || n == null) {
      return 0;
    }
    List<T> copy = new ArrayList<T>(m);
    copy.removeAll(n);
    
    return (m.size() - copy.size());
  }
  
  /**
   * Sort one map by its value
   * @param map the input map
   * @param asc indicate sort by ASC (true) or DESC (false)
   * @return the sorted map
   * @since 4.0.x
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue( Map<K, V> map , final boolean asc) {
    //
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
    //
    Collections.sort( list, new Comparator<Map.Entry<K, V>>() {
      public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 ) {
        if (asc)
          return (o1.getValue()).compareTo( o2.getValue() );
        else
          return (o1.getValue()).compareTo( o2.getValue() )/-1;
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }
  
  /**
   * Gets sub list from the provided list with start and end index.
   * @param list the identity list
   * @param startIndex start index to get
   * @param toIndex end index to get
   * @return sub list of the provided list
   */
  public static <T> List<T> subList(List<T> list, int startIndex, int toIndex) {
    int totalSize = list.size();
    
    if (startIndex >= totalSize) return Collections.emptyList();
    
    //
    if ( toIndex >= totalSize ) {
      toIndex = totalSize;
    }
    
    return list.subList(startIndex, toIndex);
  }
  
  /*
   * Gets added element when compares between l1 and l2
   * @param l1
   * @param l2
   * @return
   */
  public static String[] sub(String[] l1, String[] l2) {

    if (l1 == null) {
      return new String[]{};
    }

    if (l2 == null) {
      return l1;
    }

    List<String> l = new ArrayList(Arrays.asList(l1));
    l.removeAll(Arrays.asList(l2));
    return l.toArray(new String[]{});
  }
  /**
   * Make the decision to persist JCR Storage or not
   * @return
   */
  public static boolean persist() {
    try {
      ChromatticSession chromatticSession = AbstractStorage.lifecycleLookup().getSession();
      if (chromatticSession.getJCRSession().hasPendingChanges()) {
        chromatticSession.save();
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  /**
   * Make the decision to persist JCR Storage or not
   * @return
   */
  public static boolean persistJCR(boolean beginRequest) {
    try {
      //push to JCR
      AbstractStorage.lifecycleLookup().closeContext(true);
      
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
      Synchronization synchronization = manager.getSynchronization();
      synchronization.setSaveOnClose(true);
      //close synchronous and session.logout
      manager.endRequest(true);
      //
      if (beginRequest) {
        manager.beginRequest();
      }
      
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a collection containing all the elements in <code>list1</code> that
   * are also in <code>list2</code>.
   * 
   * @param list1
   * @param list2
   * @return
   */
  public <T> List<T> intersection(List<T> list1, List<T> list2) {
    List<T> list = new ArrayList<T>();

    for (T t : list1) {
      if (list2.contains(t)) {
        list.add(t);
      }
    }

    return list;
  }
  /**
   * Returns a array containing all the elements in <code>list1</code> that
   * @param array1
   * @param array2
   * @return
   */
  public <T> T[] intersection(T[] array1, T[] array2) {
    List<T> got = intersection(Arrays.asList(array1), Arrays.asList(array2));
    return (T[]) got.toArray();
  }
  
  /**
   * Returns a new {@link List} containing a - b
   * @param a
   * @param b
   * @return
   */
  public static <T> List<T> sub(final Collection<T> a, final Collection<T> b) {
    ArrayList<T> list = new ArrayList<T>(a);
    for (Iterator<T> it = b.iterator(); it.hasNext();) {
        list.remove(it.next());
    }
    return list;
}
}
