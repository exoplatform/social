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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;

import com.google.common.collect.Sets;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Oct 13, 2008
 * Time: 7:58:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExoService {
        /**
     * Get the set of user id's from a user and group
     */
    protected Set<Identity> getIdSet(UserId user, GroupId group, SecurityToken token) throws Exception {
      String userId = user.getUserId(token);

      Identity id = getIdentity(userId);
      Set<Identity> returnVal = Sets.newLinkedHashSet();

      if (group == null) {
        returnVal.add(id);
      }
      else {
        switch (group.getType()) {
        case all:
        case friends:
        case groupId:
          returnVal.addAll(getFriendsList(id));
          break;
        case self:
          returnVal.add(id);
          break;
        }
      }
      return returnVal;
    }

    protected List<Identity> getFriendsList(Identity id) throws Exception {
      PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
      RelationshipManager rm = (RelationshipManager) pc.getComponentInstanceOfType(RelationshipManager.class);
      List<Relationship> rels = rm.getContacts(id);

      List<Identity> ids = new ArrayList<Identity>();

      for(Relationship rel : rels) {
        if (rel.getIdentity1().getId().equals(id.getId()))
          ids.add(rel.getIdentity2());
        else
          ids.add(rel.getIdentity1());
      }

      return ids;

    }

    /**
     * Get the set of user id's for a set of users and a group
     */
    protected Set<Identity> getIdSet(Set<UserId> users, GroupId group, SecurityToken token)
            throws Exception {
      Set<Identity> ids = Sets.newLinkedHashSet();
      for (UserId user : users) {
        ids.addAll(getIdSet(user, group, token));
      }
      return ids;
    }

    protected Identity getIdentity(String id) throws Exception {
      PortalContainer pc = RootContainer.getInstance().getPortalContainer("portal");
      IdentityManager im = (IdentityManager) pc.getComponentInstanceOfType(IdentityManager.class);

      //Identity identity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME, id);
      Identity identity = im.getIdentityById(id);
//      if (identity == null) {
//        identity = im.getIdentityById(id);
//      }

      if(identity == null) {
          throw  new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "this user does not exist");
      }
      return identity;
    }

}
