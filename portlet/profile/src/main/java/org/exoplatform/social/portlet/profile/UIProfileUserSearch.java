/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.portlet.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.ProfileFiler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.portlet.URLUtils;

/**
 * UIProfileUserSearch for search users in profile.
 * The search event should broadcast for the parent one to catch and
 * get searched Identity List from UIProfileUserSearch
 * Author : hanhvi
 *          hanhvq@gmail.com
 * Sep 25, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIProfileUserSearch.gtmpl",
  events = {
    @EventConfig(listeners = UIProfileUserSearch.SearchActionListener.class) 
  }
)
public class UIProfileUserSearch extends UIComponent {
  /** USER CONTACT. */
  final public static String USER_CONTACT = "userContact";
  /** COMPANY. */
  final public static String COMPANY = "company";
  /** POSITION. */
  final public static String POSITION = "position";
  /** GENDER. */
  final public static String GENDER = "gender";
  /** SEARCH. */
  final public static String SEARCH = "Search";
  /** EXPERIENCE. */
  final public static String EXPERIENCE = "experiences";
  /** ORGANIZATION. */
  final public static String ORGANIZATION = "organization";
  /** IdentityManager */
  IdentityManager        im           = null;
  /** RelationshipManager */
  RelationshipManager    rm           = null;
  /** Current identity. */
  Identity               currIdentity = null;
  private List<Identity> identityList = null;
  boolean                hasAdvanceSearch = false;
  /** Selected character when search by alphabet */
  String selectedChar = null;
  
  /**
   * Constructor to initialize form fields
   * @throws Exception
   */
  public UIProfileUserSearch() throws Exception { }
  
  /**
   * identityList setter
   * @param identityList
   */
  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList;}
  
  /**
   * identityList getter
   * @return
   * @throws Exception 
   */
  public List<Identity> getidentityList() throws Exception { return identityList;}

  
  public String getSelectedChar() { return selectedChar;}

  public void setSelectedChar(String selectedChar) { this.selectedChar = selectedChar;}


  /**
   * SearchActionListener
   * Get the keyword and filter from the form.
   * Search identity and set identityList
   */
  static public class SearchActionListener extends EventListener<UIProfileUserSearch> {
    @Override
    public void execute(Event<UIProfileUserSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIProfileUserSearch uiSearch = event.getSource();
      List<Identity> identitiesSearchResult = new ArrayList<Identity>();
      List<Identity> identities = new ArrayList<Identity>();
      IdentityManager idm = uiSearch.getIdentityManager();
      String userContact = event.getRequestContext().getRequestParameter(USER_CONTACT);
      String position = event.getRequestContext().getRequestParameter(POSITION);
      String company = event.getRequestContext().getRequestParameter(COMPANY);
      String gender = event.getRequestContext().getRequestParameter(GENDER);
      String charSearch = event.getRequestContext().getRequestParameter("charSearch");
      
      Boolean isSearchAlphaBet = Boolean.parseBoolean(event.getRequestContext().getRequestParameter("isSearchAlphaBet"));
      if (isSearchAlphaBet)  {
        userContact = charSearch;
        uiSearch.setSelectedChar(charSearch);
      }
      
      ProfileFiler filter = new ProfileFiler();
      
      filter.setUserName(userContact);
      filter.setPosition(position);
      filter.setGender(gender);
      
      if (!isSearchAlphaBet) {
        identitiesSearchResult = idm.getIdentitiesByProfileFilter(filter);
        
        if (identitiesSearchResult != null) {
            for (Identity id : identitiesSearchResult) {
              if (!id.getRemoteId().equals(uiSearch.getCurrentViewerIdentity().getRemoteId())) identities.add(id); 
            }
        }
        
        if (company.length() > 0) {
          identities = uiSearch.getIdentitiesByCompany(company, identities);
        }
        
        uiSearch.setIdentityList(identities);
      } else {
        identitiesSearchResult = idm.getIdentitiesFilterByAlphaBet(filter);
        
        if (identitiesSearchResult != null) {
            for (Identity id : identitiesSearchResult) {
              if (!id.getRemoteId().equals(uiSearch.getCurrentViewerIdentity().getRemoteId())) identities.add(id); 
            }
        }
        
        uiSearch.setIdentityList(identities);
      }
      
      Event<UIComponent> searchEvent = uiSearch.<UIComponent>getParent().createEvent(SEARCH, Event.Phase.DECODE, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }
    
  }
  
  /**
   * Filter identity follow company information.
   * 
   * @param company
   * @param identities
   * @return List of identities that has company information match.
   */
  @SuppressWarnings("unchecked")
  private List<Identity> getIdentitiesByCompany(String company, List<Identity> identities) {
      List<Identity> identityLst = new ArrayList<Identity>();
      String comp = null;
      ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
      
      if (identities.size() > 0) {
        for (Identity id : identities) {
          Profile profile = id.getProfile();
          experiences = (ArrayList<HashMap<String, Object>>) profile.getProperty(EXPERIENCE);
          if (experiences != null) {
            for (HashMap<String, Object> exp : experiences) {
              comp = (String) exp.get(COMPANY);
              if (comp != null) {
                if (comp.contains(company)) {
                  identityLst.add(id);
                }
              }
            }
          }
        }
      }
      
      return identityLst;
  }
  
  /**
   * Get identity manager.
   * 
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im;
  }
  
  /**
   * Get current identity.
   * 
   * @return
   * @throws Exception
   */
  public Identity getCurrentIdentity() throws Exception {
      IdentityManager im = getIdentityManager();
      return im.getIdentityByRemoteId(ORGANIZATION, getCurrentUserName());
  }
  
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    return im.getIdentityByRemoteId(ORGANIZATION, getCurrentViewerUserName());
  }
  /**
   * Get current user name.
   * 
   * @return
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }
  
  private String getCurrentViewerUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;
    
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    return portalRequest.getRemoteUser();
  }
  
}
