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
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueNotification extends ConcurrentLinkedQueue<NotificationMessage> {
  private static final long serialVersionUID = 1L;

  @Override
  public boolean contains(Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Iterator<NotificationMessage> iterator() {
    return super.iterator();
  }

  @Override
  public Object[] toArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean remove(Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends NotificationMessage> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean add(NotificationMessage e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean offer(NotificationMessage e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public NotificationMessage remove() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NotificationMessage poll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NotificationMessage element() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NotificationMessage peek() {
    // TODO Auto-generated method stub
    return null;
  }

}
