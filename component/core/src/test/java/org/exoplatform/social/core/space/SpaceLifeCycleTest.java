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
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.lifecycle.LifeCycleCompletionService;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceLifeCycleTest extends AbstractCoreTest {

  private LifeCycleCompletionService completionService;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    completionService = (LifeCycleCompletionService) PortalContainer.getInstance().getComponentInstanceOfType(LifeCycleCompletionService.class);
  }

  public void testSimpleBroadcast() {

    SpaceLifecycle lifecycle = new SpaceLifecycle();

    MockListener capture = new MockListener();
    lifecycle.addListener(capture);
    MockListener capture2 = new MockListener();
    lifecycle.addListener(capture2);

    lifecycle.activateApplication(null, "foo");
    lifecycle.activateApplication(null, "bar");

    end();
    completionService.waitCompletionFinished();
    begin();

    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));

  }

  public void testBroadcastWithFailingListener() {

    SpaceLifecycle lifecycle = new SpaceLifecycle();
    MockListener capture = new MockListener();
    lifecycle.addListener(capture);
    MockFailingListener failing = new MockFailingListener();
    lifecycle.addListener(failing);
    MockListener capture2 = new MockListener();
    lifecycle.addListener(capture2);

    lifecycle.activateApplication(null, "foo");
    lifecycle.activateApplication(null, "bar");

    end();
    completionService.waitCompletionFinished();
    begin();

    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));
    assertFalse(failing.hasEvent("bar"));
    assertFalse(failing.hasEvent("foo"));

  }

  class MockListener implements SpaceLifeCycleListener {
    public Collection<String> events = new ArrayList<String>();

    public boolean hasEvent(String event) {
      return events.contains(event);
    }

    protected void recordEvent(SpaceLifeCycleEvent event) {
      events.add(event.getTarget());
    }

    public void applicationActivated(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void applicationAdded(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void applicationDeactivated(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void applicationRemoved(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void grantedLead(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void joined(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void left(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void revokedLead(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceCreated(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceRemoved(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceRenamed(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }

    public void spaceAccessEdited(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }
    
    public void addInvitedUser(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }
    
    public void addPendingUser(SpaceLifeCycleEvent event) {
      recordEvent(event);
    }


  }

  class MockFailingListener extends MockListener {
    protected void recordEvent(SpaceLifeCycleEvent event) {
      throw new RuntimeException("fake runtime exception thrown on purpose");
    }
  }

}
