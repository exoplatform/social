/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

  public static final String SOCIAL_LIFECYCLE_NAME = "soc";

  public ChromatticSession getSession() {
    if (session.get() != null) {
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

  }
}
