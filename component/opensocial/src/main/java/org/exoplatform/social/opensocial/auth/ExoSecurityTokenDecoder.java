/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.opensocial.auth;

import java.util.Map;

import org.apache.shindig.auth.BasicSecurityTokenDecoder;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.SecurityTokenException;
import org.apache.shindig.config.ContainerConfig;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ExoSecurityTokenDecoder implements SecurityTokenDecoder {

  private static final String        SECURITY_TOKEN_TYPE = "gadgets.securityTokenType";

  private final SecurityTokenDecoder decoder;

  @Inject
  public ExoSecurityTokenDecoder(ContainerConfig config) {

    String tokenType = config.getString(ContainerConfig.DEFAULT_CONTAINER, SECURITY_TOKEN_TYPE);
    if ("insecure".equals(tokenType)) {
      decoder = new BasicSecurityTokenDecoder();
    } else if ("secure".equals(tokenType)) {
      decoder = new ExoBlobCrypterSecurityDecoder(config);
    } else {
      throw new RuntimeException("Unknown security token type specified in "
          + ContainerConfig.DEFAULT_CONTAINER + " container configuration. " + SECURITY_TOKEN_TYPE
          + ": " + tokenType);
    }
  }


  public SecurityToken createToken(Map<String, String> tokenParameters) throws SecurityTokenException {
    return decoder.createToken(tokenParameters);
  }

}
