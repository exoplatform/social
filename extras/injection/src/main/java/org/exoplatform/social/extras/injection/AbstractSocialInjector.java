package org.exoplatform.social.extras.injection;

import org.chromattic.api.query.QueryBuilder;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceEntity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.AbstractStorage;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.extras.injection.utils.LoremIpsum4J;
import org.exoplatform.social.extras.injection.utils.NameGenerator;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Random;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractSocialInjector extends DataInjector {

  /** . */
  private static Log LOG = ExoLogger.getLogger(IdentityInjector.class);
  
  /** . */
  private final static String DEFAULT_USER_BASE = "bench.user";

  /** . */
  private final static String DEFAULT_SPACE_BASE = "bench.space";

  /** . */
  protected final static String PASSWORD = "exo";

  /** . */
  protected final static String DOMAIN = "exoplatform.int";

  /** . */
  protected String userBase;

  /** . */
  protected String spaceBase;

  /** . */
  protected String spacePrettyBase ;

  /** . */
  protected int userNumber;

  /** . */
  protected int spaceNumber;

  /** . */
  protected final IdentityManager identityManager;

  /** . */
  protected final IdentityStorage identityStorage;

  /** . */
  protected final RelationshipManager relationshipManager;

  /** . */
  protected final ActivityManager activityManager;

  /** . */
  protected final OrganizationService organizationService;

  /** . */
  protected final SpaceService spaceService;

  /** . */
  protected final UserHandler userHandler;

  /** . */
  protected final Random random;

  /** . */
  protected NameGenerator nameGenerator;

  /** . */
  protected LoremIpsum4J lorem;

  /** . */
  protected PortalContainer container;

  public AbstractSocialInjector() {

    this.container = PortalContainer.getInstance();
    this.identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    this.identityStorage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorage.class);
    this.relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    this.activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    this.spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
    this.organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

    //
    this.userHandler = organizationService.getUserHandler();
    this.nameGenerator = new NameGenerator();
    this.random = new Random();
    this.lorem = new LoremIpsum4J();

  }

  public void init(String userPrefix, String spacePrefix) {

    //
    userBase = (userPrefix == null ? DEFAULT_USER_BASE : userPrefix);
    spaceBase = (spacePrefix == null ? DEFAULT_SPACE_BASE : spacePrefix);
    spacePrettyBase = spaceBase.replace(".", "");

    //
    userNumber = 0;
    spaceNumber = 0;

    boolean started = AbstractStorage.startSynchronization();

    try {
      userNumber = userNumber(userBase);
      spaceNumber = userNumber(spacePrettyBase);
    }
    catch (UndeclaredThrowableException e) {
      // If no user is existing, set keep 0 as value.
    }

    AbstractStorage.stopSynchronization(started);


    //
    LOG.info("Initial user number : " + userNumber);
    LOG.info("Initial space number : " + spaceNumber);
    
  }
  
  @Override
  public Log getLog() {
    return ExoLogger.getExoLogger(this.getClass());
  }

  @Override
  public Object execute(HashMap<String, String> stringStringHashMap) throws Exception {
    return null;
  }

  @Override
  public void reject(HashMap<String, String> stringStringHashMap) throws Exception {
  }

  private int userNumber(String base) {

    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);
    
    QueryBuilder<IdentityEntity> builder = lifeCycle.getSession().createQueryBuilder(IdentityEntity.class);
    WhereExpression where = new WhereExpression();
    where.like(IdentityEntity.remoteId, base + "%");
    return builder.where(where.toString()).get().objects().size();

  }

  private int spaceNumber(String base) {

    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

    QueryBuilder<SpaceEntity> builder = lifeCycle.getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression where = new WhereExpression();
    where.like(SpaceEntity.displayName, base + "%");
    return builder.where(where.toString()).get().objects().size();

  }

  protected String userName() {
    return userBase + userNumber;
  }

  protected String spaceName() {
    return spaceBase + spaceNumber;
  }
  
  protected int param(HashMap<String, String> params, String name) {

    //
    if (params == null) {
      throw new NullPointerException();
    }

    //
    if (name == null) {
      throw new NullPointerException();
    }

    //
    try {
      String value = params.get(name);
      if (value != null) {
        return Integer.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Integer number expected for property " + name);
    }
    return 0;
    
  }
  
}
