/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.social.common.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class AbstractLifeCycleTest extends TestCase {


  public void testSimpleBroadcast() {

    AwaitingLifeCycle lifecycle = new AwaitingLifeCycle();

    MockListener capture = new MockListener();
    lifecycle.addListener(capture);
    MockListener capture2 = new MockListener();
    lifecycle.addListener(capture2);

    lifecycle.event1(null, "foo");
    lifecycle.event1(null, "bar");

    lifecycle.await(150);  // wait for the executor to finish

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

    lifecycle.event1(null, "foo");
    lifecycle.event1(null, "bar");

    lifecycle.await(100); // wait for the executor to finish

    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));
    assertFalse(failing.hasEvent("bar"));
    assertFalse(failing.hasEvent("foo"));


  }


  class MockEvent extends LifeCycleEvent<String,String> {

    public MockEvent(String source, String payload) {
      super(source, payload);
    }

  }

  class MockListener implements LifeCycleListener<MockEvent> {
    public Collection<String> events = new ArrayList<String>();

    public boolean hasEvent(String event) {
      return events.contains(event);
    }

    protected void recordEvent(MockEvent event) {
      events.add(event.getPayload());
    }

    public void event1(MockEvent event) {
      recordEvent(event);
    }


  }

  class MockFailingListener extends MockListener {
    protected void recordEvent(MockEvent event) {
      throw new RuntimeException("fake runtime exception thrown on purpose");
    }
  }


  /**
   * Custom LifeCycle for testing purpose, that can wait until all events are dispatched.
   * Necessary to avoid test failures when the internal executor has not finished executing all listeners.
   * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
   * @version $Revision$
   */
  class AwaitingLifeCycle extends AbstractLifeCycle<MockListener, MockEvent> {

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


    public void event1(String source, String payload) {
      broadcast(new MockEvent(source, payload));
    }


    @Override
    protected void dispatchEvent(MockListener listener, MockEvent event) {
      listener.event1(event);
    }

  }



}
