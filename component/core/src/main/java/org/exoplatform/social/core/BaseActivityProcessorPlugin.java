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
package org.exoplatform.social.core;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;

/**
 * A base plugin to configure {@link ActivityProcessor}s for
 * {@link ActivityManager}.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public abstract class BaseActivityProcessorPlugin extends BaseComponentPlugin implements
    ActivityProcessor {
  protected int            priority;

  private static final Log LOG = ExoLogger.getLogger(BaseActivityProcessorPlugin.class);

  public BaseActivityProcessorPlugin(InitParams params) {

    try {
      priority = Integer.valueOf(params.getValueParam("priority").getValue());
      if (priority < 1) {
        LOG.warn("<value-param> 'priority' of type int should be higher than 1");
        priority = 1;
      } else if (priority > 10) {
        LOG.warn("<value-param> 'priority' of type int should be lower than 10");
        priority = 10;

      }
    } catch (Exception e) {
      priority = 5; //default, it should be in range of 1-10
      LOG.warn("an <value-param> 'priority' of type int is recommanded for component " + getClass());
    }
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public abstract void processActivity(Activity activity);
}
