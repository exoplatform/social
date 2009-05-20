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
package org.exoplatform.gadgets.BackupManager.client.config;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ConfigItem {
	
	public interface ItemType {
		
		public static final int ATTRUBUTES = 0;
		
		public static final int PROPERTIES = 1;
		
		public static final int COLLECTION = 2;
		
	}
	
	public interface ValueType {
		
		public static final int UNDEFINED = 0;
		
		public static final int STRING = 1;
		
		public static final int NUMBER = 2;
		
		public static final int BOOLEAN = 3;
		
	}
	
	protected String jsonName;
	
	protected String displayName;
	
	protected int itemType;
	
	protected String imageName;
	
	protected Vector<ConfigItem> childs = new Vector<ConfigItem>();
	
	protected HashMap<String, String> valueMap = new HashMap<String, String>();
	
	protected HashMap<String, Integer> valueTypes = new HashMap<String, Integer>();

	public ConfigItem(String jsonName, String displayName, int itemType, String imageName) {
		this.jsonName = jsonName;
		this.displayName = displayName;
		this.itemType = itemType;
		
		if (imageName != null) {
			this.imageName = imageName;
		} else {
			switch (itemType) {
				case ItemType.ATTRUBUTES:
					this.imageName = "cubes_all.png";
					break;
				case ItemType.PROPERTIES:
					this.imageName = "pieces.png";
					break;
				case ItemType.COLLECTION:
					this.imageName = "cubes_yellow.png";
					break;
					
				default:
					this.imageName = "";
			}			
		}
	}
	
	public ConfigItem(String jsonName, String displayName, int itemType) {
		this(jsonName, displayName, itemType, null);
	}
	
	public String getJSONName() {
		return jsonName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getImageName() {
		return imageName;
	}
	
	public int getItemType() {
		return itemType;
	}
	
	public void addChild(ConfigItem child) {
		childs.add(child);
	}
	
	public Vector<ConfigItem> getChilds() {
		return childs;
	}
	
	public HashMap<String, String> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(HashMap<String, String> valueMap) {
		this.valueMap = valueMap;
	}
	
	public HashMap<String, Integer> getValueTypes() {
		return valueTypes;
	}
	
	public void setValueTypes(HashMap<String, Integer> valueTypes) {
		this.valueTypes = valueTypes;
	}
	
	public ConfigItem getChild(String jsonName) {
		for (int i = 0; i < childs.size(); i++) {
			ConfigItem child = childs.get(i);
			if (child.getJSONName().equals(jsonName)) {
				return child;
			}
		}
		
		return null;
	}
	
}
