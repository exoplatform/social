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
package social.portal.webui.component;

import org.exoplatform.social.space.Space;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/portal/webui/uiform/UISpaceInvitation.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceInvitation.AcceptActionListener.class ),
        @EventConfig(listeners = UISpaceInvitation.DeclineActionListener.class )
      }
)
public class UISpaceInvitation extends UIForm{
  
  private String leaderName;
  private Space space;
  
  static public class AcceptActionListener extends EventListener<UISpaceInvitation> {
    public void execute(Event<UISpaceInvitation> event) throws Exception {
    }
  }
   
  static public class DeclineActionListener extends EventListener<UISpaceInvitation> {
    public void execute(Event<UISpaceInvitation> event) throws Exception {
    }
  }
  
  public void setValue(String userName, Space space) {
    this.leaderName = userName;
    this.space = space;
  }
  
  public String getLeader() {
    return leaderName;
  }
  
  public Space getSpace() {
    return space;
  }
}