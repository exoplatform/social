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
package org.exoplatform.social.core.profile.settings;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: UserProfileSettingsService.java 00000 Apr 26, 2017 pnedonosko $
 */
public class UserProfileSettingsService {

  /** The Constant LOG. */
  private static final Log            LOG        = ExoLogger.getLogger(UserProfileSettingsService.class);

  /** The im types map. */
  protected final Map<String, IMType> imTypesMap = new HashMap<>();

  /**
   * Instantiates a new user profile settings service.
   */
  public UserProfileSettingsService() {
    // what here?
  }

  /**
   * Adds the IM type plugin.
   *
   * @param plugin the plugin
   */
  public void addIMTypePlugin(IMTypePlugin plugin) {
    IMType imt = plugin.getType();
    addType(imt);
  }

  /**
   * Adds the IM type plugin.
   *
   * @param plugin the plugin
   */
  public void addSimpleIMTypePlugin(SimpleIMTypePlugin plugin) {
    for (IMType imt : plugin.getTypes()) {
      addType(imt);
    }
  }

  /**
   * Gets the IM types.
   *
   * @return the IM types
   */
  public Collection<IMType> getIMTypes() {
    return Collections.unmodifiableCollection(imTypesMap.values());
  }

  /**
   * Gets the IM type.
   *
   * @param id the id
   * @return the IM type
   */
  public IMType getIMType(String id) {
    return imTypesMap.get(id);
  }

  // ****** internals *******

  /**
   * Adds the type.
   *
   * @param imt the imt
   */
  private void addType(IMType imt) {
    IMType prev = imTypesMap.put(imt.getId(), imt);
    if (prev != null) {
      LOG.warn("IM type '" + imt.getId() + "' redefined from '" + prev.toInfoString() + "' to '"
          + imt.toInfoString() + "'");
    }
  }

}
