package org.exoplatform.social.space.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.exoplatform.social.space.lifecycle.AbstractLifeCycle;

import junit.framework.TestCase;

public class TestAbstractLifeCycle extends TestCase {

  
  public void testSimpleBroadcast() {
    
    SampleLifeCycle lifecycle = new SampleLifeCycle();
    
    SampleListener capture = new SampleListener();
    lifecycle.addListener(capture);
    SampleListener capture2 = new SampleListener();
    lifecycle.addListener(capture2);
    
    lifecycle.eventOccured("foo");
    lifecycle.eventOccured("bar");
    
    lifecycle.await(100);  // wait for the executor to finish
    
    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));

    
  }
  
  public void testBroadcastWithFailingListener() {
    
    SampleLifeCycle lifecycle = new SampleLifeCycle();
    SampleListener capture = new SampleListener();
    lifecycle.addListener(capture);
    FailingListener failing = new FailingListener();
    lifecycle.addListener(failing);
    SampleListener capture2 = new SampleListener();
    lifecycle.addListener(capture2);
    
    lifecycle.eventOccured("foo");
    lifecycle.eventOccured("bar");
    
    lifecycle.await(100); // wait for the executor to finish
    
    assertTrue(capture.hasEvent("bar"));
    assertTrue(capture.hasEvent("foo"));
    assertTrue(capture2.hasEvent("bar"));
    assertTrue(capture2.hasEvent("foo"));
    assertFalse(failing.hasEvent("bar"));
    assertFalse(failing.hasEvent("foo"));

    
  }
  
  
  class SampleListener {
    public Collection<String> events = new ArrayList<String>();
    
    public boolean hasEvent(String event) {
      return events.contains(event);
    }
    
    public void onEvent(String event) {
      events.add(event);
    }
  }
  
  class FailingListener extends SampleListener {
    public void onEvent(String event) {
      throw new RuntimeException();
    }
  }
  
  class SampleLifeCycle extends AbstractLifeCycle<SampleListener, String> {
  
    public void await(long milisconds) {

        try {
          executor.awaitTermination(milisconds, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          // ignore
        }
      
    }
    
    public void eventOccured(String event) {
      broadcast(event);
    }
    
    @Override
    protected void dispatchEvent(SampleListener listener, String event) {
      listener.onEvent(event);
    }
    
  }
  

  
}
