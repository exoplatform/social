package org.exoplatform.social.core.identity;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.container.xml.Parameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import javax.jcr.RepositoryException;
import java.util.*;


public class ProfileConfig {
  private List forceMultiValue = new ArrayList<String>();
  private Map<String, String> nodeTypes = new HashMap<String, String>();
  private JCRStorage storage = null;

  public ProfileConfig(InitParams params) {
    Iterator it = params.getValuesParamIterator();
    while(it.hasNext()) {
      ValuesParam param = (ValuesParam) it.next();
      String name = param.getName();
      if(name.startsWith("nodetype.")) {
        String key = name.substring(9);
        nodeTypes.put(key, param.getValue());
      }
      if(name.equals("forceMultiValue")) {
        forceMultiValue = param.getValues();
      }
    }
  }

  public List getForceMultiValue() {
    return forceMultiValue;
  }

  public void setForceMultiValue(List forceMultiValue) {
    this.forceMultiValue = forceMultiValue;
  }

  public boolean isForcedMultiValue(String fieldName) {
    return this.forceMultiValue.contains(fieldName);
  }

  public Map<String, String> getNodeTypes() {
    return nodeTypes;
  }

  public String getNodeType(String fieldName) {
    return nodeTypes.get(fieldName);
  }

  public void setNodeTypes(Map<String, String> nodeTypes) {
    this.nodeTypes = nodeTypes;
  }

  public String getType(String fieldName, String propertyName) {
    if (this.storage == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      this.storage = im.getStorage();
    }
    try {
      String type = storage.getType(fieldName, propertyName);
      if(type != null)
        return type;
    } catch (Exception e) { }

    return "String";
  }


}
