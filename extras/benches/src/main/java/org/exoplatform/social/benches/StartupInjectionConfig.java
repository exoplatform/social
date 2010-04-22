package org.exoplatform.social.benches;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
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
  
  private Map<String, Long> userActivities = new HashMap<String,Long>();

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
      Iterator<Property> it = props.getPropertyIterator();
      while (it.hasNext()) {
        Property property = (Property) it.next();
        String name = property.getName();
        String value = property.getValue();
        Long longValue = longValue(name, value);
        if ("people".equals(property)) {
          people = longValue;
        } else if ("relations".equals(property)) {
          relations = longValue;
        } else if ("activities".equals(property)) {
          activities = longValue;
        } else if (name.contains(".activities")) {
          String user = name.substring(0, name.indexOf(".activities"));
          userActivities.put(user, longValue);
        }
      }
        

      
    }
  }

  private long getLongProperty(PropertiesParam props, String property) {
    String value = props.getProperty(property);
    return longValue(property, value);
  }

  private long longValue(String property, String value) {
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
    
    if(! userActivities.isEmpty()) {
      Set<Entry<String,Long>> entries = userActivities.entrySet();
      for (Entry<String, Long> entry : entries) {
        String username = entry.getKey();
        Long count = entry.getValue();
        LOG.info("\t> about to inject " + count + " activities for " + username + ".");
        injector.generateActivities(username, count);
      }

    }
    
    
    if (nothingWasDone) {
      LOG.info("nothing to inject.");
    }
  }

  public void stop() {
    ;// nothing
  }

}
