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
package social.portal.webui.component.space;

import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * UIProfileUserSearch for search users in profile.
 * The search event should broadcast for the parent one to catch and
 * get searched Identity List from UIProfileUserSearch
 * Author : hanhvi
 *          hanhvq@gmail.com
 * Oct 28, 2009  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/groovy/portal/webui/space/UISpaceSearch.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceSearch.SearchActionListener.class) 
  }
)
public class UISpaceSearch extends UIForm {
  /** USER CONTACT. */
  final public static String SPACE_SEARCH = "SpaceSearch";
  /** SEARCH. */
  final public static String SEARCH = "Search";
  /** DEFAULT SPACE NAME SEARCH. */
  final public static String DEFAULT_SPACE_NAME_SEARCH = "Space name";
  SpaceService spaceService = null;
  private List<Space> spaceList = null;
  /** Selected character when search by alphabet */
  String selectedChar = null;
  
  /**
   * Constructor to initialize form fields
   * @throws Exception
   */
  public UISpaceSearch() throws Exception { 
    addUIFormInput(new UIFormStringInput(SPACE_SEARCH, null, DEFAULT_SPACE_NAME_SEARCH));
  }
  
  /**
   * identityList setter
   * @param identityList
   */
  public void setSpaceList(List<Space> spaceList) {
    this.spaceList = spaceList;
  }
  
  /**
   * identityList getter
   * @return
   * @throws Exception 
   */
  public List<Space> getSpaceList() throws Exception {
    return spaceList;
  }

  public String getSelectedChar() { return selectedChar;}

  public void setSelectedChar(String selectedChar) { this.selectedChar = selectedChar;}
  
  /**
   * SearchActionListener
   * Get the space name from request and search spaces that have name like input space name.
   * Search space and set result into spaceList. 
   */
  static public class SearchActionListener extends EventListener<UISpaceSearch> {
    @Override
    public void execute(Event<UISpaceSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UISpaceSearch uiSpaceSearch = event.getSource();
      String charSearch = ctx.getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceSearch.getSpaceService();
      ResourceBundle resApp = ctx.getApplicationResourceBundle();
      String defaultSpaceName = resApp.getString(uiSpaceSearch.getId() + ".label.SpaceName");
      String spaceName = ((UIFormStringInput)uiSpaceSearch.getChild(UIFormStringInput.class)).getValue();
      spaceName = ((spaceName == null) || spaceName.equals(defaultSpaceName)) ? "" : spaceName;
      spaceName = (charSearch != null) ? charSearch : spaceName;
      spaceName = ((charSearch != null) && "All".equals(charSearch)) ? "" : spaceName;
      if (charSearch != null) ((UIFormStringInput)uiSpaceSearch.getChildById(SPACE_SEARCH)).setValue(DEFAULT_SPACE_NAME_SEARCH);
      uiSpaceSearch.setSelectedChar(charSearch);
      List<Space> spaceSearchResult = spaceService.getSpacesByName(spaceName, (charSearch == null) ? false : true );
      uiSpaceSearch.setSpaceList(spaceSearchResult);
      
      Event<UIComponent> searchEvent = uiSpaceSearch.<UIComponent>getParent().createEvent(SEARCH, Event.Phase.DECODE, ctx);
      if (searchEvent != null) {
        searchEvent.broadcast();
      }
    }
  }
  
  /**
   * get {@SpaceService}
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }
  
}
