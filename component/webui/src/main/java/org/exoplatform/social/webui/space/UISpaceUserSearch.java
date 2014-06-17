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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * {@link UISpaceUserSearch} used for searching users in a space. <br />
 * The search event should broadcast for the parent one to catch and
 * get searched userList from UISpaceUserSearch <br />
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Sep 18, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/space/UISpaceUserSearch.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceUserSearch.SearchActionListener.class)
  }
)
public class UISpaceUserSearch extends UIForm {
  private List<User> userList;
  private String groupId;
  static private final String FIELD_KEYWORD = "fieldKeyword";
  static private final String FIELD_FILTER = "fieldFilter";
  static private final String USER_NAME = "userName";
  static private final String LAST_NAME = "lastName";
  static private final String FIRST_NAME = "firstName";
  static private final String EMAIL = "email";

  /**
   * Constructor to initialize form fields
   * @throws Exception
   */
  public UISpaceUserSearch() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_KEYWORD, FIELD_KEYWORD, null));
    addUIFormInput(new UIFormSelectBox(FIELD_FILTER, FIELD_FILTER, getFilters()));
    groupId = getGroupId();
  }

  /**
   * userList setter
   * @param userList
   */
  public void setUserList(List<User> userList) {
    this.userList = userList;
  }

  /**
   * userList getter
   * @return userList
   */
  public List<User> getUserList() {
    return userList;
  }

  /**
   * gets groupId from current space
   * @return groupId
   */
  private String getGroupId() throws Exception {
    if (groupId == null) {
      String spaceUrl = Utils.getSpaceUrlByContext();
      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      return spaceService.getSpaceByUrl(spaceUrl).getGroupId();
    }
    return groupId;
  }

  /**
   * gets list of filter options
   * @return list of filter options
   * @throws Exception
   */
  private List<SelectItemOption<String>> getFilters() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(USER_NAME, USER_NAME));
    options.add(new SelectItemOption<String>(FIRST_NAME, FIRST_NAME));
    options.add(new SelectItemOption<String>(LAST_NAME, LAST_NAME));
    options.add(new SelectItemOption<String>(EMAIL, EMAIL));
    return options;
  }

  /**
   * search users based on keyword, filter and groupId provided
   * @param keyword
   * @param filter
   * @param groupId
   * @return user list
   * @throws Exception
   */
  @SuppressWarnings({"unchecked", "deprecation"})
  protected List<User> search(String keyword, String filter, String groupId) throws Exception {
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    Query q = new Query();
    if(keyword != null && keyword.trim().length() != 0) {
      if(keyword.indexOf("*")<0){
        if(keyword.charAt(0)!='*') keyword = "*"+keyword;
        if(keyword.charAt(keyword.length()-1)!='*') keyword += "*";
      }
      keyword = keyword.replace('?', '_');
      if(USER_NAME.equals(filter)) {
        q.setUserName(keyword);
      }
      if(LAST_NAME.equals(filter)) {
        q.setLastName(keyword);
      }
      if(FIRST_NAME.equals(filter)) {
        q.setFirstName(keyword);
      }
      if(EMAIL.equals(filter)) {
        q.setEmail(keyword);
      }
    }
    List results = new CopyOnWriteArrayList();
    results.addAll(service.getUserHandler().findUsers(q).getAll());
    // remove if user doesn't exist in selected group
    MembershipHandler memberShipHandler = service.getMembershipHandler();
    if(groupId != null && groupId.trim().length() != 0) {
      for(Object user : results) {
        if(memberShipHandler.findMembershipsByUserAndGroup(((User)user).getUserName(), groupId).size() == 0) {
          results.remove(user);
        }
      }
    }
    return results;
  }

  /**
   * triggers this action when user clicks on search button. <br />
   * gets the keyword and filter from the form and search by this criteria.
   * @author hoatle
   */
  static public class SearchActionListener extends EventListener<UISpaceUserSearch> {
    @Override
    public void execute(Event<UISpaceUserSearch> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UISpaceUserSearch uiSearch = event.getSource();
      String keyword = uiSearch.getUIStringInput(FIELD_KEYWORD).getValue();
      String filter = uiSearch.getUIFormSelectBox(FIELD_FILTER).getValue();
      uiSearch.setUserList(uiSearch.search(keyword, filter, uiSearch.getGroupId()));
      uiSearch.<UIComponent>getParent().createEvent("Search", Phase.DECODE, ctx).broadcast();
    }
  }
}
