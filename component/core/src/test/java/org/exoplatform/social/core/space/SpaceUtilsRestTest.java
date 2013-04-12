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
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SpaceUtilsRestTest extends AbstractCoreTest {

  private final Log LOG = ExoLogger.getLogger(SpaceUtilsRestTest.class);
  private IdentityStorage identityStorage;
  private UserPortalConfigService userPortalConfigSer_;
  /** . */
  private POMSessionManager mgr = null;
  /** . */
  private Authenticator authenticator = null;
  
  private List<Identity> tearDownUserList = null;
  private Identity root = null;
  
  @Override
  public void setUp() throws Exception {
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    mgr = (POMSessionManager)getContainer().getComponentInstanceOfType(POMSessionManager.class);
    authenticator = (Authenticator)getContainer().getComponentInstanceOfType(Authenticator.class);
    
    root = new Identity(OrganizationIdentityProvider.NAME, "root");
    
    identityStorage.saveIdentity(root);
    tearDownUserList = new ArrayList<Identity>();
    tearDownUserList.add(root);
  }
  
  @Override
  public void tearDown() throws Exception {
    for (Identity identity : tearDownUserList) {
      identityStorage.deleteIdentity(identity);
    }
  }
  
  public void testGetUserPortalForRest() throws Exception {
    new UnitTest() {
      @Override
      protected void execute() throws Exception {
        UserPortal userPortal = SpaceUtils.getUserPortalForRest();
        assertNotNull(userPortal);
      }
      
    }.execute(root.getRemoteId());
  }
  
  public void testGetUserPortal() throws Exception {
    new UnitTest() {
      @Override
      protected void execute() throws Exception {
        UserPortal userPortal = SpaceUtils.getUserPortal();
        assertNotNull(userPortal);
      }
      
    }.execute(root.getRemoteId());
  }
  
  private abstract class UnitTest {

    /** . */
    private POMSession mopSession;

    protected final void execute(String userId) {
      Throwable failure = null;

      //
      begin();

      //
      ConversationState conversationState = null;
      if (userId != null) {
        try {
          conversationState = new ConversationState(authenticator.createIdentity(userId));
        } catch (Exception e) {
          failure = e;
        }
      }

      //
      if (failure == null) {
        // Clear cache for test
        mgr.clearCache();

        //
        mopSession = mgr.openSession();

        //
        ConversationState.setCurrent(conversationState);
        try {
          execute();
        } catch (Exception e) {
          failure = e;
        } finally {
          ConversationState.setCurrent(null);
          mopSession.close(false);
          end();
        }
      }

      // Report error as a junit assertion failure
      if (failure != null) {
        AssertionFailedError err = new AssertionFailedError();
        err.initCause(failure);
        throw err;
      }
    }


    protected abstract void execute() throws Exception;
  }
}
