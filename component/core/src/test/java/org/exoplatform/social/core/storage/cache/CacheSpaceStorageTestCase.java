package org.exoplatform.social.core.storage.cache;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class CacheSpaceStorageTestCase extends AbstractCoreTest {

  private CachedSpaceStorage cachedSpaceStorage;
  private SocialStorageCacheService cacheService;
  private CachedActivityStorage cachedActivityStorage;
  private IdentityStorageImpl identityStorage;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    cachedSpaceStorage = (CachedSpaceStorage) getContainer().getComponentInstanceOfType(CachedSpaceStorage.class);
    cachedActivityStorage = (CachedActivityStorage) getContainer().getComponentInstanceOfType(CachedActivityStorage.class);
    identityStorage = (IdentityStorageImpl) getContainer().getComponentInstanceOfType(IdentityStorageImpl.class);
    cacheService = (SocialStorageCacheService) getContainer().getComponentInstanceOfType(SocialStorageCacheService.class);

  }

  public void testRemoveSpace() throws Exception {

    //
    Space space = new Space();
    space.setDisplayName("Hello");
    cachedSpaceStorage.saveSpace(space, true);

    //
    Identity i = new Identity("foo", "bar");
    identityStorage.saveIdentity(i);

    //
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(i.getId());
    cachedActivityStorage.saveActivity(i, activity);
    
    //
    List<Identity> is = new ArrayList<Identity>();
    is.add(i);
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());
    cachedActivityStorage.getActivitiesOfIdentities(is, 0, 10).size();
    assertEquals(1, cacheService.getActivitiesCache().getCacheSize());

    //
    cachedSpaceStorage.deleteSpace(space.getId());
    assertEquals(0, cacheService.getActivitiesCache().getCacheSize());

  }
}
