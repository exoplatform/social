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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/profile/UIProfileUserSearch.gtmpl",
  events = {
    @EventConfig(listeners = UIProfileUserSearch.SearchActionListener.class)
  }
)
public class UIProfileUserSearch extends UIForm {

  /**
   * SEARCH.
   */
  public static final String SEARCH = "Search";

  /**
   * USER CONTACT.
   */
  public static final String USER_CONTACT = "name";

  /**
   * REGEX FOR SPLIT STRING
   */
  public static final String REG_FOR_SPLIT = "[^_A-Za-z0-9-.\\s[\\n]]";

  /**
   * PATTERN FOR CHECK RIGHT INPUT VALUE
   */
  static final String RIGHT_INPUT_PATTERN = "^[\\p{L}][\\p{L}._\\- \\d]+$";

  /**
   * REGEX EXPRESSION OF POSITION FIELD.
   */
  public static final String POSITION_REGEX_EXPRESSION = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";

  /**
   * ADD PREFIX TO ENSURE ALWAY RIGHT THE PATTERN FOR CHECKING
   */
  static final String PREFIX_ADDED_FOR_CHECK = "PrefixAddedForCheck";

  /** Empty character. */
  public static final char EMPTY_CHARACTER = '\u0000';
  
  private static final String ASTERIK_STR = "*";
  
  private static final String PERCENTAGE_STR = "%";
  
  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /** All people filter. */
  public static final String ALL_FILTER = "All";
  
  /**
   * List used for identities storage.
   */
  private List<Identity> identityList = null;
  
  /**
   * The input conditional of search
   */
  private String rawSearchConditional;

  /**
   * Used stores filter information.
   */
  ProfileFilter profileFilter = null;

  /**
   * Used stores type of relation with current user information.
   */
  String typeOfRelation = null;

  /**
   * URL of space that this UIComponent is used in member searching.
   */
  String spaceURL = null;

  /**
   * The flag notifies a new search when clicks search icon or presses enter.
   */
  private boolean isNewSearch;

  /**
   * Number of identities.
   */
  long identitiesCount;

  /**
   * Clarifies that Spaces tab is included or not.
   */
  private boolean hasPeopleTab;
  
  /**
   * Set flag to determine this has link to connection page.
   */
  private boolean hasConnectionLink;
  
  /**
   * Number of matching people.
   */
  private int peopleNum;
  
  /**
   * Gets the flags to clarify including people tab or not. 
   * @return
   */
  public boolean isHasPeopleTab() {
    return hasPeopleTab;
  }

  /**
   * Sets the flags to clarify including people tab or not.
   * @param hasPeopleTab
   */
  public void setHasPeopleTab(boolean hasPeopleTab) {
    this.hasPeopleTab = hasPeopleTab;
  }

  /**
   * @return the hasConnectionLink
   */
  public boolean isHasConnectionLink() {
    return hasConnectionLink;
  }

  /**
   * @param hasConnectionLink the hasConnectionLink to set
   */
  public void setHasConnectionLink(boolean hasConnectionLink) {
    this.hasConnectionLink = hasConnectionLink;
  }

  /**
   * Gets number of matching people.
   * 
   * @return
   * @since 1.2.2
   */
  public int getPeopleNum() {
    return peopleNum;
  }
  
  /**
   * Sets number of matching people.
   * 
   * @param peopleNum
   * @since 1.2.2
   */
  public void setPeopleNum(int peopleNum) {
    this.peopleNum = peopleNum;
  }

/**
   * Sets list identity.
   *
   * @param identityList <code>List</code>
   * @throws Exception
   */
  public void setIdentityList(List<Identity> identityList) throws Exception {
    if (identityList.contains(Utils.getViewerIdentity())) {
      identityList.remove(Utils.getViewerIdentity());
    }
    this.identityList = identityList;
  }

  /**
   * Gets identity list result search.
   *
   * @return List of identity.
   */
  public final List<Identity> getIdentityList() {
    return identityList;
  }

  public String getRawSearchConditional() {
    return rawSearchConditional;
  }

  public void setRawSearchConditional(String rawSearchConditional) {
    this.rawSearchConditional = rawSearchConditional;
  }

  /**
   * Gets type of relation with current user.
   */
  public String getTypeOfRelation() {
    return typeOfRelation;
  }

  /**
   * Sets type of relation with current user to variable.
   *
   * @param typeOfRelation <code>char</code>
   */
  public void setTypeOfRelation(String typeOfRelation) {
    this.typeOfRelation = typeOfRelation;
  }

  /**
   * Gets space url.
   */
  public String getSpaceURL() {
    return spaceURL;
  }

  /**
   * Sets space url.
   *
   * @param spaceURL <code>char</code>
   */
  public void setSpaceURL(String spaceURL) {
    this.spaceURL = spaceURL;
  }

  /**
   * Gets current user's name.
   *
   * @return
   */
  protected String getCurrentUserName() {
    return RequestContext.getCurrentInstance().getRemoteUser();
  }

  /**
   * Gets current Rest context name of portal container.
   */
  protected String getRestContextName() {
    return PortalContainer.getCurrentRestContextName();
  }

  /**
   * Gets filter object.
   *
   * @return The object that contain filter information.
   */
  public final ProfileFilter getProfileFilter() {
    return profileFilter;
  }

  /**
   * Sets filter object.
   *
   * @param profileFilter <code>Object<code>
   */
  public final void setProfileFilter(final ProfileFilter profileFilter) {
    this.profileFilter = profileFilter;
  }

  /**
   * Initializes user search form fields. Initials and adds components as children to search form.
   *
   * @throws Exception
   */
  public UIProfileUserSearch() throws Exception {
    ResourceBundle resourceBudle = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();

    String defaultName = resourceBudle.getString("UIProfileUserSearch.label.Name");
    String defaultPos = resourceBudle.getString("UIProfileUserSearch.label.Position");
    String defaultSkills = resourceBudle.getString("UIProfileUserSearch.label.Skills");

    UIFormStringInput search = new UIFormStringInput(SEARCH, USER_CONTACT, defaultName);
    search.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, defaultName);
    addUIFormInput(search);
    UIFormStringInput position = new UIFormStringInput(Profile.POSITION, Profile.POSITION, defaultPos);
    position.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, defaultPos);
    addUIFormInput(position);
    UIFormStringInput skills = new UIFormStringInput(Profile.EXPERIENCES_SKILLS, Profile.EXPERIENCES_SKILLS, defaultSkills);
    skills.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, defaultSkills);
    addUIFormInput(skills);
    profileFilter = new ProfileFilter();
    setHasPeopleTab(false);
    setSubmitAction("return false;");
  }

  protected void resetUIComponentValues() {
    ResourceBundle resourceBudle = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();

    if(profileFilter != null && profileFilter.getName() !=null && profileFilter.getName().equals("")){
      UIFormStringInput uiName = getChildById(SEARCH);
      String defaultName = resourceBudle.getString("UIProfileUserSearch.label.Name");
      uiName.setValue(defaultName);
    }
    if(profileFilter != null && profileFilter.getPosition() !=null && profileFilter.getPosition().equals("")){
      UIFormStringInput uiPos = getChildById(Profile.POSITION);
      String defaultPos = resourceBudle.getString("UIProfileUserSearch.label.Position");
      uiPos.setValue(defaultPos);
    }
    
    if(profileFilter != null && profileFilter.getSkills() !=null && profileFilter.getSkills().equals("")){
      UIFormStringInput uiSkills = getChildById(Profile.EXPERIENCES_SKILLS);
      String defaultSkills = resourceBudle.getString("UIProfileUserSearch.label.Skills");
      uiSkills.setValue(defaultSkills);
    }
  }
  
  /**
   * Returns the current selected node.<br>
   *
   * @return selected node.
   * @since 1.2.2
   */
  public String getSelectedNode() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String currentPath = pcontext.getControllerContext().getParameter(QualifiedName.parse("gtn:path")).trim();
    if (currentPath.split("/").length >= 2) {
      return  currentPath.split("/")[1];
    }
    return currentPath;
  }

  /**
   * Listens to search event from search form, then processes search condition and set search result
   * to the result variable.<br> - Gets user name and other filter information from request.<br> -
   * Searches user that matches the condition.<br> - Sets matched users into result list.<br>
   */
  public static class SearchActionListener extends EventListener<UIProfileUserSearch> {
    @Override
    public final void execute(final Event<UIProfileUserSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIProfileUserSearch uiSearch = event.getSource();
      ProfileFilter filter = new ProfileFilter();
      List<Identity> excludedIdentityList = new ArrayList<Identity>();
      excludedIdentityList.add(Utils.getViewerIdentity());
      filter.setExcludedIdentityList(excludedIdentityList);
      
      uiSearch.invokeSetBindingBean(filter);
      normalizeInputValues(filter);
      ResourceBundle resApp = ctx.getApplicationResourceBundle();

      String defaultNameVal = resApp.getString(uiSearch.getId() + ".label.Name");
      String defaultPosVal = resApp.getString(uiSearch.getId() + ".label.Position");
      String defaultSkillsVal = resApp.getString(uiSearch.getId() + ".label.Skills");
      try {
          StringBuffer rawSearchMessageStringBuffer = new StringBuffer();
          String name = filter.getName();
          String pos = filter.getPosition();
          String skills = filter.getSkills();
          
          if ((name == null) || name.equals(defaultNameVal) || ASTERIK_STR.equals(name) || PERCENTAGE_STR.equals(name)) {
            filter.setName("");
          } else {
            rawSearchMessageStringBuffer.append(defaultNameVal + ":" + name);
          }
          
          if ((pos == null) || pos.equals(defaultPosVal) || ASTERIK_STR.equals(pos) || PERCENTAGE_STR.equals(pos)) {
            filter.setPosition("");
          } else {
            rawSearchMessageStringBuffer
              .append((rawSearchMessageStringBuffer.length() > 0 ? " " : "") + defaultPosVal + ":" + pos);
          }
          if ((skills == null) || skills.equals(defaultSkillsVal) || ASTERIK_STR.equals(skills) || PERCENTAGE_STR.equals(skills)) {
            filter.setSkills("");
          } else {
            rawSearchMessageStringBuffer
              .append((rawSearchMessageStringBuffer.length() > 0 ? " " : "") + defaultSkillsVal + ":" + skills);
          }
          
          if(rawSearchMessageStringBuffer.length() > 0){
            uiSearch.setRawSearchConditional(rawSearchMessageStringBuffer.toString());
            // Eliminates space characters.
            if(!isValidInput(filter)){
              filter.setName("@");
              filter.setCompany("");
              filter.setSkills("");
              filter.setPosition("");
            }
          } else {
            uiSearch.setRawSearchConditional(ALL_FILTER);
            filter.setFirstCharacterOfName(EMPTY_CHARACTER);
            filter.setName("");
            filter.setCompany("");
            filter.setSkills("");
            filter.setPosition("");
          }
          
        uiSearch.setProfileFilter(filter);
        uiSearch.setNewSearch(true);
        
      } catch (Exception e) {
        uiSearch.setIdentityList(new ArrayList<Identity>());
      }
      Event<UIComponent> searchEvent = uiSearch.<UIComponent>getParent()
              .createEvent(SEARCH, Event.Phase.PROCESS, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }
    
    private void normalizeInputValues(ProfileFilter filter) {
      String name = filter.getName();
      String pos = filter.getPosition();
      String skills = filter.getSkills();
      
      if (name != null && name.length() > 0) {
        filter.setName(Utils.normalizeString(name));
      }
      
      if (pos != null && pos.length() > 0) {
        filter.setPosition(Utils.normalizeString(pos));
      }
      
      if (skills != null && skills.length() > 0) {
        filter.setSkills(Utils.normalizeString(skills));
      }
    }


    /**
     * Checks input values follow regular expression.
     *
     * @param input <code>Object</code>
     * @return true if the input is properly to regular expression else return false.
     */
    private boolean isValidInput(final ProfileFilter input) {
      // Check contact name
      String contactName = input.getName();
      String position = input.getPosition();
      String skills = input.getSkills();
      String company = input.getCompany();
      
      // Eliminate '*' and '%' character in string for checking
      String contactNameForCheck = null;
      if (contactName != null) {
        contactNameForCheck = contactName.replaceAll("[/*%]", "").trim();
        // Make sure string for checking is started by alphabet character
        contactNameForCheck = PREFIX_ADDED_FOR_CHECK + contactNameForCheck;
        if (!contactNameForCheck.matches(RIGHT_INPUT_PATTERN)) {
          return false;
        }
      }
      // Eliminate '*' and '%' character in string for checking
      String positionForCheck = null;
      if (contactName != null) {
        positionForCheck = position.replaceAll("[/*%]", "").trim();
        // Make sure string for checking is started by alphabet character
        positionForCheck = PREFIX_ADDED_FOR_CHECK + positionForCheck;
        if (!positionForCheck.matches(RIGHT_INPUT_PATTERN)) {
          return false;
        }
      }

      // Eliminate '*' and '%' character in string for checking
      String skillsForCheck = null;
      if (contactName != null) {
        skillsForCheck = skills.replaceAll("[/*%]", "").trim();
        // Make sure string for checking is started by alphabet character
        skillsForCheck = PREFIX_ADDED_FOR_CHECK + skillsForCheck;
        if (!skillsForCheck.matches(RIGHT_INPUT_PATTERN)) {
          return false;
        }
      }
      
      // Eliminate '*' and '%' character in string for checking
      String companyForCheck = null;
      if (contactName != null) {
        companyForCheck = company.replaceAll("[/*%]", "").trim();
        // Make sure string for checking is started by alphabet character
        companyForCheck = PREFIX_ADDED_FOR_CHECK + companyForCheck;
        if (!companyForCheck.matches(RIGHT_INPUT_PATTERN)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Filter identity follow skills information.
   *
   * @param skills     <code>String</code>
   * @param identities <code>Object</code>
   * @return List of identities that has skills information match.
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getIdentitiesBySkills(final List<Identity> identities) {
    List<Identity> identityLst = new ArrayList<Identity>();
    String prof = null;
    ArrayList<HashMap<String, Object>> experiences;
    String skill = getProfileFilter().getSkills().trim().toLowerCase();

    if (identities.size() == 0) {
      return identityLst;
    }

    for (Identity id : identities) {
      Profile profile = id.getProfile();
      experiences = (ArrayList<HashMap<String, Object>>) profile.getProperty(Profile.EXPERIENCES);
      if (experiences == null) {
        continue;
      }
      for (HashMap<String, Object> exp : experiences) {
        prof = (String) exp.get(Profile.EXPERIENCES_SKILLS);
        if (prof == null) {
          continue;
        }
        Pattern p = Pattern.compile(REG_FOR_SPLIT);
        String[] items = p.split(prof);
        for (String item : items) {
          if (item.toLowerCase().matches(skill)) {
            identityLst.add(id);
            break;
          }
        }
      }
    }

    return GetUniqueIdentities(identityLst);
  }

  /**
   * Unions to collection to make one collection.
   *
   * @param identities1 <code>Object</code>
   * @param identities2 <code>Object</code>
   * @return One new collection that the union result of two input collection.
   */
  private static Collection<Identity> Union(final Collection<Identity> identities1,
                                            final Collection<Identity> identities2) {
    Set<Identity> identities = new HashSet<Identity>(identities1);
    identities.addAll(new HashSet<Identity>(identities2));
    return new ArrayList<Identity>(identities);
  }

  /**
   * Gets unique identities from one collection of identities.
   *
   * @param identities <code>Object</code>
   * @return one list that contains unique identities.
   */
  private static ArrayList<Identity> GetUniqueIdentities(final Collection<Identity> identities) {
    return (ArrayList<Identity>) Union(identities, identities);
  }

  public final boolean isNewSearch() {
    return isNewSearch;
  }

  public final void setNewSearch(final boolean isNewSearch) {
    this.isNewSearch = isNewSearch;
  }
}
