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
package org.exoplatform.social.webui.space;

import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/space/UISpaceSearch.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceSearch.AddSpaceActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UISpaceSearch.SearchActionListener.class)
  }
)
public class UISpaceSearch extends UIForm {
  /**
   * SPACE SEARCH.
   */
  public static final String SPACE_SEARCH = "SpaceSearch";

  /**
   * SEARCH.
   */
  public static final String SEARCH = "Search";

  private static final String ASTERIK_STR = "*";
  
  private static final String PERCENTAGE_STR = "%";
  
  private static final String POPUP_ADD_SPACE = "UIPopupAddSpace";

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /** Html attribute placeholder. */
  private static final String HTML_ATTRIBUTE_PLACEHOLDER   = "placeholder";
  
  /**
   * The spaceService is used for SpaceService instance storage.
   */
  SpaceService spaceService = null;

  /**
   * The spaceList is used for search result storage.
   */
  private List<Space> spaceList = null;

  /**
   * The spaceNameSearch is used for input space name storage.
   */
  String spaceNameSearch = null;

  /**
   * Number of matching spaces.
   */
  int spaceNum;
  
  /**
   * Contains all space name in individual context for auto suggesting.
   */
  List<String> spaceNameForAutoSuggest = null;

  /**
   * The flag notifies a new search when clicks search icon or presses enter.
   */
  private boolean isNewSearch;

  /**
   * Used stores type of relation with current user information.
   */
  String typeOfRelation = null;

  /**
   * URL of space that this UIComponent is used in member searching.
   */
  String spaceURL = null;

  /**
   * Gets input space name search input.
   *
   * @return Name of space.
   */
  public String getSpaceNameSearch() {
    return spaceNameSearch;
  }

  /**
   * Sets input space name search.
   *
   * @param spaceNameSearch A {@code String}
   */
  public void setSpaceNameSearch(String spaceNameSearch) {
    this.spaceNameSearch = spaceNameSearch;
  }

  /**
   * Gets number of matching spaces.
   * 
   * @return the spaceNum
   */
  public int getSpaceNum() {
    return spaceNum;
  }

  /**
   * Sets number of matching spaces.
   * 
   * @param spaceNum the spaceNum to set
   */
  public void setSpaceNum(int spaceNum) {
    this.spaceNum = spaceNum;
  }

  /**
   * Gets type of relation with current user.
   */
  public String getTypeOfRelation() {
    return (typeOfRelation != null) ? typeOfRelation : "";
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
    return (spaceURL != null) ? spaceURL : "";
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
   * Sets result of searching to list.
   *
   * @param spaceList The list of space. A {@code List}
   */
  public void setSpaceList(List<Space> spaceList) {
    this.spaceList = spaceList;
  }

  /**
   * Gets list of searching.
   *
   * @return List of space.
   * @throws Exception
   */
  public List<Space> getSpaceList() throws Exception {
    return spaceList;
  }

  /**
   * Initializes search form fields.
   *
   * @throws Exception
   */
  public UISpaceSearch() throws Exception {
    if (getId() == null) {
      setId("UISpaceSearch");
    }
    //
    addUIFormInput(new UIFormStringInput(SPACE_SEARCH, SPACE_SEARCH, ""));
    addChild(PopupContainer.class, null, "PopupContainer_SpaceSearch");
    //
    setSubmitAction("return false;");
  }

  protected void initPlaceholder() throws Exception {
    UIFormStringInput findSpace = getUIStringInput(SPACE_SEARCH);
    findSpace.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, getLabel("FindSpace"));
    findSpace.setHTMLAttribute(HTML_ATTRIBUTE_PLACEHOLDER, getLabel("DefaultSpaceNameAndDesc"));
  }

  /**
   * Listens to search event is broadcasted from search form, then processes search condition and
   * set search result to the result variable.<br> - Gets space name from request.<br> - Searches
   * spaces that have name like input space name.<br> - Sets matched space into result list.<br>
   */
  static public class SearchActionListener extends EventListener<UISpaceSearch> {
    @Override
    public void execute(Event<UISpaceSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UISpaceSearch uiSpaceSearch = event.getSource();
      String searchCondition = uiSpaceSearch.getUIStringInput(SPACE_SEARCH).getValue();
      if (searchCondition == null || ASTERIK_STR.equals(searchCondition) || PERCENTAGE_STR.equals(searchCondition)) {
        uiSpaceSearch.setSpaceNameSearch(null);
        uiSpaceSearch.setNewSearch(true);
      } else {
        if (searchCondition != null) {
          searchCondition = Utils.normalizeString(searchCondition);
        }
        uiSpaceSearch.setSpaceNameSearch(searchCondition);
        uiSpaceSearch.setNewSearch(true);
      }
      Event<UIComponent> searchEvent = uiSpaceSearch.<UIComponent>getParent().createEvent(SEARCH, Event.Phase.PROCESS, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }
  }

  /**
   * This action is triggered when user clicks on AddSpace <br>
   * <br>
   * UIAddSpaceForm will be displayed in a popup window
   */
  static public class AddSpaceActionListener extends EventListener<UISpaceSearch> {
    @Override
    public void execute(Event<UISpaceSearch> event) throws Exception {
      UISpaceSearch uiSpaceSearch = event.getSource();
      PopupContainer uiPopup = uiSpaceSearch.getChild(PopupContainer.class);
      uiPopup.activate(UISpaceAddForm.class, 625, POPUP_ADD_SPACE);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }

  public boolean isNewSearch() {
    return isNewSearch;
  }

  public void setNewSearch(boolean isNewSearch) {
    this.isNewSearch = isNewSearch;
  }
  
  public boolean checkPermissionCreateSpace() throws Exception {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    SpacesAdministrationService spacesAdministrationService = CommonsUtils.getService(SpacesAdministrationService.class);
  
    return spacesAdministrationService.canCreateSpace(userId);
  }
}
