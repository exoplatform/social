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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Renderer for {@link IMType}'s value control in user profile. IM value control is optional. This class
 * already a component plugin for use with {@link UserProfileRenderingService}.<br>
 * 
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: UIIMTypeControlRenderer.java 00000 May 4, 2017 pnedonosko $
 * 
 */
public abstract class UIIMControlRenderer extends BaseComponentPlugin {

  /** The Constant IM_TYPE_ID. */
  public static final String IM_TYPE_ID = "im-type-id";

  /**
   * Read type ID.
   *
   * @param initParams the init params
   * @return the string
   * @throws ConfigurationException if IM type ID not configured
   */
  protected static String readTypeID(InitParams initParams) throws ConfigurationException {
    if (initParams != null) {
      ValueParam v = initParams.getValueParam(IM_TYPE_ID);
      if (v != null) {
        String id = v.getValue();
        if (id != null && (id = id.trim()).length() > 0) {
          return id;
        }
      }
      throw new ConfigurationException("Field required: " + IM_TYPE_ID);
    }
    throw new ConfigurationException("init-params required in renderer configuration");
  }

  /**
   * Finds the IM type.
   *
   * @param imtId the imt id
   * @param settingsService the settings service
   * @return the IM type, never <code>null</code>
   * @throws ConfigurationException if IM type not found in {@link UserProfileSettingsService}
   */
  protected static IMType findIMType(String imtId,
                                   UserProfileSettingsService settingsService) throws ConfigurationException {
    IMType imt = settingsService.getIMType(imtId);
    if (imt != null) {
      return imt;
    }
    throw new ConfigurationException("No such IM type: " + imtId);
  }

  /** The IM type for this renderer. */
  protected final IMType imType;

  /**
   * Instantiates a new IM type renderer from configuration's parameters.
   *
   * @param settingsService the settings service
   * @param initParams the init params
   * @throws ConfigurationException the configuration exception
   */
  protected UIIMControlRenderer(UserProfileSettingsService settingsService, InitParams initParams)
      throws ConfigurationException {
    this(findIMType(readTypeID(initParams), settingsService));
  }

  /**
   * Instantiates a new IM type renderer without configuration.
   *
   * @param imtype the imtype
   */
  public UIIMControlRenderer(IMType imtype) {
    this.imType = imtype;
  }

  /**
   * Gets the IM type associated with this renderer.
   *
   * @return the IM type, never <code>null</code>
   */
  public IMType getType() {
    return imType;
  }

  /**
   * Add required page markup (HTML), styles, scripts or perform other UI manipulations for given IM value
   * (account).
   *
   * @param imValue the IM value
   * @param context the WebUI context
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  public abstract void render(String imValue, WebuiRequestContext context) throws IOException, Exception;

}
