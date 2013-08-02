package org.exoplatform.social.core.updater;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class ModelOverriderUpdaterPlugin extends UpgradeProductPlugin {

  private static final Log LOG = ExoLogger.getLogger(ModelOverriderUpdaterPlugin.class);

  public ModelOverriderUpdaterPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {

    //
    PortalContainer portalContainer = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) portalContainer.getComponentInstanceOfType(ChromatticManager.class);
    Boolean startComponentRequestLifecycle=false;
    try {
      ((ComponentRequestLifecycle) manager).startRequest(portalContainer);
      startComponentRequestLifecycle=true;
    } catch (Exception e1) {
      LOG.warn(e1);
    }
    SocialChromatticLifeCycle lifeCycle = (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

    //
    Session session = lifeCycle.getSession().getJCRSession();

    try {

      ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
      nodeTypeManager.registerNodeTypes(getModelIS(), ExtendedNodeTypeManager.REPLACE_IF_EXISTS, "text/xml");

    } catch (RepositoryException e) {

      LOG.error(e);
      session.logout();

    }
      finally {
        if(startComponentRequestLifecycle)
          ((ComponentRequestLifecycle) manager).endRequest(portalContainer);
    }

  }

  public InputStream getModelIS() {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/portal/chromattic-nodetypes.xml");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isAfter(newVersion, previousVersion);
  }

}
