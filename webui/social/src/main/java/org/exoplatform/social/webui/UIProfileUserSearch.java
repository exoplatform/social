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
package org.exoplatform.social.webui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.ProfileFiler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * UIProfileUserSearch for search users in profile.
 * The search event should broadcast for the parent one to catch and
 * get searched Identity List from UIProfileUserSearch
 * Author : hanhvi
 *          hanhvq@gmail.com
 * Sep 25, 2009  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/social/webui/component/UIProfileUserSearch.gtmpl",
  events = {
    @EventConfig(listeners = UIProfileUserSearch.SearchActionListener.class) 
  }
)
public class UIProfileUserSearch extends UIForm {
  /** USER CONTACT. */
  final public static String USER_CONTACT = "name";
  /** PROFESSIONAL. */
  final public static String PROFESSIONAL = "professional";
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
  /** DEFAULT GENDER. */
  final public static String GENDER_DEFAULT = "Gender";
  /** MALE. */
  final public static String MALE = "male";
  /** FEMALE. */
  final public static String FEMALE = "female";
  /**REGEX FOR SPLIT STRING */
  final public static String REG_FOR_SPLIT = "[^_A-Za-z0-9-.\\s[\\n]]";
  /** PATTERN FOR CHECK RIGHT INPUT VALUE */
  final static String RIGHT_INPUT_PATTERN = "^[\\p{L}][\\p{L}._\\- \\d]+$";
  /** REGEX EXPRESSION OF POSITION FIELD. */
  final public static String POSITION_REGEX_EXPRESSION = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";
  /** ADD PREFIX TO ENSURE ALWAY RIGHT THE PATTERN FOR CHECKING */
  final static String PREFIX_ADDED_FOR_CHECK = "PrefixAddedForCheck";
  
  /** IdentityManager */
  IdentityManager        im           = null;
  /** RelationshipManager */
  RelationshipManager    rm           = null;
  /** Current identity. */
  Identity               currIdentity = null;
  private List<Identity> identityList = null;
  /** Selected character when search by alphabet */
  String selectedChar = null;
  ProfileFiler profileFiler = null;
  List<String> allUserContactName = null;
  
  /**
   * Constructor to initialize form fields
   * @throws Exception
   */
  public UIProfileUserSearch() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(GENDER_DEFAULT));
    options.add(new SelectItemOption<String>(MALE));
    options.add(new SelectItemOption<String>(FEMALE));
    
    addUIFormInput(new UIFormStringInput(SEARCH, USER_CONTACT, USER_CONTACT));
    addUIFormInput(new UIFormStringInput(POSITION, POSITION, POSITION));
    addUIFormInput(new UIFormStringInput(PROFESSIONAL, PROFESSIONAL, PROFESSIONAL));
    addUIFormInput(new UIFormSelectBox(GENDER, GENDER, options));
  }
  
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
  public List<Identity> getIdentityList() throws Exception { return identityList;}

  
  public String getSelectedChar() { return selectedChar;}

  public void setSelectedChar(String selectedChar) { this.selectedChar = selectedChar;}

  public ProfileFiler getProfileFiler() { return profileFiler;} 
  
  public void setProfileFiler(ProfileFiler profileFiler) { this.profileFiler = profileFiler;}

  /**
   * Get all users for searching suggestion.
   * 
   * @return all contact name of each relation
   * @throws Exception 
   */
  public List<String> getAllContactName() throws Exception {
    return allUserContactName;
  }

  /**
   * Set all user contact name for auto-suggestion from parent which add this component.
   * 
   * @param allUserContactName
   */
  public void setAllUserContactName(List<String> allUserContactName) {
    this.allUserContactName = allUserContactName;
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
      String charSearch = ctx.getRequestParameter(OBJECTID);
      List<Identity> identitiesSearchResult = new ArrayList<Identity>();
      List<Identity> identities = new ArrayList<Identity>();
      IdentityManager idm = uiSearch.getIdentityManager();
      ProfileFiler filter = new ProfileFiler();
      uiSearch.invokeSetBindingBean(filter);
      ResourceBundle resApp = ctx.getApplicationResourceBundle();
      
      String defaultNameVal = resApp.getString(uiSearch.getId() + ".label.Name");
      String defaultPosVal = resApp.getString(uiSearch.getId() + ".label.Position");
      String defaultProfVal = resApp.getString(uiSearch.getId() + ".label.Professional");
      String defaultGenderVal = resApp.getString(uiSearch.getId() + ".label.AllGender");
      
      if (!isValidInput(filter)) {
        uiSearch.setIdentityList(new ArrayList<Identity>());
      } else {
      
        if ((filter.getName() == null) || filter.getName().equals(defaultNameVal)) {
          filter.setName("");
        }
        if ((filter.getPosition() == null) || filter.getPosition().equals(defaultPosVal)) {
          filter.setPosition("");
        }
        if ((filter.getProfessional() == null) || filter.getProfessional().equals(defaultProfVal)) {
          filter.setProfessional("");
        }
        if (filter.getGender().equals(defaultGenderVal)) {
          filter.setGender("");
        }
        
        String professional = null;
        
        uiSearch.setSelectedChar(charSearch);
        try {
          if (charSearch == null) {
            identitiesSearchResult = idm.getIdentitiesByProfileFilter(filter);
            uiSearch.setIdentityList(identitiesSearchResult);
            
            // Using regular expression for search
            professional = filter.getProfessional();
            if (professional.length() > 0) {
              professional = ((professional == "") || (professional.length() == 0)) ? "*" : professional;
              professional = (professional.charAt(0)!='*') ? "*" + professional : professional;
              professional = (professional.charAt(professional.length()-1)!='*') ? professional += "*" : professional;
              professional = (professional.indexOf("*") >= 0) ? professional.replace("*", ".*") : professional;
              professional = (professional.indexOf("%") >= 0) ? professional.replace("%", ".*") : professional;
              Pattern.compile(professional);
              identities = uiSearch.getIdentitiesByProfessional(professional, identitiesSearchResult);
              uiSearch.setIdentityList(identities);
            }
            
          } else {
            ((UIFormStringInput)uiSearch.getChildById(SEARCH)).setValue(USER_CONTACT);
            filter.setName(charSearch);
            filter.setPosition("");
            filter.setGender("");
            if ("All".equals(charSearch)) {
              filter.setName("");
            }
            
            identitiesSearchResult = idm.getIdentitiesFilterByAlphaBet(filter);
            uiSearch.setIdentityList(identitiesSearchResult);
          }
        } catch (Exception e) {
          uiSearch.setIdentityList(new ArrayList<Identity>());
        }
      }
      
      Event<UIComponent> searchEvent = uiSearch.<UIComponent>getParent().createEvent(SEARCH, Event.Phase.DECODE, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }

    /**
     * Check input values is right or not follow regular expression.
     * @param input
     * @return
     */
    private boolean isValidInput(ProfileFiler input) {
      //Check contact name
      String contactName = input.getName();
      // Eliminate '*' and '%' character in string for checking
      String contactNameForCheck = null;
      if (contactName != null) {
        contactNameForCheck = contactName.trim().replace("*", "");
        contactNameForCheck = contactNameForCheck.replace("%", "");
        // Make sure string for checking is started by alphabet character
        contactNameForCheck =  PREFIX_ADDED_FOR_CHECK + contactNameForCheck;
        if (!contactNameForCheck.matches(RIGHT_INPUT_PATTERN)) return false;
      }
      
      // Check position
      String position = input.getPosition();
      // Eliminate '*' and '%' character in string for checking
      String positionForCheck = null;
      if (position != null) { 
        positionForCheck = position.trim().replace("*", "");
        positionForCheck = positionForCheck.replace("%", "");
        // Make sure string for checking is started by alphabet character
        positionForCheck =  PREFIX_ADDED_FOR_CHECK + positionForCheck;
        if (!positionForCheck.matches(POSITION_REGEX_EXPRESSION)) return false;
      }
      
      return true;
    }
  }
  
  /**
   * Filter identity follow professional information.
   * 
   * @param professional
   * @param identities
   * @return List of identities that has professional information match.
   */
  @SuppressWarnings("unchecked")
  private List<Identity> getIdentitiesByProfessional(String professional, List<Identity> identities) {
      List<Identity> identityLst = new ArrayList<Identity>();
      String prof = null;
      ArrayList<HashMap<String, Object>> experiences = new ArrayList<HashMap<String, Object>>();
      String profes = professional.trim().toLowerCase();
      
      if (identities.size() == 0) return identityLst;
    
      for (Identity id : identities) {
        Profile profile = id.getProfile();
        experiences = (ArrayList<HashMap<String, Object>>) profile.getProperty(EXPERIENCE);
        if (experiences == null) continue;
        for (HashMap<String, Object> exp : experiences) {
          prof = (String) exp.get(PROFESSIONAL);
          if (prof == null) continue;
          Pattern p = Pattern.compile(REG_FOR_SPLIT);
          String[] items = p.split(prof);
          for(String item : items) {
              if (item.toLowerCase().matches(profes)) { 
                identityLst.add(id);
                break;
              }
          }
        }
      }
      
      return GetUniqueIdentities(identityLst);
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
    Identity identity = null;
    identity = im.getIdentityByRemoteId(ORGANIZATION, getCurrentViewerUserName());
    if (identity == null) {
      return im.getIdentityByRemoteId(ORGANIZATION, getCurrentUserName());
    }
      
    return identity;
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
  
  private static Collection<Identity> Union(Collection<Identity> identities1, Collection<Identity> identities2)
  {
      Set<Identity> identities = new HashSet<Identity>(identities1);
      identities.addAll(new HashSet<Identity>(identities2));
      return new ArrayList<Identity>(identities);
  }
  
  private static ArrayList<Identity> GetUniqueIdentities(Collection<Identity> identities)
  {
      return (ArrayList<Identity>)Union(identities, identities);
  }
}
