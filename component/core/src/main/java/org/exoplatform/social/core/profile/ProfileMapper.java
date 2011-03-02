/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.ProfileConfig;


/**
 * there should be a configuration file to define the mapping. it might need to create objects other
 * than string
 */
public class ProfileMapper {

  /**
   * The config.
   */
  private ProfileConfig config;

  /**
   * Copy.
   *
   * @param infos   the infos
   * @param profile the profile
   * @throws Exception the exception
   */
  public void copy(Map infos, Profile profile) throws Exception {
    Iterator it = infos.keySet().iterator();

    //remove the fields we are editing
    while (it.hasNext()) {
      String key = (String) it.next();
      if (key.endsWith(".isEditing")) {
        profile.setProperty(key.substring(0, key.length() - 10), new ArrayList());
      }
    }

    //we store some object in this map temporarly the time to reconstruct
    //the objects. At the end, we transform it to List
    HashMap tmpMaps = new HashMap<String, HashMap>();

    it = infos.keySet().iterator();
    while (it.hasNext()) {
      String key = (String) it.next();

      //we skip this element since it is not to be saved
      if (key.endsWith(".isEditing")) {
        continue;
      }

      Object value = infos.get(key);

      //need to not do it for forced multivalue
      if (value instanceof String[]) {
        if (((String[]) value).length == 1) {
          value = ((String[]) value)[0];
        }
      }

      int pos = key.indexOf(".");

      if (pos > 0) {
        String nKey = key.substring(0, pos);

        //remove all the numbers from the key
        nKey = nKey.replaceAll("[0-9]", "");

        //get the id of the key
        int nId = Integer.parseInt(key.substring(nKey.length(), pos));

        Object prop = tmpMaps.get(nKey);
        if (prop == null) {
          prop = new HashMap<String, Map>();
          tmpMaps.put(nKey, prop);
        }

        // will throw an exception if prop is of the wrong type
        HashMap lProp = (HashMap) prop;

        Map el = (Map) lProp.get("" + nId);

        if (el == null) {
          lProp.put("" + nId, new HashMap<String, String>());
          el = (Map) lProp.get("" + nId);
        }
        String name = key.substring(pos + 1);

        this.getConfig();
        String type = config.getType(config.getNodeType(nKey), name);

        if (type.equals("String")) {
          el.put(name, value.toString());
        } else if (value.toString().length() > 0) {
          if (type.equals("Boolean")) {
            el.put(name, new Boolean(value.toString()));
          } else if (type.equals("Double")) {
            el.put(name, new Double(value.toString()));
          } else if (type.equals("Long")) {
            el.put(name, new Long(value.toString()));
          }
        }
      } else {
        profile.setProperty(key, value);
      }
    }

    // transform the tmpMaps to a list
    Iterator itTmpMap = tmpMaps.keySet().iterator();
    while (itTmpMap.hasNext()) {
      String key = (String) itTmpMap.next();
      HashMap<String, HashMap> value = (HashMap) tmpMaps.get(key);
      profile.setProperty(key, new ArrayList<HashMap>());
      List l = (List) profile.getProperty(key);
      it = value.values().iterator();
      while (it.hasNext()) {
        l.add(it.next());
      }
    }

  }

  /**
   * Gets the config.
   *
   * @return the config
   */
  private ProfileConfig getConfig() {
    if (config == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      this.config = (ProfileConfig) container.getComponentInstanceOfType(ProfileConfig.class);
    }
    return config;
  }
}
