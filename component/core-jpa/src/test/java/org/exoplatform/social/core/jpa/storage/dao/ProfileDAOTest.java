/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProfileDAOTest extends BaseCoreTest {
  private IdentityDAO identityDAO;

  private List<IdentityEntity> deleteIdentities = new ArrayList<>();

  private IdentityEntity identity;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityDAO = getService(IdentityDAO.class);

    identity = identityDAO.create(createIdentity());
    end();
    begin();
    identity = identityDAO.find(identity.getId());
  }

  @Override
  public void tearDown() throws Exception {
    for (IdentityEntity identity : deleteIdentities) {
      identityDAO.delete(identity);
    }

    identityDAO.delete(identity);
    end();
    begin();

    super.tearDown();
  }

  public void testCreateProfile() {
    IdentityEntity profile = createProfile(identity);
    profile = identityDAO.update(profile);

    profile = identityDAO.find(profile.getId());
    assertNotNull(profile);
    assertEquals(1, profile.getExperiences().size());
  }

  public void testUpdateProfile() {
    IdentityEntity profile = createProfile(identity);
    profile = identityDAO.update(profile);

    profile = identityDAO.find(profile.getId());
    assertNotNull(profile);
    assertEquals("/profile/root", profile.getProperties().get(Profile.URL));
    
    profile.getProperties().put(Profile.URL, "/profile/root_updated");

    identityDAO.update(profile);

    profile = identityDAO.find(profile.getId());

    assertNotNull(profile);
    assertEquals(0, profile.getExperiences().size());
    assertEquals("/profile/root_updated", profile.getProperties().get(Profile.URL));
  }

  private IdentityEntity createProfile(IdentityEntity profile) {
    profile.setCreatedDate(new Date());

    ProfileExperienceEntity exp = new ProfileExperienceEntity();
    exp.setCompany("eXo Platform");
    exp.setPosition("Developer");
    exp.setSkills("Java, Unit test");
    exp.setStartDate("2015-01-01");
    Set<ProfileExperienceEntity> exps = new HashSet<>();
    exps.add(exp);
    profile.setExperiences(exps);
    
    Map<String, String> props = new HashMap<String, String>();
    props.put(Profile.URL, "/profile/root");
    profile.setProperties(props);

    return profile;
  }

  private IdentityEntity createIdentity() {
    IdentityEntity identity = new IdentityEntity();
    identity.setProviderId(OrganizationIdentityProvider.NAME);
    identity.setRemoteId("user_test_profile");
    identity.setEnabled(true);
    identity.setDeleted(false);

    return identity;
  }
}
