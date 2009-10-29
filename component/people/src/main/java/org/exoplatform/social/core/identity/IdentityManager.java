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
package org.exoplatform.social.core.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;


public class IdentityManager {

  private Map<String, IdentityProvider> identityProviders = new HashMap<String, IdentityProvider>();
  private JCRStorage storage;

  public IdentityManager(NodeHierarchyCreator nodeHierarchyCreator, IdentityProvider ip) throws Exception {
    this.storage = new JCRStorage(nodeHierarchyCreator);

    ip.setIdentityManager(this);
    this.addIdentityProvider(ip);
  }


  public Identity getIdentityById(String id) throws Exception {
    return getIdentityById(id, true);
  }

  /**
   * @param Id the id of the identity
   * @return null if nothing is found, or the Identity object
   */
  public Identity getIdentityById(String id, boolean loadProfile) throws Exception {
    Identity identity = storage.getIdentity(id);
    if (identity == null)
      return null;

    if(loadProfile)
      identity = identityProviders.get(identity.getProviderId()).getIdentityByRemoteId(identity);

    return identity;
  }


  public void addIdentityProvider(IdentityProvider idProvider) {
    identityProviders.put(idProvider.getName(), idProvider);
  }



  public Identity getIdentityByRemoteId(String providerId, String remoteId) throws Exception {
    return getIdentityByRemoteId(providerId, remoteId, true);  
  }
  
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFiler profileFilter) throws Exception {
    return storage.getIdentitiesByProfileFilter(providerId, profileFilter);
  }
  
  public List<Identity> getIdentitiesByProfileFilter(ProfileFiler profileFilter) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter);  
  }

  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFiler profileFilter) throws Exception {
    return storage.getIdentitiesFilterByAlphaBet(providerId, profileFilter);
  }
  
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFiler profileFilter) throws Exception {
    return getIdentitiesFilterByAlphaBet(null, profileFilter);  
  }
  
  /**
   * This function return an Identity object that specific to
   * a special type.
   * For example if the type is Linked'In, the identifier will be the URL of the profile
   * or if it's a CS contact manager contact, it will be the UID of the contact
   *
   * @param providerId refering to the name of the Identity provider
   * @param remoteId   the identifier that identify the identity in the specific identity provider
   * @return null if nothing is found, or the Identity object
   * TODO improve the performance by specifying what needs to be loaded
   */
  public Identity getIdentityByRemoteId(String providerId, String remoteId, boolean loadProfile) throws Exception {
    //System.out.println("getting the identity for " + providerId + " and remoteid:" + remoteId);
    Identity identity = storage.getIdentityByRemoteId(providerId, remoteId);
    if (identity == null) {
      //System.out.println("create the identity for " + providerId + " and remoteid:" + remoteId);
      identity = getNewIdentity(providerId, remoteId);
    }
    //System.out.println("identityProviders = " + identityProviders);
    if(loadProfile) {
      IdentityProvider identityProvider = identityProviders.get(identity.getProviderId());
      System.out.println("\n\n\n\n: identity: " + identityProvider.getIdentityByRemoteId(identity));
      identity = identityProviders.get(identity.getProviderId()).getIdentityByRemoteId(identity);
    }
    return identity;
  }

    /**
     * create a new identity object and assign him a uniq identity ID
     * @param providerId
     * @param remoteId
     * @return
     */
  public Identity getNewIdentity(String providerId, String remoteId) throws Exception {
    Identity identity = new Identity();
    identity.setProviderId(providerId);
    identity.setRemoteId(remoteId);
    //TODO  before saving, we should check if the identity exist on the provider
    saveIdentity(identity);
    return identity;
  }

  public void saveIdentity(Identity identity) throws Exception {
    storage.saveIdentity(identity);
    identityProviders.get(identity.getProviderId()).onSaveIdentity(identity);
  }

  public void saveProfile(Profile p) throws Exception {
    IdentityProvider prov = identityProviders.get(p.getIdentity().getProviderId());
    prov.saveProfile(p);
  }

  public List<Identity> getIdentities(String providerId) throws Exception {
    return getIdentities(providerId, true);
  }

  public List<Identity> getIdentities(String providerId, boolean loadProfile) throws Exception {
    IdentityProvider ip = identityProviders.get(providerId);
    List<String> userids = ip.getAllUserId();
    List<Identity> ids = new ArrayList<Identity>();

    for(String userId : userids) {
      ids.add(this.getIdentityByRemoteId(providerId, userId, loadProfile)); 
    }
    return ids;
  }

  protected JCRStorage getStorage() {
    return this.storage;
  }

}
