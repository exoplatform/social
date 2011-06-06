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

package org.exoplatform.social.core.storage;

import junit.framework.TestCase;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class AbstractStorageTestCase extends AbstractCoreTest {

  public void testChrommaticManager() throws Exception {
    PortalContainer container = PortalContainer.getInstance();
    assertNotNull(container);
    ChromatticManager chromatticManager = (ChromatticManager)container.getComponent(ChromatticManager.class);
    assertNotNull(chromatticManager);
    ChromatticLifeCycle lifeCycle = chromatticManager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    assertNotNull(lifeCycle);
  }
}
