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

import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: IMTypePlugin.java 00000 Apr 27, 2017 pnedonosko $
 */
public interface IMType {

  /**
   * Gets the IM type ID. ID used for identification and referencing the type in content and i18n resources.
   *
   * @return the IM type ID
   */
  String getId();

  /**
   * Gets the IM type name. Name used for UI, log, admin screens.
   *
   * @return the IM type name
   */
  String getName();
  
  /**
   * Inits the UI context. This method can be used for custom UI initialization on a portal page.
   *
   * @param context the context of WebUI app
   * @param imId the IM id or <code>null</code> (optional)
   */
  default void initUI(WebuiRequestContext context, String imId) {
    // do nothing by default
  }

  /**
   * Checks if is same.
   *
   * @param other the other
   * @return true, if is same
   */
  default boolean isSame(IMType other) {
    return getId().equals(other.getId()) && getName().equals(other.getName());
  }

  /**
   * To info string.
   *
   * @return the string
   */
  default String toInfoString() {
    return new StringBuilder(getId()).append('[').append(getName()).append(']').append(this).toString();
  }

}
