/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.common.lifecycle;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;

/**
 * SocialChromatticLifecyle is used to manage {@link ChromatticSession} on Social project.
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SocialChromatticLifeCycle extends ChromatticLifeCycle {

  private static final ThreadLocal<ChromatticSession> session = new ThreadLocal<ChromatticSession>();
  private final ThreadLocal providerRoot = new ThreadLocal();
  private final ThreadLocal spaceRoot = new ThreadLocal();

  public static final String SOCIAL_LIFECYCLE_NAME = "soc";

  public ChromatticSession getSession() {
    if (session.get() != null && session.get().getJCRSession().isLive() && !session.get().isClosed()) {
      return session.get();
    }
    else {
      return getChromattic().openSession();
    }
  }

  public SocialChromatticLifeCycle(final InitParams params) {

    super(params);

  }

  @Override
  protected void onOpenSession(final SessionContext context) {
    session.set(context.getSession());
    super.onOpenSession(context);

  }

  @Override
  protected void onCloseSession(final SessionContext context) {
     
    super.onCloseSession(context);
    session.get().close();
    session.remove();
    providerRoot.set(null);
    spaceRoot.set(null);

  }

  public ThreadLocal getProviderRoot() {
    return providerRoot;
  }

  public ThreadLocal getSpaceRoot() {
    return spaceRoot;
  }

}
