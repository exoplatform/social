package org.exoplatform.social.benches;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

/**
 * Startable component that can inject social data based on its init-params
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class StartupInjectionConfig implements Startable {
  private static final Log LOG        = ExoLogger.getLogger(StartupInjectionConfig.class);

  private long             people     = 0;

  private long             relations  = 0;

  private long             activities = 0;

  private DataInjector     injector;

  /**
   * Example init-params :
   * 
   * <pre>
   * &lt;init-params&gt;
   *   &lt;properties-param&gt;
   *     &lt;name&gt;inject.conf&lt;/name&gt;
   *     &lt;property name="people" value="100"/&gt;
   *     &lt;property name="relations" value="100"/&gt;
   *     &lt;property name="activities" value="100"/&gt;
   *   &lt;/properties-param&gt;
   * &lt;/init-params&gt;
   * </pre>
   * 
   * @param params
   * @param injector
   */
  public StartupInjectionConfig(InitParams params,
                           DataInjector injector,
                           OrganizationService organizationService) {
    this.injector = injector;
    PropertiesParam props = params.getPropertiesParam("inject.conf");
    if (props != null) {
      people = getLongProperty(props, "people");
      relations = getLongProperty(props, "relations");
      activities = getLongProperty(props, "activities");
    }
  }

  private long getLongProperty(PropertiesParam props, String property) {
    String value = props.getProperty(property);
    try {
      if (value != null) {
        return Long.valueOf(value);
      }
    } catch (NumberFormatException e) {
      LOG.warn("Long number expected for property " + property);
    }
    return 0;
  }

  public void start() {
    try {

      //
      RequestLifeCycle.begin(PortalContainer.getInstance());
      inject();

    } catch (Exception e) {
      LOG.error("Data injeciton failed", e);

    } finally {
      RequestLifeCycle.end();
    }

  }

  private void inject() {
    LOG.info("starting...");
    boolean nothingWasDone = true;
    if (people > 0) {
      nothingWasDone = false;
      LOG.info("\t> about to inject " + people + " people.");
      injector.generatePeople(people);
    }
    if (relations > 0) {
      nothingWasDone = false;
      LOG.info("\t> about to inject " + relations + " relations.");
      injector.generateRelations(relations);
    }
    if (activities > 0) {
      nothingWasDone = false;
      LOG.info("\t> about to inject " + activities + " activities.");
      injector.generateActivities(activities);
    }
    if (nothingWasDone) {
      LOG.info("nothing to inject.");
    }
  }

  public void stop() {
    ;// nothing
  }

}
