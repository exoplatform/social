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
package org.exoplatform.social.space.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.exoplatform.social.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;

import junit.framework.TestCase;

public class SpaceLifeCycleTest extends TestCase {


  public void testSimpleBroadcast() {

    AwaitingLifeCycle lifecycle = new AwaitingLifeCycle();

    MockListener capture = new MockListener();
    lifecycle.addListener(capture);
    MockListener capture2 = new MockListener();
    lifecycle.addListener(capture2);

    lifecycle.activateApplication(null, "foo");
    lifecycle.activateApplication(null, "bar");

    lifecycle.await(100);  // wait for the executor to finish

    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));


  }

  public void testBroadcastWithFailingListener() {

    AwaitingLifeCycle lifecycle = new AwaitingLifeCycle();
    MockListener capture = new MockListener();
    lifecycle.addListener(capture);
    MockFailingListener failing = new MockFailingListener();
    lifecycle.addListener(failing);
    MockListener capture2 = new MockListener();
    lifecycle.addListener(capture2);

    lifecycle.activateApplication(null, "foo");
    lifecycle.activateApplication(null, "bar");

    lifecycle.await(100); // wait for the executor to finish

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

  }

  class MockFailingListener extends MockListener {
    protected void recordEvent(SpaceLifeCycleEvent event) {
      throw new RuntimeException("fake runtime exception thrown on purpose");
    }
  }


  /**
   * Custom SpaceLifeCycle for testing purpose, that can wait until all events are dispatched.
   * Necessary to avoid test failures when the internal executor has not finished executing all listeners.
   * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
   * @version $Revision$
   */
  class AwaitingLifeCycle extends SpaceLifecycle {

    /**
     * Awaits until all events are dispatched
     * @param milisconds
     */
    public void await(long milisconds) {

        try {
          executor.awaitTermination(milisconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          // ignore
        }

    }


  }



}
