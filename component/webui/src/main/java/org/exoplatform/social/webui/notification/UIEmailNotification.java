/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.webui.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/notification/UIEmailNotification.gtmpl",
  events = {
    @EventConfig(listeners = UIEmailNotification.SaveActionListener.class),
    @EventConfig(listeners = UIEmailNotification.CancelActionListener.class) 
  }
)
public class UIEmailNotification extends UIForm {
  
  private static final String NEW_USER  = "NewUser";
  
  private static final String CONNECTION_REQUEST  = "ConnectionRequest";
  
  private static final String SPACE_INVITATION  = "SpaceInvitation";
  
  private static final String REQUEST_JOIN_SPACE  = "RequestJoinSpace";
  
  private static final String POSTED_ON_SPACE  = "PostedOnSpace";
  
  private static final String MENTION_ME  = "MentionMe";
  
  private static final String COMMENT_ON_MY_ACTIVITIES  = "CommentOnMyActivities";
  
  private static final String FREQUENCY_NOTICE  = "FrequencyNotice";
  
  private static final String FREQUENCY  = "Frequency";
  
  private static final String Daily  = "Daily";
  
  private static final String Weekly  = "Weekly";

  private static final String Monthly  = "Monthly";
  
  private final static Log LOG = ExoLogger.getExoLogger(UIEmailNotification.class);
  
  public UIEmailNotification() throws Exception {
    UICheckBoxInput newUser = new UICheckBoxInput(NEW_USER, NEW_USER, false);
    UICheckBoxInput connectionRequest = new UICheckBoxInput(CONNECTION_REQUEST, CONNECTION_REQUEST, false);
    UICheckBoxInput spaceInvitation = new UICheckBoxInput(SPACE_INVITATION, SPACE_INVITATION, false);
    UICheckBoxInput requestJoinSpace = new UICheckBoxInput(REQUEST_JOIN_SPACE, REQUEST_JOIN_SPACE, false);
    UICheckBoxInput postedOnSpace = new UICheckBoxInput(POSTED_ON_SPACE, POSTED_ON_SPACE, false);
    UICheckBoxInput mentionMe = new UICheckBoxInput(MENTION_ME, MENTION_ME, false);
    UICheckBoxInput commentOnMyActivities = new UICheckBoxInput(COMMENT_ON_MY_ACTIVITIES, COMMENT_ON_MY_ACTIVITIES, false);
    UICheckBoxInput frequencyNotice = new UICheckBoxInput(FREQUENCY_NOTICE, FREQUENCY_NOTICE, false);
    UIFormSelectBox frequency = new UIFormSelectBox(FREQUENCY, FREQUENCY, null);
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(getLabel(Daily), Daily));
    ls.add(new SelectItemOption<String>(getLabel(Weekly), Weekly));
    ls.add(new SelectItemOption<String>(getLabel(Monthly), Monthly));
    frequency.setOptions(ls);
    frequency.setDisabled(!frequencyNotice.isChecked());
    addChild(newUser);
    addChild(connectionRequest);
    addChild(spaceInvitation);
    addChild(requestJoinSpace);
    addChild(postedOnSpace);
    addChild(mentionMe);
    addChild(commentOnMyActivities);
    addChild(frequencyNotice);
    addChild(frequency);
    setActions(new String[] {"Save", "Cancel"});
  }

  public static class SaveActionListener extends EventListener<UIEmailNotification> {
    public void execute(Event<UIEmailNotification> event) throws Exception {
      LOG.debug("Save Action");
    }
  }

  public static class CancelActionListener extends EventListener<UIEmailNotification> {
    public void execute(Event<UIEmailNotification> event) throws Exception {
      LOG.debug("Cancel Action");
    }
  }
}
