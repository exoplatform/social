/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.dao.UserSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.storage.GroupSpaceBindingStorageException;
import org.exoplatform.social.core.storage.api.GroupSpaceBindingStorage;

/**
 * {@link GroupSpaceBindingStorage}
 * implementation.
 */

public class RDBMSGroupSpaceBindingStorageImpl implements GroupSpaceBindingStorage {

    /**
     * Logger
     */
    private static final Log LOG = ExoLogger.getLogger(RDBMSGroupSpaceBindingStorageImpl.class);

    private final org.exoplatform.social.core.jpa.storage.dao.SpaceDAO spaceDAO;

    private GroupSpaceBindingDAO groupSpaceBindingDAO;

    private UserSpaceBindingDAO userSpaceBindingDAO;

    public RDBMSGroupSpaceBindingStorageImpl(GroupSpaceBindingDAO groupSpaceBindingDAO, UserSpaceBindingDAO userSpaceBindingDAO, SpaceDAO spaceDAO) {
        this.groupSpaceBindingDAO = groupSpaceBindingDAO;
        this.userSpaceBindingDAO = userSpaceBindingDAO;
        this.spaceDAO = spaceDAO;
    }

    public List<GroupSpaceBinding> findSpaceBindings(String spaceId, String role) throws GroupSpaceBindingStorageException {
        return buildBindingListFromEntities(groupSpaceBindingDAO.findSpaceBindings(Long.parseLong(spaceId), role));
    }

    @ExoTransactional
    public void saveBinding(GroupSpaceBinding binding, boolean isNew) throws GroupSpaceBindingStorageException {
        if (isNew) {
            groupSpaceBindingDAO.create(buildEntityBindingFrom(binding));
        } else {
            Long id = binding.getId();
            GroupSpaceBindingEntity entity = groupSpaceBindingDAO.find(id);
            if (entity != null) {
                entity = buildEntityBindingFrom(binding);
                groupSpaceBindingDAO.update(entity);
            }
        }
    }

    @ExoTransactional
    public void deleteBinding(String id) throws GroupSpaceBindingStorageException {
        groupSpaceBindingDAO.delete(groupSpaceBindingDAO.find(Long.parseLong(id)));
    }

    /**
     * Fills {@link GroupSpaceBinding}'s properties to
     * {@link GroupSpaceBindingEntity}'s.
     *
     * @param entity the GroupSpaceBinding entity
     */
    private GroupSpaceBinding fillBindingFromEntity(GroupSpaceBindingEntity entity) {
        if (entity == null) {
            return null;
        }
        GroupSpaceBinding groupSpaceBinding = new GroupSpaceBinding();
        groupSpaceBinding.setId(entity.getId());
        groupSpaceBinding.setGroup(entity.getGroup());
        groupSpaceBinding.setGroupRole(entity.getGroupRole());
        String spaceId = Long.toString(entity.getSpace().getId());
        groupSpaceBinding.setSpaceId(spaceId);
        groupSpaceBinding.setSpaceRole(entity.getSpaceRole());
        return groupSpaceBinding;
    }

    /**
     * build {@link GroupSpaceBinding}'s list from {@link GroupSpaceBindingEntity}'s
     * list.
     *
     * @param entities the list of entities
     */
    private List<GroupSpaceBinding> buildBindingListFromEntities(List<GroupSpaceBindingEntity> entities) {
        List<GroupSpaceBinding> groupSpaceBindings = new LinkedList<>();
        if (groupSpaceBindings != null) {
            for (GroupSpaceBindingEntity entity : entities) {
                GroupSpaceBinding groupSpaceBinding = fillBindingFromEntity(entity);
                groupSpaceBindings.add(groupSpaceBinding);
            }
        }
        return groupSpaceBindings;
    }

    /**
     * build {@link GroupSpaceBindingEntity} from {@link GroupSpaceBinding} object
     * list.
     *
     * @param groupSpaceBinding the GroupSpaceBinding object
     */
    private GroupSpaceBindingEntity buildEntityBindingFrom(GroupSpaceBinding groupSpaceBinding) {
        LOG.info("Generating identity "+ groupSpaceBinding.getId()+" " + groupSpaceBinding.getGroup()+ " " + groupSpaceBinding.getGroupRole()+  " "+ groupSpaceBinding.getSpaceId() + " " + groupSpaceBinding.getSpaceRole());
        GroupSpaceBindingEntity groupSpaceBindingEntity = new GroupSpaceBindingEntity();
        groupSpaceBindingEntity.setGroup(groupSpaceBinding.getGroup());
        groupSpaceBindingEntity.setGroupRole(groupSpaceBinding.getGroupRole());
        Long spaceId = Long.parseLong(groupSpaceBinding.getSpaceId());
        SpaceEntity entity = spaceDAO.find(spaceId);
        groupSpaceBindingEntity.setSpace(entity);
        groupSpaceBindingEntity.setSpaceRole(groupSpaceBinding.getSpaceRole());
        return groupSpaceBindingEntity;
    }

}
