package org.exoplatform.social;

import org.gatein.pc.api.invocation.InvocationException;

public interface Invocation {

  Object invokeNext() throws InvocationException;

}
