/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;

public class MockMessageQueue implements QueueMessage {

  @Override
  public boolean put(MessageInfo messageInfo) {
    return true;
  }

  @Override
  public void send() {
    // do nothing
  }

  @Override
  public boolean sendMessage(MessageInfo message) throws Exception {
    return true;
  }

  @Override
  public void removeAll() throws Exception {
    // do nothing
  }

  @Override
  public void enable(boolean enabled) {
    // do nothing
  }
}
