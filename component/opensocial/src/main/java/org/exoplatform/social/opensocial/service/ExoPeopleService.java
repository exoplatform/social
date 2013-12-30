/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.opensocial.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.model.SortOrder;
import org.apache.shindig.social.core.model.ListFieldImpl;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.core.model.UrlImpl;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Url;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.opensocial.auth.ExoBlobCrypterSecurityToken;
import org.exoplatform.social.opensocial.model.ExoPersonImpl;
import org.exoplatform.social.opensocial.model.SpaceImpl;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * The Class ExoPeopleService.
 */
public class ExoPeopleService extends ExoService implements PersonService, AppDataService {

  /**
   * The injector.
   */
  private Injector injector;

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(ExoPeopleService.class);
  
  /**
   * Instantiates a new exo people service.
   *
   * @param injector the injector
   */
  @Inject
  public ExoPeopleService(Injector injector) {
    this.injector = injector;
  }

  /**
   * The Constant NAME_COMPARATOR.
   */
  private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
    public int compare(Person person, Person person1) {
      String name = person.getName().getFormatted();
      String name1 = person1.getName().getFormatted();
      return name.compareTo(name1);
    }
  };


  /**
   * {@inheritDoc}
   */
  public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId,
                                                     CollectionOptions collectionOptions, Set<String> fields,
                                                     SecurityToken token) throws ProtocolException {
    List<Person> result = Lists.newArrayList();
    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);

      Iterator<Identity> it = idSet.iterator();

      while (it.hasNext()) {
        Identity id = it.next();
        if (id != null) {
          result.add(convertToPerson(id, fields, token));
        }
      }

      // We can pretend that by default the people are in top friends order
      if (collectionOptions.getSortBy().equals(Person.Field.NAME.toString())) {
        Collections.sort(result, NAME_COMPARATOR);
      }

      if (collectionOptions.getSortOrder().equals(SortOrder.descending)) {
        Collections.reverse(result);
      }

      // TODO: The eXocontainer doesn't  have the concept of HAS_APP so
      // we can't support any filters yet. We should fix this.

      int totalSize = result.size();
      int fromIndex = collectionOptions.getFirst();
      int toIndex = fromIndex + collectionOptions.getMax();
      fromIndex = fromIndex < 0 ? 0 : fromIndex;
      toIndex = totalSize < toIndex ? totalSize : toIndex;
      toIndex = toIndex < fromIndex ? fromIndex : toIndex;
      result = result.subList(fromIndex, toIndex);

      return ImmediateFuture.newInstance(new RestfulCollection<Person>(
              result, collectionOptions.getFirst(), totalSize));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token) throws ProtocolException {
    try {

      if (token instanceof AnonymousSecurityToken) {
        throw new Exception(Integer.toString(HttpServletResponse.SC_FORBIDDEN));
      }

      Identity identity = getIdentity(id.getUserId(token), true, token);

      return ImmediateFuture.newInstance(convertToPerson(identity, fields, token));
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Converts to person.
   *
   * @param identity the identity
   * @param fields   the fields
   * @return the person
   * @throws Exception
   */
  private Person convertToPerson(Identity identity, Set<String> fields, SecurityToken st) throws Exception {
    Person p = new ExoPersonImpl();
    Profile pro = identity.getProfile();
    PortalContainer container = getPortalContainer(st);
    String host = getHost(st);
    for (String field : fields) {
      if (Person.Field.DISPLAY_NAME.toString().equals(field)) {
        p.setDisplayName(pro.getFullName());
      } else if (Person.Field.EMAILS.toString().equals(field)) {
        p.setEmails(convertToListFields((List<Map>) pro.getProperty("emails")));
      } else if (Person.Field.URLS.toString().equals(field)) {
        p.setUrls(convertToURLListFields((List<Map>) pro.getProperty("urls")));
      } else if (Person.Field.IMS.toString().equals(field)) {
        p.setIms(convertToListFields((List<Map>) pro.getProperty("ims")));
      } else if (Person.Field.ID.toString().equals(field)) {
        p.setId(identity.getId());
      } else if (Person.Field.NAME.toString().equals(field)) {
        NameImpl name = new NameImpl();
        name.setFamilyName((String) pro.getProperty(Profile.LAST_NAME));
        name.setGivenName((String) pro.getProperty(Profile.FIRST_NAME));
        name.setFormatted(name.getGivenName() + " " + name.getFamilyName());
        p.setName(name);
      } else if (Person.Field.PROFILE_URL.toString().equals(field)) {
        String portalOwner = getPortalOwner(st);
        String portalName = getPortalContainer(st).getName();
        p.setProfileUrl(LinkProvider.getAbsoluteProfileUrl(identity.getRemoteId(), portalName, portalOwner, host));
      } else if (Person.Field.GENDER.toString().equals(field)) {
        String gender = (String) pro.getProperty("gender");
        if (gender != null && gender.equals("female")) {
          p.setGender(Person.Gender.female);
        } else {
          p.setGender(Person.Gender.male);
        }
      } else if (ExoPersonImpl.Field.SPACES.toString().equals(field)) {
        List<org.exoplatform.social.opensocial.model.Space> spaces =
                new ArrayList<org.exoplatform.social.opensocial.model.Space>();
        //TODO: dang.tung: improve space to person, it will auto convert field by shindig
        SpaceService spaceService = (SpaceService) (container.getComponentInstanceOfType(SpaceService.class));
        try {
          ListAccess<Space> memberSpaceListAccess = spaceService.getMemberSpaces(identity.getRemoteId());
          //Load 100 maximum only for performance gain
          Space[] spaceArray = memberSpaceListAccess.load(0, 100);
          SpaceImpl space = new SpaceImpl();
          for(Space spaceObj : spaceArray) {
              space.setId(spaceObj.getId());
              space.setDisplayName(spaceObj.getDisplayName());
              spaces.add(space);
          }
          ((ExoPersonImpl) p).setSpaces(spaces);
        } catch (SpaceException e) {
          LOG.warn("Failed to convert spaces!");
        }
      } else if (Person.Field.THUMBNAIL_URL.toString().equals(field)) {
        String avatarUrl = pro.getAvatarUrl();
        if (avatarUrl != null) {
          p.setThumbnailUrl(host + avatarUrl);          
        } else {
          p.setThumbnailUrl(host + LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
        }
      } else if (ExoPersonImpl.Field.PORTAL_CONTAINER.toString().equals(field)) {
        ((ExoPersonImpl) p).setPortalName(container.getName());
      } else if (ExoPersonImpl.Field.REST_CONTEXT.toString().equals(field)) {
        ((ExoPersonImpl) p).setRestContextName(container.getRestContextName());
      } else if (ExoPersonImpl.Field.HOST.toString().equals(field)) {
        ((ExoPersonImpl) p).setHostName(getHost(st));
      } else if (ExoPersonImpl.Field.PEOPLE_URI.toString().equals(field)) {
        ((ExoPersonImpl) p).setPeopleUri(getURIForPeople(container, identity.getRemoteId()));
      }
    }
    return p;
  }

  /**
   * Convert to list fields.
   *
   * @param fields the fields
   * @return the list
   */
  private List<ListField> convertToListFields(List<Map> fields) {
    List<ListField> l = new ArrayList<ListField>();
    if (fields == null) {
      return null;
    }
    for (Map field : fields) {
      l.add(new ListFieldImpl((String) field.get("key"), (String) field.get("value")));
    }
    return l;
  }

  /**
   * Convert to url list fields.
   *
   * @param fields the fields
   * @return the list
   */
  private List<Url> convertToURLListFields(List<Map> fields) {
    List<Url> l = new ArrayList<Url>();
    if (fields == null) {
      return null;
    }
    for (Map field : fields) {
      l.add(new UrlImpl((String) field.get("value"), (String) field.get("key"), (String) field.get("key")));
    }
    return l;
  }

  /**
   * {@inheritDoc}
   */
  public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId,
                                              Set<String> fields, SecurityToken token) throws ProtocolException {
    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);
      Map<String, Map<String, String>> idToData = new HashMap<String, Map<String, String>>();
      Iterator<Identity> it = idSet.iterator();

      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();
      while (it.hasNext()) {
        Identity id = it.next();
        idToData.put(id.getId(), getPreferences(id.getRemoteId(), gadgetId, instanceId, fields));
      }
      return ImmediateFuture.newInstance(new DataCollection(idToData));
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Clean.
   *
   * @param url the url
   * @return the string
   */
  private String clean(String url) {
    url = URLEncoder.encode(url);
    url = url.replaceAll(":", "");
    return url;
  }

  /**
   * Gets the preferences.
   *
   * @param userID     the user id
   * @param gadgetId   the gadget id
   * @param instanceID the instance id
   * @param fields     the fields
   * @return the preferences
   * @throws Exception the exception
   */
  private Map<String, String> getPreferences(String userID, String gadgetId, String instanceID,
                                             Set<String> fields) throws Exception {
//      PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
//      UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);
//
//      Map<String, String> values = userGadgetStorage.get(userID, gadgetId, instanceID, fields);
    Map<String, String> values = null;
    return values;
  }

  /**
   * Save preferences.
   *
   * @param userID     the user id
   * @param gadgetId   the gadget id
   * @param instanceID the instance id
   * @param values     the values
   * @throws Exception the exception
   */
  private void savePreferences(String userID, String gadgetId, String instanceID,
                               Map<String, String> values) throws Exception {
//    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
//    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);
//
//    userGadgetStorage.save(userID, gadgetId, instanceID, values);
  }

  /**
   * Delete preferences.
   *
   * @param userID     the user id
   * @param gadgetId   the gadget id
   * @param instanceID the instance id
   * @param keys       the keys
   * @throws Exception the exception
   */
  private void deletePreferences(String userID, String gadgetId, String instanceID, Set<String> keys) throws Exception {
//    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
//    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);
//
//    userGadgetStorage.delete(userID, gadgetId, instanceID, keys);
  }

  /**
   * {@inheritDoc}
   */
  public Future<Void> deletePersonData(UserId user, GroupId groupId, String appId,
                                       Set<String> fields, SecurityToken token) throws ProtocolException {
    try {
      if (token instanceof AnonymousSecurityToken) {
        throw new Exception(Integer.toString(HttpServletResponse.SC_FORBIDDEN));
      }
      String userId = user.getUserId(token);

      String portalName = PortalContainer.getCurrentPortalContainerName();
      if (token instanceof ExoBlobCrypterSecurityToken) {
        portalName = ((ExoBlobCrypterSecurityToken) token).getPortalContainer();
      }

      Identity id = getIdentity(userId, true, token);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      deletePreferences(id.getRemoteId(), gadgetId, instanceId, fields);
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }

  /**
   * {@inheritDoc}
   */
  public Future<Void> updatePersonData(UserId user, GroupId groupId, String appId, Set<String> fields,
                                       Map<String, String> values, SecurityToken token) throws ProtocolException {
    //TODO: remove the fields that are in the fields list and not in the values map
    try {
      if (token instanceof AnonymousSecurityToken) {
        throw new Exception(Integer.toString(HttpServletResponse.SC_FORBIDDEN));
      }
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId, true, token);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      savePreferences(id.getRemoteId(), gadgetId, instanceId, values);
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }

  /**
   * Get the uri people.
   * 
   * @param portalContainer
   * @param remoteId
   * @return
   * @throws Exception
   */
  private String getURIForPeople(PortalContainer portalContainer, String remoteId) throws Exception {
    UserPortalConfigService userPortalConfigSer = (UserPortalConfigService)
                                                  portalContainer.getComponentInstanceOfType(UserPortalConfigService.class);
    
    UserPortalContext NULL_CONTEXT = new UserPortalContext() {
      public ResourceBundle getBundle(UserNavigation navigation) {
        return null;
      }

      public Locale getUserLocale() {
        return Locale.ENGLISH;
      }
    };
    StringBuffer stringBuffer = new StringBuffer();
    RequestLifeCycle.begin(portalContainer);
    try {
      UserPortalConfig userPortalCfg = userPortalConfigSer.
                                       getUserPortalConfig(userPortalConfigSer.getDefaultPortal(), remoteId, NULL_CONTEXT);
      UserPortal userPortal = userPortalCfg.getUserPortal();
      
      SiteKey siteKey = SiteKey.portal(userPortalConfigSer.getDefaultPortal());
      UserNavigation userNav = userPortal.getNavigation(siteKey);
      UserNode rootNode = userPortal.getNode(userNav, Scope.ALL, null, null);
      UserNode peopleNode = rootNode.getChild("people");
      UserNode iteratorNode = peopleNode;
      
      if(iteratorNode != null){
        while(iteratorNode !=null && iteratorNode.getParent()!=null){
          stringBuffer.insert(0, iteratorNode.getName());
          stringBuffer.insert(0, "/");
          iteratorNode = iteratorNode.getParent();
        }
        stringBuffer.insert(0, userPortalConfigSer.getDefaultPortal());
        stringBuffer.insert(0, "/");
        stringBuffer.insert(0, portalContainer.getName());
        stringBuffer.insert(0, "/");
      }
    } catch (Exception e){
      LOG.debug("Could not get the people page node.");
    }
    finally{
      RequestLifeCycle.end();
    }
    return stringBuffer.toString();
  }
}
