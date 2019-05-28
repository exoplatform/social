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

package org.exoplatform.social.webui;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.organization.account.UIGroupSelector;

@ComponentConfigs({
    @ComponentConfig(template = "war:/groovy/social/webui/space/UISocialGroupSelector.gtmpl", events = {
        @EventConfig(phase = Phase.DECODE, listeners = UIGroupSelector.ChangeNodeActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIGroupSelector.SelectGroupActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIGroupSelector.SelectPathActionListener.class) }),
    @ComponentConfig(type = UITree.class, id = "UITreeGroupSelector", template = "system:/groovy/webui/core/UITree.gtmpl", events = @EventConfig(phase = Phase.DECODE, listeners = UITree.ChangeNodeActionListener.class)),
    @ComponentConfig(type = UIBreadcumbs.class, id = "BreadcumbGroupSelector", template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl", events = @EventConfig(phase = Phase.DECODE, listeners = UIBreadcumbs.SelectPathActionListener.class)) })
public class UISocialGroupSelector extends UIGroupSelector {

  public UISocialGroupSelector() throws Exception {
    super();
  }

  @Override
  protected List<Group> getGroups(Group parentGroup) throws Exception {
    OrganizationService organizationService = getApplicationComponent(OrganizationService.class);
    UserACL userACL = getApplicationComponent(UserACL.class);

    ConversationState conversationState = ConversationState.getCurrent();
    if(conversationState != null && conversationState.getIdentity() != null) {
      return organizationService.getGroupHandler().findGroups(parentGroup)
              .stream()
              .filter(group -> userACL.hasPermission(conversationState.getIdentity(), group, "space_creation_invitation"))
              .collect(Collectors.toList());
    } else {
      return Collections.EMPTY_LIST;
    }
  }
}
