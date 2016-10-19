/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.connections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({
  @ComponentConfig(
    template =  "war:/groovy/social/webui/connections/UIMyConnections.gtmpl",
    events = {
      @EventConfig(listeners = UIMyConnections.RemoveConnectionActionListener.class),
      @EventConfig(listeners = UIMyConnections.SearchActionListener.class),
      @EventConfig(listeners = UIMyConnections.LoadMorePeopleActionListener.class)
    }
  )
})
public class UIMyConnections extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIMyConnections.class);
  
  /** Label for display relation is deleted information */
  private static final String RELATION_DELETED_INFO = "UIMyConnections.label.DeletedInfo";

  /** Confirmed Status information */
  private static final String CONFIRMED_STATUS = "confirmed";
  
  /** Stores UIProfileUserSearch instance. */
  UIProfileUserSearch uiProfileUserSearch = null;

  /**
   * Default the number of relationships per page.
   * 
   * @since 1.2.0-Beta3
   */
  private static final int MY_CONNECTION_PER_PAGE = 45;
  
  private boolean loadAtEnd = false;
  private int currentLoadIndex;
  private boolean enableLoadNext;
  private int loadingCapacity;
  private List<Identity> peopleList;
  private ListAccess<Identity> peopleListAccess;
  private int peopleNum;
  private boolean hasPeopleTab;
  String selectedChar = null;
  private Identity lastOwner = null;
  
  public boolean isHasPeopleTab() {
    return hasPeopleTab;
  }

  public void setHasPeopleTab(boolean hasPeopleTab) {
    this.hasPeopleTab = hasPeopleTab;
  }

  /**
   * Gets selected character when search by alphabet.
   *
   * @return The selected character.
   */
  public final String getSelectedChar() {
    return selectedChar;
  }

  /**
   * Sets selected character to variable.
   *
   * @param selectedChar <code>char</code>
   */
  public final void setSelectedChar(final String selectedChar) {
    this.selectedChar = selectedChar;
  }

  /**
   * Initializes components and add as child of form.<br>
   *
   * @throws Exception
   */
  public UIMyConnections() throws Exception {
    uiProfileUserSearch = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    uiProfileUserSearch.setTypeOfRelation(CONFIRMED_STATUS);
    if (Utils.isOwner()) {
      setHasPeopleTab(true);
    } else {
      setHasPeopleTab(false);
    }
    uiProfileUserSearch.setHasConnectionLink(false);
    uiProfileUserSearch.setLoadFromSearch(false);
    addChild(uiProfileUserSearch);
    init();
  }
  
  /**
   * Inits at the first loading.
   * 
   * @since 1.2.2
   */
  public void init() {
    try {
      setLoadAtEnd(false);
      enableLoadNext = false;
      currentLoadIndex = 0;
      loadingCapacity = MY_CONNECTION_PER_PAGE;
      peopleList = new ArrayList<Identity>();
      if (Utils.isOwner()) {
        uiProfileUserSearch.setHasPeopleTab(true);
      } else {
        uiProfileUserSearch.setHasPeopleTab(false);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  /**
   * Sets loading capacity.
   * 
   * @param loadingCapacity
   * @since 1.2.2
   */
  public void setLoadingCapacity(int loadingCapacity) {
    this.loadingCapacity = loadingCapacity;
  }

  /**
   * Gets flag to display LoadNext button or not.
   * 
   * @return the enableLoadNext
   * @since 1.2.2
   */
  public boolean isEnableLoadNext() {
    return enableLoadNext;
  }

  /**
   * Sets flag to display LoadNext button or not.
   * 
   * @param enableLoadNext the enableLoadNext to set
   * @since 1.2.2
   */
  public void setEnableLoadNext(boolean enableLoadNext) {
    this.enableLoadNext = enableLoadNext;
  }

  /**
   * Gets flags to clarify that load at the last element or not. 
   * 
   * @return the loadAtEnd
   * @since 1.2.2
   */
  public boolean isLoadAtEnd() {
    return loadAtEnd;
  }

  /**
   * Sets flags to clarify that load at the last element or not.
   * 
   * @param loadAtEnd the loadAtEnd to set
   * @since 1.2.2
   */
  public void setLoadAtEnd(boolean loadAtEnd) {
    this.loadAtEnd = loadAtEnd;
  }

  /**
   * Gets list of all type of people.
   * 
   * @return the peopleList
   * @throws Exception 
   * @since 1.2.2
   */
  public List<Identity> getPeopleList() throws Exception {
    if (!uiProfileUserSearch.isLoadFromSearch()) {
      this.peopleList = loadPeople(0, currentLoadIndex + loadingCapacity);
    }
    uiProfileUserSearch.setLoadFromSearch(false);
    
    int realPeopleListSize = this.peopleList.size();

    setEnableLoadNext((realPeopleListSize >= MY_CONNECTION_PER_PAGE)
            && (realPeopleListSize < getPeopleNum()));
    
    return this.peopleList;
  }

  /**
   * Sets list of all type of people.
   * 
   * @param peopleList the peopleList to set
   * @since 1.2.2
   */
  public void setPeopleList(List<Identity> peopleList) {
    this.peopleList = peopleList;
  }
  
  /**
   * Gets number of people for displaying.
   * 
   * @return the peopleNum
   * @since 1.2.2
   */
  public int getPeopleNum() {
    return peopleNum;
  }

  /**
   * Sets number of people for displaying.
   * 
   * @param peopleNum the peopleNum to set
   * @since 1.2.2
   */
  public void setPeopleNum(int peopleNum) {
    this.peopleNum = peopleNum;
  }

  /**
   * Gets people with ListAccess type.
   * 
   * @return the peopleListAccess
   * @since 1.2.2
   */
  public ListAccess<Identity> getPeopleListAccess() {
    return peopleListAccess;
  }

  /**
   * Sets people with ListAccess type.
   * 
   * @param peopleListAccess the peopleListAccess to set
   * @since 1.2.2
   */
  public void setPeopleListAccess(ListAccess<Identity> peopleListAccess) {
    this.peopleListAccess = peopleListAccess;
  }
  
  /**
   * increase offset.
   * @throws Exception
   */
  public void increaseOffset() throws Exception {
    currentLoadIndex += loadingCapacity;
  }
  
  /**
   * Loads people when searching.
   * 
   * @throws Exception
   * @since 1.2.2
   */
  public void loadSearch() throws Exception {
    currentLoadIndex = 0;
    setPeopleList(loadPeople(currentLoadIndex, loadingCapacity));
  }
  
  private List<Identity> loadPeople(int index, int length) throws Exception {

    lastOwner = Utils.getOwnerIdentity();

    ProfileFilter filter = uiProfileUserSearch.getProfileFilter();

    ListAccess<Identity> listAccess = Utils.getRelationshipManager().getConnectionsByFilter(lastOwner, filter);
    Identity[] identities = listAccess.load(index, length);

    setPeopleNum(identities.length < MY_CONNECTION_PER_PAGE ? identities.length : listAccess.getSize());
    setPeopleListAccess(listAccess);
    uiProfileUserSearch.setPeopleNum(getPeopleNum());

    return Arrays.asList(identities);

  }
  
  /**
   * Checks need to refresh relationship list or not.
   * @return
   */
  protected boolean isNewOwner() {
    Identity current = Utils.getOwnerIdentity();
    if (this.lastOwner == null || current == null) return false;
    return !this.lastOwner.getRemoteId().equals(current.getRemoteId());
  }
  
  /**
   * Listeners loading more people action.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 18, 2011
   * @since 1.2.2
   */
  public static class LoadMorePeopleActionListener extends EventListener<UIMyConnections> {
    public void execute(Event<UIMyConnections> event) throws Exception {
      UIMyConnections uiMyConnections = event.getSource();
      if (uiMyConnections.currentLoadIndex < uiMyConnections.peopleNum) {
        uiMyConnections.increaseOffset();
      } else {
        uiMyConnections.setEnableLoadNext(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMyConnections);
    }
  }
  
  /**
   * Listens to remove action then delete the relation.<br>
   *   - Gets information of user is removed.<br>
   *   - Checks the relation to confirm that still got relation.<br>
   *   - Removes the current relation.<br>
   *
   */
  public static class RemoveConnectionActionListener extends EventListener<UIMyConnections> {
    @Override
    public void execute(Event<UIMyConnections> event) throws Exception {
      UIMyConnections uiMyConnections = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(identityId, true);
      Relationship relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), requestedIdentity);

      uiMyConnections.setLoadAtEnd(false);
      if (relationship == null || relationship.getStatus() != Relationship.Type.CONFIRMED) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(RELATION_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.getRelationshipManager().delete(relationship);
      Utils.clearCacheOnUserPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMyConnections);
    }
  }

  /**
   * Listens event that broadcast from UIProfileUserSearch.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 25, 2011
   */
  public static class SearchActionListener extends EventListener<UIMyConnections> {
    @Override
    public void execute(Event<UIMyConnections> event) throws Exception {
      UIMyConnections uiMyConnections = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIProfileUserSearch uiSearch = uiMyConnections.uiProfileUserSearch;
      
      String charSearch = ctx.getRequestParameter(OBJECTID);
      
      ProfileFilter filter = uiMyConnections.uiProfileUserSearch.getProfileFilter();
      
      try {
        uiMyConnections.setSelectedChar(charSearch);
        if (charSearch != null) { // search by alphabet
          filter.setName(charSearch);
          filter.setPosition("");
          filter.setSkills("");
          filter.setFirstCharacterOfName(charSearch.toCharArray()[0]);
          if (UIProfileUserSearch.ALL_FILTER.equals(charSearch)) {
            filter.setFirstCharacterOfName(UIProfileUserSearch.EMPTY_CHARACTER);
            filter.setName("");
          }
          uiSearch.setRawSearchConditional("");
        } else if (UIProfileUserSearch.ALL_FILTER.equals(uiSearch.getRawSearchConditional())) {
          uiMyConnections.setSelectedChar(UIProfileUserSearch.ALL_FILTER);
        }
        
        uiSearch.setProfileFilter(filter);
        uiSearch.setNewSearch(true);
        uiMyConnections.uiProfileUserSearch.setLoadFromSearch(true);
      } catch (Exception e) {
        uiSearch.setIdentityList(new ArrayList<Identity>());
      }
      
      uiMyConnections.loadSearch();
      uiMyConnections.setLoadAtEnd(false);
    }
  }

  /**
   * Return true to accept user is viewing can edit.
   *
   * @return true if current user is current login user.
   */
  public boolean isEditable () {
    return Utils.isOwner();
  }

}
