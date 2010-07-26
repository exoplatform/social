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
package org.exoplatform.social.extras.samples.activity;

import org.exoplatform.social.webui.composer.UIActivityComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;

/**
 *
 * @author    <a href="http://hoatuicomponent.getActivityComposersle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 22, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
                 lifecycle = UIApplicationLifecycle.class,
                 template = "classpath:groovy/social/webui/activity/UIEmotionActivityComposer.gtmpl",
                 events = {
                  @EventConfig(listeners = UIActivityComposer.CloseActionListener.class),
                  @EventConfig(listeners = UIActivityComposer.SubmitContentActionListener.class),
                  @EventConfig(listeners = UIActivityComposer.ActivateActionListener.class)
                 })
public class UIEmotionActivityComposer extends UIActivityComposer {

  @Override
  protected void onActivate(Event<UIActivityComposer> event) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onClose(Event<UIActivityComposer> event) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> event) {
    // TODO Auto-generated method stub

  }


  @Override
  public void postActivity(PostContext postContext,
                           UIComponent source,
                           WebuiRequestContext requestContext,
                           String postedMessage) throws Exception {
    // TODO Auto-generated method stub

  }

}
