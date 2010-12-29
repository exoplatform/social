/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Gets user names by input text for auto-suggestion.
 * 
 * @author hanhvq@gmail.com
 * @since Nov 22, 2010  
 */

@Path("social/people")
public class PeopleRestService implements ResourceContainer{
  /** Confirmed Status information */
  private static final String CONFIRMED_STATUS = "confirmed";
  /** Pending Status information */
  private static final String PENDING_STATUS = "pending";
  /** Incoming Status information */
  private static final String INCOMING_STATUS = "incoming";
  /** Member of space Status information */
  private static final String SPACE_MEMBER = "member_of_space";
  /** User to invite to join the space Status information */
  private static final String USER_TO_INVITE = "user_to_invite";
  /** Number of user names is added to suggest list. */
  private static final long SUGGEST_LIMIT = 20;
  
  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  public PeopleRestService() {
  }

  @GET
  @Path("suggest.{format}")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                    @QueryParam("nameToSearch") String name,
                    @QueryParam("currentUser") String currentUser,
                    @QueryParam("typeOfRelation") String typeOfRelation,
                    @QueryParam("spaceURL") String spaceURL,
                    @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    UserNameList nameList = new UserNameList();
    ProfileFilter filter = new ProfileFilter();
    
    filter.setName(name);
    filter.setCompany("");
    filter.setGender("");
    filter.setPosition("");
    filter.setSkills("");
    List<Identity> identities = getIdentityManager()
        .getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, 0, SUGGEST_LIMIT);

    Identity currentIdentity = getIdentityManager().getIdentity(OrganizationIdentityProvider.NAME, currentUser, false);
    
    // Eliminate current name in suggesting.
    identities.remove(currentIdentity);
    Space space = getSpaceService().getSpaceByUrl(spaceURL);
    if (PENDING_STATUS.equals(typeOfRelation)) {
      addToNameList(currentIdentity, getRelationshipManager().getPending(currentIdentity, identities), nameList);
    } else if (INCOMING_STATUS.equals(typeOfRelation)) {
      addToNameList(currentIdentity, getRelationshipManager().getIncoming(currentIdentity, identities), nameList);
    } else if (CONFIRMED_STATUS.equals(typeOfRelation)){
      addToNameList(currentIdentity, getRelationshipManager().getConfirmed(currentIdentity, identities), nameList);
    } else if (SPACE_MEMBER.equals(typeOfRelation)) {  // Use in search space member
      addSpaceUserToList (identities, nameList, space, typeOfRelation);
    } else if (USER_TO_INVITE.equals(typeOfRelation)) { 
      addSpaceUserToList (identities, nameList, space, typeOfRelation);
    } else { // Identities that match the keywords.
      for (Identity identity : identities) {
        String fullName = identity.getProfile().getFullName();
        nameList.addName(fullName);
      }
    }
    
    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }
  
  private void addToNameList(Identity currentIdentity, List<Relationship> identitiesHasRelation, UserNameList nameList) {
    for (Relationship relationship : identitiesHasRelation) {
      Identity identity = relationship.getPartner(currentIdentity);
      String fullName = identity.getProfile().getFullName();
      nameList.addName(fullName);
    }
  }
  
  private void addSpaceUserToList (List<Identity> identities, UserNameList nameList,
                                   Space space, String typeOfRelation) throws SpaceException {
    SpaceService spaceSrv = getSpaceService(); 
    for (Identity identity : identities) {
      String fullName = identity.getProfile().getFullName();
      String userName = (String) identity.getProfile().getProperty(Profile.USERNAME); 
      if (SPACE_MEMBER.equals(typeOfRelation) && spaceSrv.isMember(space, userName)) {
        nameList.addName(fullName);
        continue;
      } else if (USER_TO_INVITE.equals(typeOfRelation) && !spaceSrv.isInvited(space, userName)
                 && !spaceSrv.isPending(space, userName) && !spaceSrv.isMember(space, userName)) {
        nameList.addName(userName);
      }
    }
  }
  
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  /**
   * Gets identityManager
   * @return
   */
  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      relationshipManager = (RelationshipManager) portalContainer.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
  

  @XmlRootElement
  static public class UserNameList {
    private List<String> _names;
    /**
     * Sets user name list
     * @param user name list
     */
    public void setNames(List<String> names) {
      this._names = names; 
    }
    
    /**
     * Gets user name list
     * @return user name list
     */
    public List<String> getNames() { 
      return _names; 
    }
    
    /**
     * Add name to user name list
     * @param user name
     */
    public void addName(String name) {
      if (_names == null) {
        _names = new ArrayList<String>();
      }
      _names.add(name);
    }
  }
  
}
