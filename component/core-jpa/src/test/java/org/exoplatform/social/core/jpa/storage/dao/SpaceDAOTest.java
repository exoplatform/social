/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.social.core.jpa.storage.entity.AppEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

public class SpaceDAOTest extends BaseCoreTest {
  private SpaceDAO spaceDAO;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    spaceDAO = getService(SpaceDAO.class);
  }

  @Override
  public void tearDown() throws Exception {
    spaceDAO.deleteAll();
    super.tearDown();
  }

  public void testSaveSpace() throws Exception {
    SpaceEntity spaceEntity = createSpace();

    spaceDAO.create(spaceEntity);

    end();
    begin();

    SpaceEntity result = spaceDAO.find(spaceEntity.getId());
    assertSpace(spaceEntity, result);
  }

  public void testGetSpace() throws Exception {
    SpaceEntity spaceEntity = createSpace();

    spaceDAO.create(spaceEntity);

    end();
    begin();

    SpaceEntity result = spaceDAO.getSpaceByDisplayName(spaceEntity.getDisplayName());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByGroupId(spaceEntity.getGroupId());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByPrettyName(spaceEntity.getPrettyName());
    assertSpace(spaceEntity, result);
    
    result = spaceDAO.getSpaceByURL(spaceEntity.getUrl());
    assertSpace(spaceEntity, result);
  }
  
  public void testGetLastSpace() throws Exception {
    SpaceEntity space1 = createSpace();
    spaceDAO.create(space1);
    SpaceEntity space2 = createSpace();
    spaceDAO.create(space2);

    end();
    begin();
    
    List<SpaceEntity> result = spaceDAO.getLastSpaces(1);
    assertEquals(1, result.size());
    assertSpace(space2, result.iterator().next());
  }

  private SpaceEntity createSpace() {
    SpaceEntity spaceEntity = new SpaceEntity();    
    spaceEntity.setApp(createApp());
    spaceEntity.setAvatarLastUpdated(new Date());
    spaceEntity.setDescription("testDesc");
    spaceEntity.setDisplayName("testDisplayName");
    spaceEntity.setGroupId("testGroupId");
    spaceEntity.setPrettyName("testPrettyName");
    spaceEntity.setPriority(SpaceEntity.PRIORITY.HIGH);
    spaceEntity.setRegistration(SpaceEntity.REGISTRATION.OPEN);
    spaceEntity.setUrl("testUrl");
    spaceEntity.setVisibility(SpaceEntity.VISIBILITY.PRIVATE);
    spaceEntity.setAvatarLastUpdated(new Date());

    SpaceMemberEntity mem = new SpaceMemberEntity();
    mem.setSpace(spaceEntity);
    mem.setStatus(SpaceMemberEntity.Status.PENDING);
    mem.setUserId("root");
    spaceEntity.getMembers().add(mem);
    return spaceEntity;
  }

  private Set<AppEntity> createApp() {
    Set<AppEntity> apps = new HashSet<>();
    AppEntity app = new AppEntity();
    app.setAppId("appId");
    app.setAppName("appName");
    app.setRemovable(true);
    app.setStatus(AppEntity.Status.ACTIVE);
    apps.add(app);
    return apps;
  }

  private void assertSpace(SpaceEntity spaceEntity, SpaceEntity result) {
    assertNotNull(result);
    assertEquals(spaceEntity.getPrettyName(), result.getPrettyName());
    assertEquals(1, result.getApp().size());
    AppEntity appEx = spaceEntity.getApp().iterator().next();
    AppEntity app = result.getApp().iterator().next();
    assertEquals(appEx, app);
    assertEquals(appEx.isRemovable(), app.isRemovable());
    assertEquals(appEx.getStatus(), app.getStatus());
    assertEquals(spaceEntity.getDescription(), result.getDescription());
    assertEquals(spaceEntity.getDisplayName(), result.getDisplayName());
    assertEquals(spaceEntity.getGroupId(), result.getGroupId());
    assertEquals(spaceEntity.getPriority(), result.getPriority());
    assertEquals(spaceEntity.getRegistration(), result.getRegistration());
    assertEquals(spaceEntity.getUrl(), result.getUrl());
    assertEquals(spaceEntity.getUrl(), result.getUrl());
    assertEquals(spaceEntity.getVisibility(), result.getVisibility());
    assertEquals(spaceEntity.getAvatarLastUpdated(), result.getAvatarLastUpdated());
    assertEquals(spaceEntity.getCreatedDate(), result.getCreatedDate());
    assertEquals(1, result.getMembers().size());
  }
}
