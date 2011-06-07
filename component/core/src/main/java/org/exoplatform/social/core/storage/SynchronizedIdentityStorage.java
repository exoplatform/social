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

package org.exoplatform.social.core.storage;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;

import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedIdentityStorage extends IdentityStorage {

  @Override
  public void saveIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.saveIdentity(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.updateIdentity(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.findIdentityById(nodeId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.deleteIdentity(identity);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void loadProfile(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.loadProfile(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.findIdentity(providerId, remoteId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void saveProfile(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.saveProfile(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void updateProfile(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.updateProfile(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesCount(final String providerId) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesCount(providerId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Identity> getIdentitiesByProfileFilter(
      final String providerId, final ProfileFilter profileFilter, final long offset, final long limit,
      final boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
       return super.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByProfileFilterCount(providerId, profileFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public List<Identity> getIdentitiesByFirstCharacterOfName(
      final String providerId, final ProfileFilter profileFilter, final long offset, final long limit,
      final boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      return super.getIdentitiesByFirstCharacterOfName(
          providerId, profileFilter, offset, limit, forceLoadOrReloadProfile);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public String getType(final String nodetype, final String property) {

    boolean created = startSynchronization();
    try {
      return super.getType(nodetype, property);
    }
    finally {
      stopSynchronization(created);
    }

  }

  @Override
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {

    boolean created = startSynchronization();
    try {
      super.addOrModifyProfileProperties(profile);
    }
    finally {
      stopSynchronization(created);
    }

  }
}
