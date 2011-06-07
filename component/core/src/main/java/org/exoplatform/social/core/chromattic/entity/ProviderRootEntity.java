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
package org.exoplatform.social.core.chromattic.entity;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name="soc:providers")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ProviderRootEntity {

  @OneToMany
  public abstract Map<String, ProviderEntity> getProviders();

  @Create
  public abstract ProviderEntity createProvider();

  public ProviderEntity getProvider(String name) {

    ProviderEntity got = getProviders().get(name);
    if (got == null) {
      got = createProvider();
      getProviders().put(name, got);
    }

    return got;

  }
}
