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
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.ProfileFiler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.RelationshipManager;

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

  /** IdentityManager */
  IdentityManager     im           = null;
  /** RelationshipManager */
  RelationshipManager rm           = null;
  /** Current identity. */
  Identity            currIdentity = null;
  private List<Identity> identityList = null;
  boolean hasAdvanceSearch = false;
 
  /**
   * Constructor to initialize form fields
   * @throws Exception
   */
  public UIProfileUserSearch() throws Exception {
  }
  
  /**
   * identityList setter
   * @param identityList
   */
  public void setIdentityList(List<Identity> identityList) {
    this.identityList = identityList;
  }
  
  /**
   * identityList getter
   * @return
   * @throws Exception 
   */
  public List<Identity> getidentityList(boolean includeCurrentIdentity) throws Exception {
    List<Identity> identities = new ArrayList<Identity>();
    List<Identity> identityLst = this.identityList;
    if ((!includeCurrentIdentity) && (identityLst != null)) {
        for (Identity id : identityLst) {
          if (!id.getRemoteId().equals(getCurrentIdentity().getRemoteId())) identities.add(id); 
        }
      return identities; 
    }
  
    return identityList;
  }

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
      List<Identity> identities = new ArrayList<Identity>();
      IdentityManager idm = uiSearch.getIdentityManager();
      String userContact = event.getRequestContext().getRequestParameter("userContact");
//      String position = event.getRequestContext().getRequestParameter("position");
//      String company = event.getRequestContext().getRequestParameter("company");
//      String gender = event.getRequestContext().getRequestParameter("gender");
      
      ProfileFiler filter = new ProfileFiler();
      
      if ((userContact != null) && (userContact.length() != 0)) {
        filter.setUserName(userContact);
        filter.setHasFilter(true);
      }
//      if ((position != null) && (position.length() != 0)) {
//        filter.setPosition(position);
//        filter.setHasFilter(true);
//      }
//      if ((company != null) && (company.length() != 0)) {
//        filter.setCompany(company);
//        filter.setHasFilter(true);
//      }
//      if ((gender != null) && (gender.length() != 0)) {
//        filter.setGender(gender);
//        filter.setHasFilter(true);
//      }
      
      if (filter.isHasFilter()) {
        identities = idm.getIdentitiesByProfileFilter(filter);
      } else { 
        identities = null;
      }
      
      uiSearch.setIdentityList(identities);
      Event<UIComponent> searchEvent = uiSearch.<UIComponent>getParent().createEvent("Search", Event.Phase.DECODE, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }
    
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
    if (currIdentity == null) {
      IdentityManager im = getIdentityManager();
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
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
  
}
