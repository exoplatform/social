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
package org.exoplatform.social.feedmash;

import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.PeriodJob;
import org.quartz.JobDataMap;

public class FeedmashJobPlugin extends PeriodJob {
  private static final Log LOG = ExoLogger.getLogger(FeedmashJobPlugin.class);
  private JobDataMap jobDataMap;

  @SuppressWarnings("unchecked")
  public FeedmashJobPlugin(InitParams params, ExoContainerContext context) throws Exception {
    super(params);
    Map mashinfo = params.getPropertiesParam("mash.info").getProperties();

    // portalContainer may be indicated to target a specific container
    String portalContainer = (String) mashinfo.get("portalContainer");
    if (portalContainer == null) {
      // for all
      portalContainer = context.getPortalContainerName();
    }
    mashinfo.put("portalContainer", portalContainer);
    mashinfo.put("pluginName", getClass() + "-"+ System.currentTimeMillis());
    LOG.info("Initializing feedmash plugin :\n" + mashinfo);
    LOG.info("Job info :\n" + params.getPropertiesParam("job.info"));
    jobDataMap = new JobDataMap(mashinfo);
  }

  @Override
  public JobDataMap getJobDataMap()
  {
     return jobDataMap;
  }

}
