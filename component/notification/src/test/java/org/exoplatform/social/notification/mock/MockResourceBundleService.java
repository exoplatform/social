/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.ResourceBundleService;


public class MockResourceBundleService implements ResourceBundleService {
  private static final String RESOURCE_LOCATION = "jar:/";
  ConfigurationManager configurationService;
  ResourceBundle resourceBundle;
  List<String> locals = new ArrayList<String>();
  public MockResourceBundleService() {
    configurationService = CommonsUtils.getService(ConfigurationManager.class);
  }
  
  private ResourceBundle getResourceBundle() {
    if (resourceBundle == null) {
      resourceBundle = new TestResourceBundle();
    }
    return resourceBundle;
  }
  
  private static List<String> readTextECToListByInput(InputStream input) throws IOException {
    List<String> list = new ArrayList<String>();
    Scanner scanner = new Scanner(input, "UTF-8");
    try {
      while (scanner.hasNextLine()) {
        String s = scanner.nextLine();
        list.add(s);
      }
    } catch (Exception e) {
    } finally {
      scanner.close();
      input.close();
    }
    return list;
  }

  @Override
  public ResourceBundle getResourceBundle(String name, Locale locale) {
    String id = name.replace(".", "/") + "_" + locale.getLanguage() + ".properties";
    ResourceBundle resourceBundle = getResourceBundle();
    if(locals.contains(id) == false) {
      InputStream inputStream = null;
      List<String> list = new ArrayList<String>();
      try {
        inputStream = configurationService.getInputStream(RESOURCE_LOCATION + id);
        list = readTextECToListByInput(inputStream);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        if(inputStream != null) {
          try {
            inputStream.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      boolean isContinue = false;
      String key = "", value = "";
      int t;
      for (String string : list) {
        
        if(isContinue) {
          value += string;
        }
        
        if((t = string.indexOf("=")) > 0 && isContinue == false) {
          key = string.substring(0, t);
          value = string.substring(t + 1);
        } 
        
        if((t=string.lastIndexOf("\\")) == (string.length() - 1) && t > 0) {
          isContinue = true;
        } else {
          if(key.length() > 0) {
            ((TestResourceBundle)resourceBundle).addData(key, value.replace("\\", ""));
          }
          isContinue = false;
          key="";
          value ="";
        }
      }
      locals.add(id);
    }
    
    return resourceBundle;
  }

  @Override
  public ResourceBundle getResourceBundle(String name, Locale locale, ClassLoader cl) {
    return null;
  }

  @Override
  public ResourceBundle getResourceBundle(String[] name, Locale locale) {
    return null;
  }

  @Override
  public ResourceBundle getResourceBundle(String[] name, Locale locale, ClassLoader cl) {
    return null;
  }

  @Override
  public ResourceBundleData getResourceBundleData(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ResourceBundleData removeResourceBundleData(String id) {
    return null;
  }

  @Override
  public void saveResourceBundle(ResourceBundleData data) {
    
  }

  @Override
  public PageList<ResourceBundleData> findResourceDescriptions(Query q) {
    return null;
  }

  @Override
  public ResourceBundleData createResourceBundleDataInstance() {
    return null;
  }

  @Override
  public String[] getSharedResourceBundleNames() {
    return null;
  }
  
  
  private class TestResourceBundle extends ResourceBundle {
    private Map<String, String> data = new HashMap<String, String>();

    public void addData(String key, String value) {
      this.data.put(key, value);
    }

    @Override
    protected Object handleGetObject(String key) {
      return data.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return new Enumeration<String>() {
        Iterator<String> it = data.keySet().iterator();

        @Override
        public boolean hasMoreElements() {
          return it.hasNext();
        }

        @Override
        public String nextElement() {
          return it.next();
        }
      };
    }
    
  }

}
