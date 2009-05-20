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
import java.util.Iterator;
import java.util.Vector;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class WorkspaceConfig extends DynamicForm {
	
	protected PartsTreeGrid treeGrid;
	
	protected ListGrid configListGrid;
	
	protected String listGridEmptyMessage;
	
	protected Vector<ConfigItem> configItems = new Vector<ConfigItem>();
	
	protected HashMap<String, ConfigItem> itemsMap = new HashMap<String, ConfigItem>();
	
	protected ListGridRecord treeGridSelectedRecord;
	
	
	public static String gadgetURL;

	public static String gadgetImagesURL;
	
	private native String getGadgetUrl() /*-{ 
	// gathering the gadget's URL from the properties url of document.URL
    if ($wnd.gadgets == null) {
      return "";
    }
    return $wnd.gadgets.util.getUrlParameters().url.match(/(.*)\//)[1];
	}-*/;
	
	
	public WorkspaceConfig(int width, int height) {
		gadgetURL = getGadgetUrl();
		gadgetImagesURL = (gadgetURL != "") ? gadgetURL + "/images/" : "";
		
		setLayoutAlign(Alignment.CENTER);
		setIsGroup(true);
		setGroupTitle("Workspace config");
		setWidth(width);
		setHeight(height);
		
		treeGrid = new PartsTreeGrid();
		treeGrid.setWidth("40%");
		treeGrid.setShowResizeBar(true);

		VLayout cL = new VLayout();
		cL.setWidth("60%");

		configListGrid = new ListGrid();
		configListGrid.setUseAllDataSourceFields(true);
		configListGrid.setAutoFetchData(true);
		configListGrid.setCanEdit(true);

		configListGrid.setWidth100();
		configListGrid.setHeight100();

		listGridEmptyMessage = configListGrid.getEmptyMessage();
		configListGrid.setShowHeader(false);
		configListGrid.setEmptyMessage("");

		DynamicForm ff = new DynamicForm();

		ff.setCellBorder(1);
		ff.setWidth100();
		ff.setBackgroundColor("#FF0000");
		TextItem ti = new TextItem("Obana!");
		ff.setFields(ti);
		ff.setLeft(10);
		ff.setTop(10);

		cL.addChild(configListGrid);
		configListGrid.addChild(ff);

		HLayout layout = new HLayout();
		layout.setMembers(treeGrid, cL);
		layout.setLeft(10);
		layout.setTop(12);
		layout.setWidth(width - 20 - 2);
		layout.setHeight(height - 30);
		addChild(layout);
	}
	
	
	public void parseConfiguration(String jsonConfiguration) {
		JSONObject json = JSONParser.parse(jsonConfiguration).isObject();

		configItems.add(parseCacheItem(json));
		itemsMap.put("cache", configItems.lastElement());
		
		configItems.add(parseContainerItem(json));
		itemsMap.put("container", configItems.lastElement());
		
		configItems.add(parseLockManager(json));
		itemsMap.put("lockManager", configItems.lastElement());
		
		configItems.add(parseQueryHandler(json));
		itemsMap.put("", configItems.lastElement());

		parseAccessManager(json);
	}
	
	protected void parseAttributesMap(JSONObject json, ConfigItem configItem) {
		HashMap<String, String> attributes = new HashMap<String, String>();
		HashMap<String, Integer> types = new HashMap<String, Integer>();

		Iterator<String> keys = json.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();

			JSONValue value = json.get(key);

			System.out.println("KEY -> " + key + " - values > " + value.toString());

			if (value.isNull() != null) {
				System.out.println("Parameter " + key + " is null!");
				attributes.put(key, "null");
				types.put(key, ConfigItem.ValueType.UNDEFINED);
			} else if (value.isString() != null) {
				attributes.put(key, value.isString().stringValue());
				types.put(key, ConfigItem.ValueType.STRING);
			} else if (value.isNumber() != null) {
				attributes.put(key, "" + value.isNumber());
				types.put(key, ConfigItem.ValueType.NUMBER);
			} else if (value.isBoolean() != null) {
				attributes.put(key, "" + value.isBoolean());
				types.put(key, ConfigItem.ValueType.BOOLEAN);
			}
			
		}

		configItem.setValueMap(attributes);
		configItem.setValueTypes(types);
	}

	protected HashMap<String, String> getParametersMap(JSONArray json) {
		HashMap<String, String> params = new HashMap<String, String>();

		for (int i = 0; i < json.size(); i++) {
			JSONObject obj = json.get(i).isObject();
			String key = obj.get("name").isString().stringValue();
			String value = obj.get("value").isString().stringValue();
			//System.out.println("KEY [" + key + "] VALUES [" + value + "]");
			params.put(key, value);
		}

		return params;
	}
	
	protected ConfigItem parseCacheItem(JSONObject json) {
		JSONObject cache = json.get("cache").isObject();
		// String type = cache.get("type").isString().stringValue();
		// com.google.gwt.user.client.Window.alert("TYPE: " + type);

		ConfigItem cacheItem = new ConfigItem("cache", "Cache", ConfigItem.ItemType.ATTRUBUTES);
		parseAttributesMap(cache, cacheItem);
		// cacheItem.setValueMap(getAttributesMap(cache));

		if (cache.get("parameters") != null) {
			ConfigItem properties = new ConfigItem("parameters", "Properties", ConfigItem.ItemType.PROPERTIES);
			cacheItem.addChild(properties);
			JSONArray parameters = cache.get("parameters").isArray();
			properties.setValueMap(getParametersMap(parameters));
		}

		// logJSON(cache);

		return cacheItem;
	}
	
	protected ConfigItem parseContainerItem(JSONObject json) {
		JSONObject container = json.get("container").isObject();

		ConfigItem containerItem = new ConfigItem("container", "Container", ConfigItem.ItemType.ATTRUBUTES);
		// containerItem.setValueMap(getAttributesMap(container));
		parseAttributesMap(container, containerItem);

		if (container.get("parameters") != null) {
			ConfigItem properties = new ConfigItem("parameters", "Properties", ConfigItem.ItemType.PROPERTIES);
			containerItem.addChild(properties);
			JSONArray parameters = container.get("parameters").isArray();
			properties.setValueMap(getParametersMap(parameters));
		}

		if (container.get("valueStorages") != null) {
			ConfigItem valueStorages = new ConfigItem("valueStorages", "Value Storages", ConfigItem.ItemType.COLLECTION);
			containerItem.addChild(valueStorages);

			JSONArray storageArray = container.get("valueStorages").isArray();
			for (int i = 0; i < storageArray.size(); i++) {
				JSONObject storageJSON = storageArray.get(i).isObject();
				ConfigItem storageItem = new ConfigItem("", "Value Storage", ConfigItem.ItemType.ATTRUBUTES, "cube_yellow.png");
				valueStorages.addChild(storageItem);
				parseAttributesMap(storageJSON, storageItem);

				ConfigItem storageParams = new ConfigItem("parameters", "Properties", ConfigItem.ItemType.PROPERTIES);
				storageItem.addChild(storageParams);
				if (storageJSON.get("parameters") != null) {
					storageParams.setValueMap(getParametersMap(storageJSON.get("parameters").isArray()));
				}

				ConfigItem storageFilters = new ConfigItem("filters", "Filters", ConfigItem.ItemType.COLLECTION, "star_blue.png");
				storageItem.addChild(storageFilters);
				parseStorageFilters(storageJSON, storageFilters);
			}
		}

		return containerItem;
	}
	
	protected void parseStorageFilters(JSONObject storageJSON, ConfigItem storageFilrers) {
		if (storageJSON.get("filters") == null) {
			com.google.gwt.user.client.Window.alert("Filters not set!");
			return;
		}

		JSONArray filters = storageJSON.get("filters").isArray();
		// com.google.gwt.user.client.Window.alert("Size: " + filters.size());
		for (int i = 0; i < filters.size(); i++) {
			JSONObject filterJSON = filters.get(i).isObject();
			ConfigItem filterConfig = new ConfigItem("", "Filter", ConfigItem.ItemType.ATTRUBUTES, "star_grey.png");
			storageFilrers.addChild(filterConfig);
			parseAttributesMap(filterJSON, filterConfig);
		}

	}
	
	protected ConfigItem parseLockManager(JSONObject json) {
		JSONObject lockManagerJSON = json.get("lockManager").isObject();

		ConfigItem lockManagerItem = new ConfigItem("lockManager", "Lock Manager", ConfigItem.ItemType.ATTRUBUTES);
		parseAttributesMap(lockManagerJSON, lockManagerItem);

		ConfigItem persisterItem = new ConfigItem("persister", "Persister", ConfigItem.ItemType.ATTRUBUTES, "cube_blue.png");
		lockManagerItem.addChild(persisterItem);
		if (lockManagerJSON.get("persister") != null) {
			JSONObject persisterJSON = lockManagerJSON.get("persister").isObject();
			parseAttributesMap(persisterJSON, persisterItem);

			if (persisterJSON.get("parameters") != null) {
				ConfigItem properties = new ConfigItem("parameters", "Properties", ConfigItem.ItemType.PROPERTIES);
				persisterItem.addChild(properties);
				JSONArray parameters = persisterJSON.get("parameters").isArray();
				properties.setValueMap(getParametersMap(parameters));
			}

		}

		return lockManagerItem;
	}
	
	protected ConfigItem parseQueryHandler(JSONObject json) {
		JSONObject queryHandler = json.get("queryHandler").isObject();

		ConfigItem queryHandlerItem = new ConfigItem("queryHandler", "Query Handler", ConfigItem.ItemType.ATTRUBUTES);
		parseAttributesMap(queryHandler, queryHandlerItem);

		if (queryHandler.get("parameters") != null) {
			ConfigItem properties = new ConfigItem("parameters", "Properties", ConfigItem.ItemType.PROPERTIES);
			queryHandlerItem.addChild(properties);
			JSONArray parameters = queryHandler.get("parameters").isArray();
			properties.setValueMap(getParametersMap(parameters));
		}

		if (queryHandler.get("analyzer") != null) {
			ConfigItem analyzerItem = new ConfigItem("analyzer", "Analyzer", ConfigItem.ItemType.ATTRUBUTES, "cube_blue.png");
			queryHandlerItem.addChild(analyzerItem);
		}

		return queryHandlerItem;
	}
	
	protected ConfigItem parseAccessManager(JSONObject json) {
		return null;
	}
	
	
	public void refreshTreeContent() {
		Tree tree = new Tree();
		tree.setModelType(TreeModelType.CHILDREN);

		PartsTreeNode rootNode = new PartsTreeNode("Root");

		fillTreeItems(rootNode, configItems);

		tree.setRoot(rootNode);
		treeGrid.setData(tree);
		tree.setNameProperty("Name");

		// treeGrid.setShowHeader(false);
		// treeGrid.setLeaveScrollbarGap(false);
		// treeGrid.setBodyStyleName("normal");

		treeGrid.addSelectionChangedHandler(new SelectionChangedHandler() {

			public void onSelectionChanged(SelectionEvent event) {
				ListGridRecord selection = treeGrid.getSelectedRecord();

				if (selection != null) {
					if (selection != treeGridSelectedRecord) {
						treeGridSelectedRecord = selection;

						ConfigItem config = (ConfigItem) treeGridSelectedRecord.getAttributeAsObject("backupServiceConfigItem");
						if (config != null) {
							if (config.getItemType() == ConfigItem.ItemType.ATTRUBUTES) {
								// createAttributesConfigGrid();
								createPropertiesConfigGrid(config);
							} else if (config.getItemType() == ConfigItem.ItemType.PROPERTIES) {
								createPropertiesConfigGrid(config);
							} else if (config.getItemType() == ConfigItem.ItemType.COLLECTION) {
								createCollectionConfig();
							}

						}

					}
				}
				
			}

		});

	}
	
	
	protected void fillTreeItems(PartsTreeNode parentNode, Vector<ConfigItem> configItems) {
		if (configItems.size() == 0) {
			return;
		}

		PartsTreeNode[] nodes = new PartsTreeNode[configItems.size()];
		for (int i = 0; i < configItems.size(); i++) {
			ConfigItem config = configItems.get(i);
			PartsTreeNode treeNode = new PartsTreeNode(config.getDisplayName(), gadgetImagesURL + config.getImageName());
			treeNode.setAttribute("backupServiceConfigItem", config);
			nodes[i] = treeNode;
			fillTreeItems(treeNode, config.getChilds());
		}

		parentNode.setChildren(nodes);
	}

	
	protected void createPropertiesConfigGrid(ConfigItem configItem) {
		configListGrid.setShowHeader(true);

		ListGridField fieldName = new ListGridField("name", "Name");
		fieldName.setAlign(Alignment.LEFT);
		ListGridField fieldValue = new ListGridField("value", "Value");
		fieldValue.setAlign(Alignment.LEFT);
		configListGrid.setData(new ListGridRecord[0]);
		configListGrid.setFields(fieldName, fieldValue);

		HashMap<String, String> params = configItem.getValueMap();
		ListGridRecord[] recs = new ListGridRecord[params.size()];
		Iterator<String> iter = params.keySet().iterator();
		int i = 0;
		while (iter.hasNext()) {
			String key = iter.next();
			String value = params.get(key);

			ListGridRecord record = new ListGridRecord();
			record.setAttribute("name", key);
			record.setAttribute("value", value);

			recs[i] = record;

			i++;
		}

		configListGrid.setData(recs);
	}
	

	protected void createCollectionConfig() {
		configListGrid.setData(new ListGridRecord[0]);
		configListGrid.setFields();
	}
	
	
	public static class PartsTreeGrid extends TreeGrid {
		public PartsTreeGrid() {
			// setBodyStyleName("normal");
			// setAlternateRecordStyles(true);
			setShowHeader(false);
			setLeaveScrollbarGap(false);

			// setCellHeight(cellHeight)

			// setShowRoot(true);

			// setEmptyMessage("<br>Drag & drop parts here");
			// setManyItemsImage(gadgetImagesURL + "images/AddNewCategory.gif");
			// setAppImgDir("");

			// com.google.gwt.user.client.Window.alert("111111 " + getAppImgDir());

			// setCanReorderRecords(true);
			// setCanAcceptDroppedRecords(true);
			// setCanDragRecordsOut(true);
		}
	}
	
	
	public static class PartsTreeNode extends TreeNode {
		public PartsTreeNode(String name, String icon) {
			this(name, icon, new PartsTreeNode[] {});
		}

		public PartsTreeNode(String name, PartsTreeNode... children) {
			this(name, null, children);
		}

		public PartsTreeNode(String name, String icon, PartsTreeNode... children) {
			setAttribute("Name", name);
			setAttribute("children", children);
			if (icon != null)
				setAttribute("icon", icon);
		}
	}
	
	
	protected static void logJSON(JSONObject json) {
		Iterator<String> jsonKeys = json.keySet().iterator();
		while (jsonKeys.hasNext()) {
			String key = jsonKeys.next();

			JSONValue value = json.get(key);
			if (value.isArray() != null) {
				System.out.println("ARRAY >> " + key + " >> " + value);
			}

			if (value.isString() != null) {
				System.out.println("STRING >> " + key + " >> " + value);
			}

			if (value.isNumber() != null) {
				System.out.println("NUMBER >> " + key + " >> " + value);
			}

			if (value.isBoolean() != null) {
				System.out.println("BOOLEAN >> " + key + " >> " + value);
			}

			if (value.isObject() != null) {
				System.out.println("OBJECT >> " + key + " >> " + value);
			}

		}
	}	
	
	
	public JSONObject getConfiguration() {
		JSONObject configuration = new JSONObject();
		
		configuration.put("cache", getCacheConfiguration());
		configuration.put("container", getContainerConfiguration());
		
		return configuration;
	}
	
	
	protected void setParameters(JSONObject json, ConfigItem propertiesItem) {
		JSONArrayShell propertiesJSON = new JSONArrayShell();
		json.put("parameters", propertiesJSON);
		
		HashMap<String, String> properties = propertiesItem.getValueMap();
		Iterator<String> keyIter = properties.keySet().iterator();
		//int i = 0;
		while (keyIter.hasNext()) {
			String key = keyIter.next();
			String value = properties.get(key);
			
			JSONObject propertyJSON = new JSONObject();
			propertyJSON.put("name", new JSONString(key));
			propertyJSON.put("value", new JSONString(value));
			
			propertiesJSON.putValue(propertyJSON);
			//propertiesJSON.set(i, );
		}
	}
	
	
	protected void setAttributes(JSONObject json, ConfigItem configItem) {
		HashMap<String, String> attributes = configItem.getValueMap();
		HashMap<String, Integer> attributeTypes = configItem.getValueTypes();
		
		Iterator<String> keyIter = attributes.keySet().iterator();
		while (keyIter.hasNext()) {
			String attributeName = keyIter.next();
			String attributeValue = attributes.get(attributeName);
			int attributeType = attributeTypes.get(attributeName);
			
			System.out.println("NAME: " + attributeName + "  VALUE: " + attributeValue + "  TYPE: " + attributeType);
			
			switch (attributeType) {
				case ConfigItem.ValueType.STRING:
					json.put(attributeName, new JSONString(attributeValue));
					break;
					
				case ConfigItem.ValueType.NUMBER:
					json.put(attributeName, new JSONNumber(Integer.parseInt(attributeValue)));
					break;
					
				case ConfigItem.ValueType.BOOLEAN:
					json.put(attributeName, JSONBoolean.getInstance(Boolean.parseBoolean(attributeValue)));
					break;
					
				default:
					json.put(attributeName, JSONNull.getInstance());
			}
			
		}
		
	}
	
	
	protected JSONObject getCacheConfiguration() {
		JSONObject cacheJSON = new JSONObject();
		
		ConfigItem cacheConfig = itemsMap.get("cache");
		setAttributes(cacheJSON, cacheConfig);
		
		ConfigItem propertiesItem = cacheConfig.getChild("parameters");
		setParameters(cacheJSON, propertiesItem);
		
		return cacheJSON;
	}
	
	protected JSONObject getContainerConfiguration() {
		JSONObject containerJSON = new JSONObject();
		
		ConfigItem containerConfig = itemsMap.get("container");		
		setAttributes(containerJSON, containerConfig);
		
		return containerJSON;
	}


	
//	protected HashMap<String, String> getParametersMap(JSONArray json) {
//		HashMap<String, String> params = new HashMap<String, String>();
//
//		for (int i = 0; i < json.size(); i++) {
//			JSONObject obj = json.get(i).isObject();
//			String key = obj.get("name").isString().stringValue();
//			String value = obj.get("value").isString().stringValue();
//			//System.out.println("KEY [" + key + "] VALUES [" + value + "]");
//			params.put(key, value);
//		}
//
//		return params;
//	}
	
	
	
	

}
