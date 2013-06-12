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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.api.notification;

import java.util.Collection;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

public abstract class MailConnector extends BaseComponentPlugin {
  private String connectType; // Connector type
  private String connectName; // Connector display name


  public MailConnector(InitParams initParams) {
    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.connectType = param.getProperty("connectorType");
    this.connectName = param.getProperty("displayName");
  }
  
  /**
   * @return the connectType
   */
  public String getConnectType() {
    return connectType;
  }

  /**
   * @return the connectName
   */
  public String getConnectName() {
    return connectName;
  }

  public abstract Collection<EmailMessage> emails() ;

}
