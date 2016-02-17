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
package org.exoplatform.social.common.lifecycle;

/**
 * An event fired at different stages of the lifecycle
 *
 * @see LifeCycleListener
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice  Lamarque</a>
 * @version $Revision$
 */
public class LifeCycleEvent<S,P> {

  /**
   * space where the event occurs
   */
  protected P  payload;

  /**
   * source of the event.
   */
  protected S source;

  public LifeCycleEvent(S source, P payload) {
    this.payload = payload;
    this.source = source;
  }


  public P getPayload() {
    return payload;
  }

  public S getSource() {
    return source;
  }



}
