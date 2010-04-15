package org.exoplatform.social.core.activitystream;

import java.util.Comparator;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.model.Activity;

/**
 * A base plugin to configure {@link ActivityProcessor}s for {@link ActivityManager}.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class BaseActivityProcessorPlugin extends BaseComponentPlugin  implements ActivityProcessor {

  protected int               priority;

  
  private static final Log LOG = ExoLogger.getLogger(BaseActivityProcessorPlugin.class);

  public BaseActivityProcessorPlugin(InitParams params) {
    
    try {
    priority = Integer.valueOf(params.getValueParam("priority").getValue());
    }
    catch (Exception e) {
      LOG.warn("an <value-param> 'priority' of type int is recommanded for component " + getClass());
    }
    /**
    ObjectParameter objectParam = params.getObjectParam("processor");
    if (objectParam == null) {
      throw new RuntimeException("an <object-param> 'processor' of type " + ActivityProcessor.class
          + " is mandatory for component " + getClass());
    }

    processor = (ActivityProcessor) objectParam.getObject();
*/
  }



  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }




  public abstract void processActivity(Activity activity);

}
