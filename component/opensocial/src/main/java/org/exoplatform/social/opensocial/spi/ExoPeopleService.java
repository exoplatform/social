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
package org.exoplatform.social.opensocial.spi;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.opensocial.model.impl.ExoPersonImpl;
import org.exoplatform.social.opensocial.model.impl.SpaceImpl;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;

// TODO: Auto-generated Javadoc
/**
 * The Class ExoPeopleService.
 */
public class ExoPeopleService extends ExoService implements PersonService, AppDataService {

  /** The injector. */
  private Injector injector;
  
  /**
   * Instantiates a new exo people service.
   * 
   * @param injector the injector
   */
  @Inject
  public ExoPeopleService(Injector injector) {
    this.injector = injector;
  }
  
  /** The Constant NAME_COMPARATOR. */
  private static final Comparator<Person> NAME_COMPARATOR = new Comparator<Person>() {
    public int compare(Person person, Person person1) {
      String name = person.getName().getFormatted();
      String name1 = person1.getName().getFormatted();
      return name.compareTo(name1);
    }
  };

    
  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.PersonService#getPeople(java.util.Set, org.apache.shindig.social.opensocial.spi.GroupId, org.apache.shindig.social.opensocial.spi.CollectionOptions, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds, GroupId groupId, CollectionOptions collectionOptions, Set<String> fields, SecurityToken token) throws ProtocolException {
    List<Person> result = Lists.newArrayList();
    try {

      Set<Identity> idSet = getIdSet(userIds, groupId, token);

      Iterator<Identity> it = idSet.iterator();

        while(it.hasNext()) {
            Identity id = it.next();

            if(id != null) {
              result.add(convertToPerson(id, fields));
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
      int last = collectionOptions.getFirst() + collectionOptions.getMax();
      result = result.subList(collectionOptions.getFirst(), Math.min(last, totalSize));

      return ImmediateFuture.newInstance(new RestfulCollection<Person>(
          result, collectionOptions.getFirst(), totalSize));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }



  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.PersonService#getPerson(org.apache.shindig.social.opensocial.spi.UserId, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token) throws ProtocolException {
    try {

        Identity identity = getIdentity(id.getUserId(token));


        return ImmediateFuture.newInstance(convertToPerson(identity, fields));
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
      }
  }

  /**
   * Convert to person.
   * 
   * @param identity the identity
   * @param fields the fields
   * @return the person
   */
  private Person convertToPerson(Identity identity, Set<String> fields) {
    Person p = injector.getInstance(Person.class);
    Profile pro = identity.getProfile();

    for (String field : fields) {
      if(Person.Field.DISPLAY_NAME.toString().equals(field)) {
        p.setDisplayName(pro.getFullName());  
      }
      else if(Person.Field.EMAILS.toString().equals(field)) {
        p.setEmails(convertToListFields((List<Map>) pro.getProperty("emails")));  
      }
      else if(Person.Field.URLS.toString().equals(field)) {
        p.setUrls(convertToURLListFields((List<Map>) pro.getProperty("urls")));  
      }
      else if(Person.Field.IMS.toString().equals(field)) {
        p.setIms(convertToListFields((List<Map>) pro.getProperty("ims")));  
      }
      else if(Person.Field.ID.toString().equals(field)) {
        p.setId(identity.getId());  
      }
      else if(Person.Field.NAME.toString().equals(field)) {
        NameImpl name = new NameImpl();
        name.setFamilyName((String) pro.getProperty("lastName"));
        name.setGivenName((String) pro.getProperty("firstName"));
        name.setFormatted(name.getGivenName() + " " + name.getFamilyName());
        p.setName(name);
      }
      else if(Person.Field.PROFILE_URL.toString().equals(field)) {
        //todo use a url manager to manage this
        p.setProfileUrl("/portal/private/classic/people/" + identity.getRemoteId());  
      }
      else if(Person.Field.GENDER.toString().equals(field)) {
        String gender = (String) pro.getProperty("gender");
        if (gender != null && gender.equals("female"))
          p.setGender(Person.Gender.female);
        else
          p.setGender(Person.Gender.male);
      }
      else if(ExoPersonImpl.Field.SPACES.toString().equals(field)) {
        List<org.exoplatform.social.opensocial.model.Space> spaces = new ArrayList<org.exoplatform.social.opensocial.model.Space>();
        //TODO: dang.tung: improve space to person, it will auto convert field by shindig
        PortalContainer container = PortalContainer.getInstance();
        SpaceService spaceService = (SpaceService)(container.getComponentInstanceOfType(SpaceService.class));
        try {
          List<Space> allSpaces = spaceService.getAllSpaces();
          SpaceImpl space = new SpaceImpl();
          for(Space obj : allSpaces) {
            if(spaceService.isMember(obj, identity.getRemoteId())) {
              space.setId(obj.getId());
              space.setDisplayName(obj.getName());
              spaces.add(space);
            }
          }
          ((ExoPersonImpl) p).setSpaces(spaces);
        } catch (SpaceException e) {}
      }
      else if(Person.Field.THUMBNAIL_URL.toString().equals(field)) {
        ProfileAttachment att = (ProfileAttachment) pro.getProperty("avatar");
        String avatar = null;
        if (att != null) {
          try {
            avatar = "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + att.getWorkspace()
            + att.getDataPath() + "/?rnd=" + System.currentTimeMillis();
          } catch (Exception e) {}
        }
        p.setThumbnailUrl(avatar);
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
    if(fields == null)
      return null;
    for(Map field : fields) {
      l.add(new ListFieldImpl((String)field.get("key"), (String) field.get("value")));
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
    if(fields == null)
      return null;
    for(Map field : fields) {
      l.add(new UrlImpl((String) field.get("value"), (String)field.get("key"), (String)field.get("key")));
    }
    return l;
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.AppDataService#getPersonData(java.util.Set, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);
      Map<String, Map<String, String>> idToData = new HashMap<String, Map<String, String>>();
      Iterator<Identity> it = idSet.iterator();

      String gadgetId = clean(appId);  
      String instanceId = "" + token.getModuleId();
      while(it.hasNext()) {
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
   * @param userID the user id
   * @param gadgetId the gadget id
   * @param instanceID the instance id
   * @param fields the fields
   * @return the preferences
   * @throws Exception the exception
   */
  private Map<String, String> getPreferences(String userID, String gadgetId, String instanceID, Set<String> fields) throws Exception {
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
   * @param userID the user id
   * @param gadgetId the gadget id
   * @param instanceID the instance id
   * @param values the values
   * @throws Exception the exception
   */
  private void savePreferences(String userID, String gadgetId, String instanceID, Map<String, String> values) throws Exception {
//    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
//    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);
//
//    userGadgetStorage.save(userID, gadgetId, instanceID, values);
  }

  /**
   * Delete preferences.
   * 
   * @param userID the user id
   * @param gadgetId the gadget id
   * @param instanceID the instance id
   * @param keys the keys
   * @throws Exception the exception
   */
  private void deletePreferences(String userID, String gadgetId, String instanceID, Set<String> keys) throws Exception {
//    PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
//    UserGadgetStorage userGadgetStorage = (UserGadgetStorage) pc.getComponentInstanceOfType(UserGadgetStorage.class);
//
//    userGadgetStorage.delete(userID, gadgetId, instanceID, keys);
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.AppDataService#deletePersonData(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> deletePersonData(UserId user, GroupId groupId, String appId, Set<String> fields, SecurityToken token) throws ProtocolException {
    try {
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      deletePreferences(id.getRemoteId(), gadgetId, instanceId, fields);
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }

  /* (non-Javadoc)
   * @see org.apache.shindig.social.opensocial.spi.AppDataService#updatePersonData(org.apache.shindig.social.opensocial.spi.UserId, org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String, java.util.Set, java.util.Map, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> updatePersonData(UserId user, GroupId groupId, String appId, Set<String> fields, Map<String, String> values, SecurityToken token) throws ProtocolException {
    //TODO: remove the fields that are in the fields list and not in the values map
    try {
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId);
      String gadgetId = clean(appId);
      String instanceId = "" + token.getModuleId();

      savePreferences(id.getRemoteId(), gadgetId, instanceId, values);
    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return ImmediateFuture.newInstance(null);
  }
  
  /**
   * Gets the portal name.
   * 
   * @return the portal name
   */
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  /**
   * Gets the repository.
   * 
   * @return the repository
   * @throws Exception the exception
   */
  private String getRepository() throws Exception {
    PortalContainer pContainer = PortalContainer.getInstance();
    RepositoryService rService = (RepositoryService) pContainer.getComponentInstanceOfType(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
}
