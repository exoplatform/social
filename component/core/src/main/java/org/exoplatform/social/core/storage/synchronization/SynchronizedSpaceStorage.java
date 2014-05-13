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

package org.exoplatform.social.core.storage.synchronization;

import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;

import java.util.List;

/**
 * {@link SynchronizedSpaceStorage} as a decorator to
 * {@link org.exoplatform.social.core.storage.impl.SpaceStorageImpl} for synchronization management.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SynchronizedSpaceStorage extends SpaceStorageImpl {

  /**
   * Constructor.
   *
   * @param identityStorage the identity storage
   */
  public SynchronizedSpaceStorage(final IdentityStorageImpl identityStorage, final ActivityStreamStorage streamStorage) {
    super(identityStorage, streamStorage);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Space getSpaceByDisplayName(final String spaceDisplayName) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaceByDisplayName(spaceDisplayName);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveSpace(final Space space, final boolean isNew) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      super.saveSpace(space, isNew);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void renameSpace(final Space space, final String newDisplayName) throws SpaceStorageException {
    boolean created = startSynchronization();
    try {
      super.renameSpace(space, newDisplayName);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteSpace(final String id) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      super.deleteSpace(id);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMemberSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getMemberSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMemberSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getMemberSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getMemberSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getMemberSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getMemberSpaces(final String userId, final long offset, final long limit)
                                     throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getMemberSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getMemberSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                             final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getMemberSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPendingSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPendingSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPendingSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getPendingSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPendingSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPendingSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPendingSpaces(final String userId, final long offset, final long limit)
                                      throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPendingSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPendingSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                              final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getPendingSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInvitedSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getInvitedSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getInvitedSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getInvitedSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getInvitedSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getInvitedSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getInvitedSpaces(final String userId, final long offset, final long limit)
                                      throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getInvitedSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getInvitedSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                              final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getInvitedSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPublicSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPublicSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPublicSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getPublicSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPublicSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                             final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getPublicSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPublicSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPublicSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getPublicSpaces(final String userId, final long offset, final long limit)
                                     throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getPublicSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAccessibleSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getAccessibleSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public int getVisibleSpacesCount(final String userId, final SpaceFilter spaceFiler) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getVisibleSpacesCount(userId, spaceFiler);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAccessibleSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getAccessibleSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getAccessibleSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getAccessibleSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getAccessibleSpaces(final String userId, final long offset, final long limit)
                                         throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getAccessibleSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getVisibleSpaces(final String userId, final SpaceFilter spaceFilter, final long offset, final long limit)
                                      throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getVisibleSpaces(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getAccessibleSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                                 final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getAccessibleSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getEditableSpacesCount(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getEditableSpacesCount(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getEditableSpacesByFilterCount(final String userId, final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getEditableSpacesByFilterCount(userId, spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getEditableSpaces(final String userId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getEditableSpaces(userId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getEditableSpaces(final String userId, final long offset, final long limit)
                                       throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getEditableSpaces(userId, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getEditableSpacesByFilter(final String userId, final SpaceFilter spaceFilter, final long offset,
                                               final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getEditableSpacesByFilter(userId, spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAllSpacesCount() throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getAllSpacesCount();
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getAllSpaces() throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getAllSpaces();
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAllSpacesByFilterCount(final SpaceFilter spaceFilter) {

    boolean created = startSynchronization();
    try {
      return super.getAllSpacesByFilterCount(spaceFilter);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getSpaces(final long offset, final long limit) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaces(offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Space> getSpacesByFilter(final SpaceFilter spaceFilter, final long offset, final long limit) {

    boolean created = startSynchronization();
    try {
      return super.getSpacesByFilter(spaceFilter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Space getSpaceById(final String id) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaceById(id);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Space getSpaceByPrettyName(final String spacePrettyName) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaceByPrettyName(spacePrettyName);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Space getSpaceByGroupId(final String groupId) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaceByGroupId(groupId);
    }
    finally {
      stopSynchronization(created);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Space getSpaceByUrl(final String url) throws SpaceStorageException {

    boolean created = startSynchronization();
    try {
      return super.getSpaceByUrl(url);
    }
    finally {
      stopSynchronization(created);
    }

  }
  
  @Override
  public void updateSpaceAccessed(String remoteId, Space space) throws SpaceStorageException {
    boolean created = startSynchronization();
    try {
      super.updateSpaceAccessed(remoteId, space);
    }
    finally {
      stopSynchronization(created);
    }
  }
  
  @Override
  public List<Space> getLastAccessedSpace(SpaceFilter filter, int offset, int limit) throws SpaceStorageException {
    boolean created = startSynchronization();
    try {
      return super.getLastAccessedSpace(filter, offset, limit);
    }
    finally {
      stopSynchronization(created);
    }
    
  }

  public List<Space> getLastSpaces(final int limit) {
    boolean created = startSynchronization();
    try {
      return super.getLastSpaces(limit);
    }
    finally {
      stopSynchronization(created);
    }
  }
}
