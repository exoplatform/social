/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.commons.search.service;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 29, 2013  
 */
public class MockUserPortalConfigService  extends UserPortalConfigService{
  
  public MockUserPortalConfigService(UserACL userACL,
                                     DataStorage storage,
                                     OrganizationService orgService,
                                     NavigationService navService,
                                     DescriptionService descriptionService,
                                     PageService pageService,
                                     InitParams params) throws Exception {
    super(userACL, storage, orgService, navService, descriptionService, pageService, params);
    // TODO Auto-generated constructor stub
  }

  @Override
  public List<String> getAllPortalNames() throws Exception {
    // TODO Auto-generated method stub
    return Arrays.asList("intranet","acme");
  }

}
