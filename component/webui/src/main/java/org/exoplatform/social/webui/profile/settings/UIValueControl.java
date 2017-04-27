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

import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Option value UI control renderer. This interface used by various renderers of
 * {@link UserProfileRenderingService}.
 * 
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: UIValueControl.java 00000 May 4, 2017 pnedonosko $
 * 
 */
public interface UIValueControl {

  /**
   * Render the value control. At this point the value field already rendered on the page and writer stands
   * just after it.
   *
   * @param key the key
   * @param value the value to which add a control
   * @param context the WebUI context
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception the exception
   */
  void render(String key, String value, WebuiRequestContext context) throws IOException, Exception;

}
