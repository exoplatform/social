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

import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.space.impl.SocialDataLocation;


// TODO: Auto-generated Javadoc
/**
 * The Class IdentityManager.
 */
public class IdentityManager {

  /** The identity providers. */
  private Map<String, IdentityProvider> identityProviders = new HashMap<String, IdentityProvider>();
  
  /** The storage. */
  private JCRStorage storage;

  /**
   * Instantiates a new identity manager.
   * 
   * @param dataLocation the data location
   * @param ip the indentity provider such as organization service from portal
   * @throws Exception the exception
   */
  public IdentityManager(SocialDataLocation dataLocation, IdentityProvider ip) throws Exception {
    this.storage = new JCRStorage(dataLocation);

    ip.setIdentityManager(this);
    this.addIdentityProvider(ip);
  }


  /**
   * Gets the identity by id.
   * 
   * @param id the id
   * @return the identity by id
   * @throws Exception the exception
   */
  public Identity getIdentityById(String id) throws Exception {
    return getIdentityById(id, true);
  }

  /**
   * Gets the identity by id also load his profile
   * 
   * @param id the id
   * @param loadProfile the load profile true if load and false if doesn't
   * @return null if nothing is found, or the Identity object
   * @throws Exception the exception
   */
  public Identity getIdentityById(String id, boolean loadProfile) throws Exception {
    Identity identity = null;
    
    // attempts to match a global id in the form "providerId:remoteId"
    if (GlobalId.isValid(id)) {
      GlobalId globalId = new GlobalId(id);
      String providerId = globalId.getDomain();
      String remoteId = globalId.getLocalId();
      identity = storage.getIdentityByRemoteId(providerId, remoteId);
    }

    // attempts to find a raw id
    if (identity == null) {
      identity = storage.getIdentity(id);
    }
    

    if (identity == null)
      return null;

    if(loadProfile)
      identity = identityProviders.get(identity.getProviderId()).getIdentityByRemoteId(identity);

    return identity;
  }


  /**
   * Adds the identity provider.
   * 
   * @param idProvider the id provider
   */
  public void addIdentityProvider(IdentityProvider idProvider) {
    identityProviders.put(idProvider.getName(), idProvider);
  }



  /**
   * Gets the identity by remote id.
   * 
   * @param providerId the provider id
   * @param remoteId the remote id
   * @return the identity
   * @throws Exception the exception
   */
  public Identity getIdentityByRemoteId(String providerId, String remoteId) throws Exception {
    return getIdentityByRemoteId(providerId, remoteId, true);  
  }
  
  /**
   * Gets the identities by profile filter.
   * 
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFiler profileFilter) throws Exception {
    return storage.getIdentitiesByProfileFilter(providerId, profileFilter);
  }
  
  /**
   * Gets the identities by profile filter.
   * 
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFiler profileFilter) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter);  
  }

  /**
   * Gets the identities filter by alpha bet.
   * 
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFiler profileFilter) throws Exception {
    return storage.getIdentitiesFilterByAlphaBet(providerId, profileFilter);
  }
  
  /**
   * Gets the identities filter by alpha bet.
   * 
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFiler profileFilter) throws Exception {
    return getIdentitiesFilterByAlphaBet(null, profileFilter);  
  }
  
  /**
   * This function return an Identity object that specific to
   * a special type.
   * <p>
   * For example if the type is Linked'In, the identifier will be the URL of the profile
   * or if it's a CS contact manager contact, it will be the UID of the contact.</p>
   *  A new identity is created if it does not exist.
   *  
   * @param providerId refering to the name of the Identity provider
   * @param remoteId   the identifier that identify the identity in the specific identity provider
   * @param loadProfile the load profile
   * @return null if nothing is found, or the Identity object
   * TODO improve the performance by specifying what needs to be loaded
   * @throws Exception the exception
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
      identity = identityProvider.getIdentityByRemoteId(identity);
    }
    return identity;
  }

    /**
     * create a new identity object and assign him a uniq identity ID.
     * 
     * @param providerId the provider id
     * @param remoteId the remote id
     * @return the new identity
     * @throws Exception the exception
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

  /**
   * Save identity.
   * 
   * @param identity the identity
   * @throws Exception the exception
   */
  public void saveIdentity(Identity identity) throws Exception {
    storage.saveIdentity(identity);
    identityProviders.get(identity.getProviderId()).onSaveIdentity(identity);
  }

  /**
   * Save profile.
   * 
   * @param p the profile based on some identity.
   * @throws Exception the exception
   */
  public void saveProfile(Profile p) throws Exception {
    IdentityProvider prov = identityProviders.get(p.getIdentity().getProviderId());
    prov.saveProfile(p);
  }

  /**
   * Gets the identities.
   * 
   * @param providerId the provider id
   * @return the identities
   * @throws Exception the exception
   */
  public List<Identity> getIdentities(String providerId) throws Exception {
    return getIdentities(providerId, true);
  }

  /**
   * Gets the identities.
   * 
   * @param providerId the provider id
   * @param loadProfile the load profile
   * @return the identities
   * @throws Exception the exception
   */
  public List<Identity> getIdentities(String providerId, boolean loadProfile) throws Exception {
    IdentityProvider ip = identityProviders.get(providerId);
    List<String> userids = ip.getAllUserId();
    List<Identity> ids = new ArrayList<Identity>();

    for(String userId : userids) {
      ids.add(this.getIdentityByRemoteId(providerId, userId, loadProfile)); 
    }
    return ids;
  }

  /**
   * Gets the storage.
   * 
   * @return the storage
   */
  protected JCRStorage getStorage() {
    return this.storage;
  }

}
