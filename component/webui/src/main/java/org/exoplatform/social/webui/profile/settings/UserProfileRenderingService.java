/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.social.webui.profile.settings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * It is a common place for user profile fields and its controls renderers. Each type of file or control
 * should be acquired via a dedicated method, e.g. {@link #getIMControl()} returns IM account field control
 * renderer. These renderers can be used in actual WebUI components at rendering phase.
 * 
 * 
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: UserProfileRenderingService.java 00000 May 4, 2017 pnedonosko $
 * 
 */
public class UserProfileRenderingService {

  /** Logger. */
  private static final Log LOG = ExoLogger.getLogger(UserProfileRenderingService.class);

  /**
   * The IMValueControl class.
   */
  class UIIMValueControl implements UIValueControl {

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(String imtId, String imId, WebuiRequestContext context) throws IOException, Exception {
      UIIMControlRenderer renderer = imControls.get(imtId);
      if (renderer != null) {
        renderer.render(imId, context);
      }
    }
  }

  /** The settings service. */
  protected final UserProfileSettingsService       settingsService;

  /** The im controls. */
  protected final Map<String, UIIMControlRenderer> imControls = new HashMap<>();

  /**
   * Instantiates a new user profile rendering service.
   *
   * @param settingsService the settings service
   */
  public UserProfileRenderingService(UserProfileSettingsService settingsService) {
    this.settingsService = settingsService;
  }

  /**
   * Adds the IM type renderer.
   *
   * @param plugin the plugin
   */
  public void addIMControl(UIIMControlRenderer plugin) {
    IMType imt = plugin.getType();
    UIIMControlRenderer prev = imControls.put(imt.getId(), plugin);
    if (prev != null) {
      LOG.info("IM type '" + imt.getId() + "' UI control redefined from '" + prev.getType() + "' to '" + imt
          + "'");
    }
  }

  /**
   * Gets the IM account UI control renderer.
   *
   * @return the {@link UIValueControl} instance
   */
  public UIValueControl getIMControl() {
    return new UIIMValueControl();
  }

}
