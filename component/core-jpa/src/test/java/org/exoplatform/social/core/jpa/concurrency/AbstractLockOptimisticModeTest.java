/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.concurrency;

import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 28, 2015  
 */
public class AbstractLockOptimisticModeTest extends BaseCoreTest {
  
  protected ExoSocialActivity createdActivity = null;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    init();
  }
  
  @Override
  protected void tearDown() throws Exception {
    createdActivity = null;
    super.tearDown();
  }
  
  public void init() {
    doInTransaction(new TransactionCallable<Void>() {
      @Override
      public Void execute() {
        createdActivity = oneOfActivity("john post activity", johnIdentity, false, true);
        return null;
      }
    });
  }
  
}
