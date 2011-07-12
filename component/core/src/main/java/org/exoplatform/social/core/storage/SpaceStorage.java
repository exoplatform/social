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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceListEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceRef;
import org.exoplatform.social.core.chromattic.entity.SpaceRootEntity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.QueryFunction;
import org.exoplatform.social.core.storage.query.WhereExpression;

/**
 * Space storage layer.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceStorage extends AbstractStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(SpaceStorage.class);

  /**
   * The identity storage
   */
  private final IdentityStorage identityStorage;

  /**
   * Constructor.
   *
   * @param identityStorage the identity storage
   */
  public SpaceStorage(IdentityStorage identityStorage) {
   this.identityStorage = identityStorage;
 }

  /**
   * Fills {@link Space}'s properties to {@link SpaceEntity}'s.
   *
   * @param entity the space entity from chromattic
   * @param space  the space pojo for services
   */
  private void fillSpaceFromEntity(SpaceEntity entity, Space space) {

    space.setApp(entity.getApp());
    space.setId(entity.getId());
    space.setPrettyName(entity.getPrettyName());
    space.setDisplayName(entity.getDisplayName());
    space.setRegistration(entity.getRegistration());
    space.setDescription(entity.getDescription());
    space.setType(entity.getType());
    space.setVisibility(entity.getVisibility());
    space.setPriority(entity.getPriority());
    space.setGroupId(entity.getGroupId());
    space.setUrl(entity.getURL());
    space.setMembers(entity.getMembersId());
    space.setManagers(entity.getManagerMembersId());
    space.setPendingUsers(entity.getPendingMembersId());
    space.setInvitedUsers(entity.getInvitedMembersId());

    if (entity.getHasAvatar()) {
      space.setAvatarUrl(LinkProvider.buildAvatarImageUri(space.getPrettyName()));
    }

  }

  /**
   * Fills {@link SpaceEntity}'s properties from {@link Space}'s.
   *
   * @param space the space pojo for services
   * @param entity the space entity from chromattic
   */
  private void fillEntityFromSpace(Space space, SpaceEntity entity) {

    entity.setApp(space.getApp());
    entity.setPrettyName(space.getPrettyName());
    entity.setDisplayName(space.getDisplayName());
    entity.setRegistration(space.getRegistration());
    entity.setDescription(space.getDescription());
    entity.setType(space.getType());
    entity.setVisibility(space.getVisibility());
    entity.setPriority(space.getPriority());
    entity.setGroupId(space.getGroupId());
    entity.setURL(space.getUrl());
    entity.setMembersId(space.getMembers());
    entity.setManagerMembersId(space.getManagers());
    entity.setPendingMembersId(space.getPendingUsers());
    entity.setInvitedMembersId(space.getInvitedUsers());
    entity.setHasAvatar(space.getAvatarAttachment() != null || space.getAvatarUrl() != null);

  }

  /**
   * The reference types.
   */
  private enum RefType {
    MEMBER() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getSpaces();
      }},
    MANAGER() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getManagerSpaces();
      }},
    PENDING() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getPendingSpaces();
      }},
    INVITED() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getInvitedSpaces();
      }};

    public abstract SpaceListEntity refsOf(IdentityEntity identityEntity);
  }

  private class UpdateContext {
    private String[] added;
    private String[] removed;

    private UpdateContext(String[] added, String[] removed) {
      this.added = added;
      this.removed = removed;
    }

    public String[] getAdded() {
      return added;
    }

    public String[] getRemoved() {
      return removed;
    }
  }

  private String[] sub(String[] l1, String[] l2) {

    if (l1 == null) {
      return new String[]{};
    }

    if (l2 == null) {
      return l1;
    }

    List<String> l = new ArrayList(Arrays.asList(l1));
    l.removeAll(Arrays.asList(l2));
    return l.toArray(new String[]{});
  }

  private void createRefs(SpaceEntity spaceEntity, Space space) throws NodeNotFoundException {

    String[] removedMembers = sub(spaceEntity.getMembersId(), space.getMembers());
    String[] removedManagers = sub(spaceEntity.getManagerMembersId(), space.getManagers());
    String[] removedInvited = sub(spaceEntity.getInvitedMembersId(), space.getInvitedUsers());
    String[] removedPending = sub(spaceEntity.getPendingMembersId(), space.getPendingUsers());

    String[] addedMembers = sub(space.getMembers(), spaceEntity.getMembersId());
    String[] addedManagers = sub(space.getManagers(), spaceEntity.getManagerMembersId());
    String[] addedInvited = sub(space.getInvitedUsers(), spaceEntity.getInvitedMembersId());
    String[] addedPending = sub(space.getPendingUsers(), spaceEntity.getPendingMembersId());

    manageRefList(new UpdateContext(addedMembers, removedMembers), spaceEntity, RefType.MEMBER);
    manageRefList(new UpdateContext(addedManagers, removedManagers), spaceEntity, RefType.MANAGER);
    manageRefList(new UpdateContext(addedInvited, removedInvited), spaceEntity, RefType.INVITED);
    manageRefList(new UpdateContext(addedPending, removedPending), spaceEntity, RefType.PENDING);

  }

  private void manageRefList(UpdateContext context, SpaceEntity spaceEntity, RefType type) {

    if (context.getAdded() != null) {
      for (String userName : context.getAdded()) {
        try {
          IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userName);
          SpaceListEntity listRef = type.refsOf(identityEntity);
          SpaceRef ref = listRef.getRef(spaceEntity.getName());
          ref.setSpaceRef(spaceEntity);
        }
        catch (NodeNotFoundException e) {
          LOG.warn(e.getMessage(), e);
        }
      }

      for (String userName : context.getRemoved()) {
        try {
          IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userName);
          SpaceListEntity listRef = type.refsOf(identityEntity);
          SpaceRef ref = listRef.getRef(spaceEntity.getName());
          getSession().remove(ref);
        }
        catch (NodeNotFoundException e) {
          LOG.warn(e.getMessage(), e);
        }
      }
    }
  }

  private boolean validateFilter(SpaceFilter filter) {

    if (filter == null) {
      return false;
    }

    if (filter.getSpaceNameSearchCondition() != null &&
        filter.getSpaceNameSearchCondition().length() != 0) {
      return isValidInput(filter.getSpaceNameSearchCondition());
    }
    else if (!Character.isDigit(filter.getFirstCharacterOfSpaceName())) {
      return true;
    }
    return false;

  }

  private SpaceEntity createSpace(Space space) throws SpaceStorageException {

    SpaceRootEntity spaceRootEntity = getSpaceRoot();
    SpaceEntity spaceEntity = spaceRootEntity.getSpace(space.getPrettyName());
    space.setId(spaceEntity.getId());

    return spaceEntity;

  }

  private SpaceEntity _saveSpace(Space space) throws NodeNotFoundException {

    return _findById(SpaceEntity.class, space.getId());

  }

  private void _applyFilter(WhereExpression whereExpression, SpaceFilter spaceFilter) {

    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    char firstCharacterOfName = spaceFilter.getFirstCharacterOfSpaceName();

    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      if (this.isValidInput(spaceNameSearchCondition)) {

        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);

        if (spaceNameSearchCondition.indexOf(PERCENT_STR) >= 0) {
          whereExpression.startGroup();
          whereExpression
              .like(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .like(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
        else {
          whereExpression.startGroup();
          whereExpression
              .contains(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .contains(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
      }
    }
    else if (!Character.isDigit(firstCharacterOfName)) {
      String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
      String firstCharacterOfNameLowerCase = firstCharacterOfNameString.toLowerCase() + PERCENT_STR;
      whereExpression
          .like(whereExpression.callFunction(QueryFunction.LOWER, SpaceEntity.name), firstCharacterOfNameLowerCase);
    }
  }

  private boolean isValidInput(String input) {
    if (input == null) {
      return false;
    }
    String cleanString = input.replaceAll("\\*", "");
    cleanString = cleanString.replaceAll("\\%", "");
    if (cleanString.length() > 0 && Character.isDigit(cleanString.charAt(0))) {
       return false;
    } else if (cleanString.length() == 0) {
      return false;
    }
    return true;
  }

  private String processSearchCondition(String searchCondition) {
    StringBuffer searchConditionBuffer = new StringBuffer();
    if (searchCondition.indexOf(ASTERISK_STR) < 0 && searchCondition.indexOf(PERCENT_STR) < 0) {
      if (searchCondition.charAt(0) != ASTERISK_CHAR) {
        searchConditionBuffer.append(ASTERISK_STR).append(searchCondition);
      }
      if (searchCondition.charAt(searchCondition.length() - 1) != ASTERISK_CHAR) {
        searchConditionBuffer.append(ASTERISK_STR);
      }
    } else {
      searchCondition = searchCondition.replace(ASTERISK_STR, PERCENT_STR);
      searchConditionBuffer.append(PERCENT_STR).append(searchCondition).append(PERCENT_STR);
    }
    return searchConditionBuffer.toString();
  }

  /*
    Filter query
   */
  
  private Query<SpaceEntity> _getSpacesByFilterQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    _applyFilter(whereExpression, spaceFilter);

    if (userId != null) {
      whereExpression
          .and()
          .equals(SpaceEntity.membersId, userId);
    }

    if (whereExpression.toString().length() == 0) {
      return builder.get();
    }
    else {
      return builder.where(whereExpression.toString()).get();
    }

  }

  private Query<SpaceEntity> getAccessibleSpacesByFilterQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (validateFilter(spaceFilter)) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
      whereExpression.startGroup();
    }

    whereExpression
        .equals(SpaceEntity.membersId, userId)
        .or()
        .equals(SpaceEntity.managerMembersId, userId);


    whereExpression.endAllGroup();

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> getPublicSpacesQuery(String userId) {
    return getPublicSpacesQuery(userId, null);
  }

  private Query<SpaceEntity> getPublicSpacesQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (validateFilter(spaceFilter)) {
      _applyFilter(whereExpression, spaceFilter);
        whereExpression.and();
    }

      builder.where(whereExpression
          .not().equals(SpaceEntity.membersId, userId)
          .and().not().equals(SpaceEntity.managerMembersId, userId)
          .and().not().equals(SpaceEntity.invitedMembersId, userId)
          .and().not().equals(SpaceEntity.pendingMembersId, userId)
          .toString()
      );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> getPendingSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (validateFilter(spaceFilter)) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.pendingMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> getInvitedSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (validateFilter(spaceFilter)) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.invitedMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> getEditableSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (validateFilter(spaceFilter)) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.managerMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> getSpacesByFilterQuery(SpaceFilter spaceFilter) {
    return _getSpacesByFilterQuery(null, spaceFilter);
  }

  /*
    Public
   */

  /**
   * Gets a space by its display name.
   *
   * @param spaceDisplayName
   * @return the space with spaceDisplayName that matches the spaceDisplayName input.
   * @since  1.2.0-GA
   * @throws SpaceStorageException
   */
  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    Space space = null;

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression.equals(SpaceEntity.displayName, spaceDisplayName);

    QueryResult<SpaceEntity> result = builder.where(whereExpression.toString()).get().objects();
    
    if (result.hasNext()) {
      space = new Space();
      fillSpaceFromEntity(result.next(), space);
    }

    return space;
  }

  /**
   * Saves a space. If isNew is true, creates new space. If not only updates space
   * an saves it.
   *
   * @param space
   * @param isNew
   * @throws SpaceStorageException
   */
  public void saveSpace(Space space, boolean isNew) throws SpaceStorageException {

    SpaceEntity entity;

    try {
      
      if (isNew) {
        entity = createSpace(space);
      }
      else {
        entity = _saveSpace(space);
      }

      //
      createRefs(entity, space);
      fillEntityFromSpace(space, entity);

      //
      getSession().save();

      //
      LOG.debug(String.format(
          "Space %s (%s) saved",
          space.getPrettyName(),
          space.getId()
      ));

    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE, e.getMessage(), e);
    }

  }

  /**
   * Deletes a space by space id.
   *
   * @param id
   * @throws SpaceStorageException
   */
  public void deleteSpace(String id) throws SpaceStorageException {

    String name;

    //
    try {
      name = _findById(SpaceEntity.class, id).getPrettyName();
    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_DELETE_SPACE, e.getMessage());
    }

    //
    _removeById(SpaceEntity.class, id);

    //
    getSession().save();

    //
    LOG.debug(String.format(
        "Space %s removed",
        name)
    );
  }

  /*
    Member spaces
   */

  /**
   * Gets the count of the spaces that a user has the "member" role.
   *
   * @param userId
   * @return the count of the member spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    try {
       return identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId).getSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e){
       return 0;
    }
  }

  /**
   * Gets the count of the spaces which user has "member" role by filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getSpacesByFilterQuery(userId, spaceFilter).objects().size();
  }

  /**
   * Gets the spaces that a user has the "member" role.
   *
   * @param userId
   * @return a list of the member spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {

    try {

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);

      List<Space> spaces = new ArrayList<Space>();
      for (SpaceRef space : identityEntity.getSpaces().getRefs().values()) {

        Space newSpace = new Space();
        fillSpaceFromEntity(space.getSpaceRef(), newSpace);
        spaces.add(newSpace);
      }

      return spaces;

    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES, e.getMessage(), e);
    }
  }

  /**
   * Gets the spaces that a user has the "member" role with offset, limit.
   *
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the member spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

          Space space = new Space();
          fillSpaceFromEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      return spaces;
    }

    return spaces;
  }

  /**
   * Gets the member spaces of the user id by the filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getSpacesByFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;

  }

  /*
    Pending spaces
   */

  /**
   * Gets the count of the pending spaces of the userId.
   *
   * @param userId
   * @return the count of the pending spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();
      return spaceEntities.size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
  }

  /**
   * Gets the count of the pending spaces of the user by space filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getPendingSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  /**
   * Gets a user's pending spaces and that the user can revoke that request.
   *
   * @param userId
   * @return a list of the pending spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();

      for (SpaceRef ref : spaceEntities) {

        Space space = new Space();
        fillEntityFromSpace(space, ref.getSpaceRef());
        spaces.add(space);
      }
    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;
  }

  /**
   * Gets a user's pending spaces and that the user can revoke that request with offset, limit.
   *
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the pending spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

          Space space = new Space();
          fillSpaceFromEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;
  }

  /**
   * Gets the pending spaces of the user by space filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getPendingSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Invited spaces
   */

  /**
   * Gets the count of the invited spaces of the userId.
   *
   * @param userId
   * @return the count of the invited spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {

    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();
      return spaceEntities.size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }

  }

  /**
   * Gets the count of the invited spaces of the user by filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {

    if (validateFilter(spaceFilter)) {
      return getInvitedSpacesFilterQuery(userId, spaceFilter).objects().size();
    }
    else {
      return 0;
    }
  }

  /**
   * Gets a user's invited spaces and that user can accept or deny the request.
   *
   * @param userId
   * @return a list of the invited spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();

      for (SpaceRef ref : spaceEntities) {

        Space space = new Space();
        fillEntityFromSpace(space, ref.getSpaceRef());
        spaces.add(space);
      }
    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;
  }

  /**
   * Gets a user's invited spaces and that user can accept or deny the request with offset, limit.
   *
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the invited spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

          Space space = new Space();
          fillSpaceFromEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;
  }

  /**
   * Gets the invited spaces of the user by space filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getInvitedSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Public spaces
   */

  /**
   * Gets the count of the public spaces of the userId.
   *
   * @param userId
   * @return the count of the spaces in which the user can request to join
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPublicSpacesCount(String userId) throws SpaceStorageException {
    return getPublicSpacesQuery(userId).objects().size();
  }

  /**
   * Gets the count of the public spaces of the user by space filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getPublicSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    if (validateFilter(spaceFilter)) {
      return getPublicSpacesQuery(userId, spaceFilter).objects().size();
    }
    else {
      return 0;
    }
  }

  /**
   * Gets the public spaces of the user by filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    try {
      identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
    }
    catch (NodeNotFoundException e) {
      userId = null;
    }

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getPublicSpacesQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /**
   * Gets a user's public spaces and that user can request to join.
   *
   * @param userId
   * @return spaces list in which the user can request to join.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpaces(String userId) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getPublicSpacesQuery(userId).objects();

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /**
   * Gets a user's public spaces and that user can request to join with offset, limit.
   *
   * @param userId
   * @param offset
   * @param limit
   * @return spaces list in which the user can request to join with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getPublicSpacesQuery(userId).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Accessible spaces
   */

  /**
   * Gets the count of the accessible spaces of the userId.
   *
   * @param userId
   * @return the count of the accessible spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    return getAccessibleSpacesByFilterQuery(userId, null).objects().size();
  }

  /**
   * Gets the count of the accessible spaces of the user by filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getAccessibleSpacesByFilterQuery(userId, spaceFilter).objects().size();
  }

  /**
   * Gets the spaces of a user which that user has the "member" role or edit permission.
   *
   * @param userId the userId
   * @return a list of the accessible spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getAccessibleSpacesByFilterQuery(userId, null).objects();

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /**
   * Gets the spaces of a user which that user has "member" role or edit permission with offset, limit.
   *
   * @param userId the userId
   * @param offset
   * @param limit
   * @return a list of the accessible space with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getAccessibleSpacesByFilterQuery(userId, null).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /**
   * Gets the accessible spaces of the user by filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getAccessibleSpacesByFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Editable spaces    
   */

  /**
   * Gets the count of the spaces of a user which that user has the edit permission.
   *
   * @param userId
   * @return the count of the editable spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      return identityEntity.getManagerSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
  }

  /**
   * Gets the count of the editable spaces of the user by filter.
   *
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return getEditableSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  /**
   * Gets the spaces of a user which that user has the edit permission.
   *
   * @param userId
   * @return a list of the editable spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getManagerSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          fillSpaceFromEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);
        }
      }

    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;

  }

  /**
   * Gets the spaces of a user which that user has the edit permission with offset, limit.
   *
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getManagerSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

          Space space = new Space();
          fillSpaceFromEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    return spaces;
  }

  /**
   * Gets the editable spaces of the user by filter with offset, limit.
   *
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = getEditableSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    All spaces
   */

  /**
   * Gets the count of the spaces.
   *
   * @return the count of all spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAllSpacesCount() throws SpaceStorageException {

    // TODO : use property to improve the perfs

    return getSpaceRoot().getSpaces().size();

  }

  /**
   * Gets all the spaces. By the default get the all spaces with OFFSET = 0, LIMIT = 200;
   *
   * @throws SpaceStorageException
   * @return the list of all spaces
   */
  public List<Space> getAllSpaces() throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    for (SpaceEntity spaceEntity : getSpaceRoot().getSpaces().values()) {
      Space space = new Space();
      fillSpaceFromEntity(spaceEntity, space);
      spaces.add(space);
    }

    return spaces;

  }

  /**
   * Gets the count of the spaces which are searched by space filter.
   *
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {

    if (validateFilter(spaceFilter)) {
      return getSpacesByFilterQuery(spaceFilter).objects().size();
    }
    else {
      return 0;
    }

  }


  /*
    Get spaces
   */

  /**
   * Gets the spaces with offset, limit.
   *
   * @param offset
   * @param limit
   * @return the list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    int i = 0;

    Iterator<SpaceEntity> it = getSpaceRoot().getSpaces().values().iterator();
    _skip(it, offset);
    
    while (it.hasNext()) {

      SpaceEntity spaceEntity = it.next();

      Space space = new Space();
      fillSpaceFromEntity(spaceEntity, space);
      spaces.add(space);

      if (++i >= limit) {
        break;
      }

    }

    return spaces;

  }

  /**
   * Gets the spaces by space filter with offset, limit.
   *
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @throws SpaceStorageException
   */
  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    if (!validateFilter(spaceFilter)) {
      return spaces;
    }

    //
    QueryResult<SpaceEntity> results = getSpacesByFilterQuery(spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      fillSpaceFromEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /**
   * Gets a space by its space id.
   *
   * @param id
   * @return space with id specified
   * @throws SpaceStorageException
   */
  public Space getSpaceById(String id) throws SpaceStorageException {

    try {

      SpaceEntity spaceEntity = _findById(SpaceEntity.class, id);

      Space space = new Space();

      fillSpaceFromEntity(spaceEntity, space);

      return space;

    }
    catch (NodeNotFoundException e) {
      return null;
    }

  }

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName
   * @return the space with spacePrettyName that matches spacePrettyName input.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {

    try {

      SpaceEntity entity = _findByPath(SpaceEntity.class, String.format("/production/soc:spaces/soc:%s", spacePrettyName));

      Space space = new Space();
      fillSpaceFromEntity(entity, space);

      return space;

    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets a space by its associated group id.
   *
   * @param  groupId
   * @return the space that has group id matching the groupId string input.
   * @throws SpaceStorageException
   * @since  1.2.0-GA
   */
  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {

    // TODO : avoid JCR query ?

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    builder.where(whereExpression.equals(SpaceEntity.groupId, groupId).toString());

    QueryResult<SpaceEntity> result = builder.get().objects();

    if (result.hasNext()) {
      SpaceEntity entity =  result.next();
      Space space = new Space();

      fillSpaceFromEntity(entity, space);

      return space;
    }
    else {
      return null;
    }

  }

  /**
   * Gets a space by its url.
   *
   * @param url
   * @return the space with string url specified
   * @throws SpaceStorageException
   */
  public Space getSpaceByUrl(String url) throws SpaceStorageException {

    // TODO : avoid JCR query ?

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);

    if (url != null) {
      WhereExpression whereExpression = new WhereExpression();
      whereExpression.equals(SpaceEntity.url, url);
      builder.where(whereExpression.toString());
    }

    QueryResult<SpaceEntity> result = builder.get().objects();

    if (result.hasNext()) {

      Space space = new Space();
      SpaceEntity entity =  builder.get().objects().next();

      fillSpaceFromEntity(entity, space);

      return space;

    }
    else {
      return null;
    }

  }

}