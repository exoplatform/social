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
package org.exoplatform.gadgets.BackupManager.client;

import java.util.HashMap;

import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@ModulePrefs(title = "BackupManager", author = "Vitaliy Gulyy", 
		author_email = "gavrikvetal@gmail.com",
	    height = 515,
	    description = "Backup Manager" )
	    
public class BackupManager extends Gadget<BMUserPreferences> implements Const {
	
	private String URL_PREFIX;
	
	public static final int GADGET_WIDTH = 700;
	
	public static final int GADGET_HEIGHT = 500;
	
	public static final int DELIMITER_WIDTH = 10;
	
	public static final int BUTTONS_WIDTH = 100;
	
	protected AbsolutePanel absolutePanel;
	
	protected ListGrid backupsGrid;
	
	protected ListGrid restoresGrid;
	
	

	protected String backupLogDir;

	protected int defaultIncrementalJobPeriod;
	
	protected String fullBackupTypeClass;
	
	protected String incrementalBackupTypeClass;
	

	protected IButton newButton;
	
	protected IButton restoreButton;
	
	protected IButton backupInfoButton;
	
	protected static BackupManager instance;
	
	public static BackupManager getInstance() {
		return instance;
	}
	
	public BackupManager() {
		instance = this;
	}
	
	
	  // ------------- JSNI methods
	  private native void initGadget() /*-{
	    // set width of gadget to 100%
	    if ($wnd.frameElement == null) {
	      return;
	    }
	    $wnd.frameElement.style.width = "100%";
	  }-*/;	
	  
	  
	private native boolean isShell() /*-{
		return $wnd.gadgets == null;
	}-*/;
	
	public String getURLPrefix() {
		return URL_PREFIX;
	}
	
	@Override
	protected void init(BMUserPreferences preferences) {
		initGadget();
		
		//Window.alert("W: " + RootPanel.get().getOffsetWidth());
		
		if (isShell()) {
			URL_PREFIX = "http://localhost:8888/org.exoplatform.gadgets.BackupManager.BackupManager";
		} else {
			URL_PREFIX = "";
		}
		
		absolutePanel = new AbsolutePanel();
		absolutePanel.setStyleName("backupmanager-panel");
		RootPanel.get().add(absolutePanel);
		
		DOM.setStyleAttribute(absolutePanel.getElement(), "width", "" + GADGET_WIDTH + "px");
		DOM.setStyleAttribute(absolutePanel.getElement(), "height", "" + GADGET_HEIGHT + "px");				
		
		//DOM.setStyleAttribute(absolutePanel.getElement(), "background", "#FFAAEE");
		
		new ReSizeTimer().schedule(500);
	}
	
	protected class ReSizeTimer extends Timer {
		@Override
		public void run() {
			initGadget();
			positionToCenter(Window.getClientWidth(), Window.getClientHeight());
			new CreateTimer().schedule(200);			
		}
	};
	
	protected void positionToCenter(int width, int height) {
		int left = (width - GADGET_WIDTH) / 2;
		int top = (height - GADGET_HEIGHT) / 2;			
		DOM.setStyleAttribute(absolutePanel.getElement(), "left", "" + left + "px");
		DOM.setStyleAttribute(absolutePanel.getElement(), "top", "" + top + "px");
	}
	
//	protected void createResizeableWindow() {
//		Window.addWindowResizeListener(new WindowResizeListener() {
//			public void onWindowResized(int arg0, int arg1) {
//				int w = RootPanel.get().getOffsetWidth();
//				int h = RootPanel.get().getOffsetHeight();
//				BackupManager.getInstance().positionToCenter(w, h);
//			}
//		});
//	}
	
	protected class CreateTimer extends Timer {
		@Override
		public void run() {
			//DOM.setStyleAttribute(absolutePanel.getElement(), "background", "#FFAAEE");
			createBackupsPanel();
			createRestoresPanel();
			
			refreshBaskupsList();
			
			loadBackupManagerConfig();

			//createResizeableWindow();

		}
	};
	

	protected void createBackupsPanel() {
		int panelWidth = GADGET_WIDTH - (DELIMITER_WIDTH * 2);
		int panelHeight = (GADGET_HEIGHT - (DELIMITER_WIDTH * 3)) / 2;

		AbsolutePanel backupsPanel = new AbsolutePanel();
		//DOM.setStyleAttribute(backupsPanel.getElement(), "background", "#FFEEAA");
		backupsPanel.setWidth("" + panelWidth + "px");
		backupsPanel.setHeight("" + panelHeight + "px");
		absolutePanel.add(backupsPanel, DELIMITER_WIDTH, DELIMITER_WIDTH);
		
		DynamicForm form = new DynamicForm();
		backupsPanel.add(form, 0, 0);
		form.setIsGroup(true);
		form.setGroupTitle("Backups");
		form.setSize("" + panelWidth + "px", "" + panelHeight + "px");

		int gridWidth = panelWidth - 20 - 2;
		backupsGrid = createGrid(gridWidth);
		backupsGrid.setSize("" + gridWidth + "px", "" + (panelHeight - 70) + "px");
		backupsGrid.setLeft(10);
		backupsGrid.setTop(20);
		form.addChild(backupsGrid);
		
		backupsGrid.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (backupsGrid.getSelectedRecord() != null) {
					backupInfoButton.enable();
					restoreButton.enable();
				}									
			}
			
		});
		
//		backupsGrid.addDoubleClickHandler(new DoubleClickHandler() {
//			public void onDoubleClick(DoubleClickEvent event) {
//				ListGridRecord selectedRecord = backupsGrid.getSelectedRecord();
//				if (selectedRecord == null) {
//					return;
//				}
//				new NewBackupForm().editBackup(selectedRecord);
//			}								
//		});		
		
		newButton = new IButton("New");
		form.addChild(newButton);
		newButton.setWidth(BUTTONS_WIDTH);
		newButton.setLeft(10);
		newButton.setTop(panelHeight - 20 - 2 - newButton.getHeight());
		newButton.addClickHandler(newBackupClickHandler);

		restoreButton = new IButton("Restore");
		form.addChild(restoreButton);
		restoreButton.setWidth(BUTTONS_WIDTH);
		restoreButton.setLeft(10 + BUTTONS_WIDTH + 10);
		restoreButton.setTop(panelHeight - 20 - 2 - restoreButton.getHeight());
		restoreButton.disable();
		restoreButton.addClickHandler(restoreBackupClickHandler);
		
		backupInfoButton = new IButton("Get Info");
		form.addChild(backupInfoButton);
		backupInfoButton.setWidth(BUTTONS_WIDTH);
		backupInfoButton.setLeft(panelWidth - 10 - BUTTONS_WIDTH - 2);
		backupInfoButton.setTop(panelHeight - 20 - 2 - backupInfoButton.getHeight());
		backupInfoButton.disable();
		backupInfoButton.addClickHandler(backupInfoClickHandler);
	}
	
	protected ClickHandler newBackupClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			new BackupForm().newBackup(defaultIncrementalJobPeriod, backupLogDir);			
		}
	};
	
	protected ClickHandler restoreBackupClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			//new NewRestoreForm();
			//new RestoreForm().createNew();
			
			try {
				String requestURL = URL_PREFIX + "/rest/jcr-backup/info/default-ws-config?" + Random.nextDouble();
				
				RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, requestURL);

				requestBuilder.sendRequest("", new RequestCallback() {

					public void onError(Request request, Throwable throwable) {
						SC.warn("Can't contact the server!", null);
					}

					public void onResponseReceived(Request request, Response response) {
						int status = response.getStatusCode();
						
						if (status != 200) {
							SC.say("Can't load default configuration! Server status: " + status);
						} else {
							ListGridRecord selectedRecord = backupsGrid.getSelectedRecord();
							if (selectedRecord == null) {
								return;
							}
							
							String backupId = selectedRecord.getAttribute(BACKUP_ID);
							new RestoreForm().createNew(response.getText(), backupId);							
						}
						
					}
					
				});
				
			} catch (Exception exc) {
				System.out.println("Unhandled exception. " + exc.getMessage());
				exc.printStackTrace();
			}
			
		}
	};
	
	protected ClickHandler backupInfoClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			ListGridRecord selectedRecord = backupsGrid.getSelectedRecord();
			if (selectedRecord == null) {
				return;
			}
			new BackupForm().editBackup(selectedRecord);			
		}
	};
	
	protected void createRestoresPanel() {
		int panelWidth = GADGET_WIDTH - (DELIMITER_WIDTH * 2);
		int panelHeight = (GADGET_HEIGHT - (DELIMITER_WIDTH * 3)) / 2;

		AbsolutePanel restortesPanel = new AbsolutePanel();
		//DOM.setStyleAttribute(restortesPanel.getElement(), "background", "#EEFF99");
		restortesPanel.setWidth("" + panelWidth + "px");
		restortesPanel.setHeight("" + panelHeight + "px");		
		absolutePanel.add(restortesPanel, DELIMITER_WIDTH, DELIMITER_WIDTH * 2 + panelHeight);
		
		DynamicForm form = new DynamicForm();
		restortesPanel.add(form, 0, 0);
		form.setIsGroup(true);
		form.setGroupTitle("Restores");
		form.setSize("" + panelWidth + "px", "" + panelHeight + "px");
		//form.setSize("550px", "275px");
		
		int gridWidth = panelWidth - 20 - 2;
		restoresGrid = createGrid(gridWidth);
		restoresGrid.setSize("" + gridWidth + "px", "" + (panelHeight - 70) + "px");
		restoresGrid.setLeft(10);
		restoresGrid.setTop(20);
		form.addChild(restoresGrid);		
		
		IButton deleteWorkspaceButton = new IButton("Delete workspace...");
		form.addChild(deleteWorkspaceButton);
		deleteWorkspaceButton.setWidth(150);
		deleteWorkspaceButton.setLeft(10);
		deleteWorkspaceButton.setTop(panelHeight - 20 - 2 - deleteWorkspaceButton.getHeight());
		deleteWorkspaceButton.addClickHandler(deleteWorkspaceButtonClickHandler);
		
		IButton infoButton = new IButton("Get Info");
		form.addChild(infoButton);
		infoButton.setWidth(BUTTONS_WIDTH);
		infoButton.setLeft(panelWidth - 10 - BUTTONS_WIDTH - 2);
		infoButton.setTop(panelHeight - 20 - 2 - infoButton.getHeight());
		infoButton.addClickHandler(restoreInfoButtonClickHandler);
	}
	
	protected ClickHandler deleteWorkspaceButtonClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			SC.ask("Delete workspace?<br>&nbsp;", new BooleanCallback() {
				public void execute(Boolean value) {
				}
			});
		}
	};
	
	protected ClickHandler restoreInfoButtonClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
		}
	};
	
	protected String formatCell(String text) {
		//return "<div class=\"backupManager-tableCell\" title=\"" + text + "\">" + ("".equals(text) ? "&nbsp;" : text) + "</div>";
		return text;
	}

	
	public void refreshBaskupsList() {
		try {
			String requestURL = URL_PREFIX + "/rest/jcr-backup/info/backup?" + Random.nextDouble();

			RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, requestURL);
			
			requestBuilder.sendRequest("", new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					//Window.alert("Request error!");
					SC.warn("Can't connect the server!", null);
				}

				public void onResponseReceived(Request request,
						Response response) {
					int status = response.getStatusCode();
					if (status == 200) {
						try {
							//Window.alert("!!!!!");
							
							JSONValue value = JSONParser.parse(response.getText());
							JSONObject v = value.isObject();							
							JSONArray backups = v.get("backups").isArray();
							
							ListGridRecord []recs = new ListGridRecord[backups.size()];
							for (int i = 0; i < backups.size(); i++) {
								JSONObject obj = backups.get(i).isObject();

								ListGridRecord record = new ListGridRecord();
								
								String backupId = obj.get(BACKUP_ID).isString().stringValue();								
								String workspaceName = obj.get(REPOSITORY_NAME).isString().stringValue() + "/" + obj.get(WORKSPACE_NAME).isString().stringValue();
								int backupType = (int)obj.get(BACKUP_TYPE).isNumber().doubleValue();								
								String startedTime = obj.get(STARTED_TIME).isString().stringValue();
								String finishedTime = obj.get(FINISHED_TIME).isString().stringValue();								
								
								record.setAttribute(BACKUP_ID, formatCell(backupId));
								
								record.setAttribute(WORKSPACE_NAME, workspaceName);
								record.setAttribute(DISPLAY_WORKSPACENAME, formatCell(workspaceName));
								
								record.setAttribute(BACKUP_TYPE, formatCell(backupType == 1 ? "full and incremental" : "full only"));
								record.setAttribute(STARTED_TIME, formatCell(startedTime));
								record.setAttribute(FINISHED_TIME, formatCell(finishedTime));
								
								int type = (int)obj.get(TYPE).isNumber().doubleValue();
								int state = (int)obj.get(STATE).isNumber().doubleValue();
								
								if (type == 0) {
									record.setAttribute(STATE, formatCell("Finished"));
								} else if (type == -1) {
									if (backupType == 0) {
										switch (state) {
										case 0:
											record.setAttribute(STATE, formatCell("Starting"));
											break;
										case 1:
											record.setAttribute(STATE, formatCell("Waiting"));
											break;
										case 2:
											record.setAttribute(STATE, formatCell("Working"));
											break;
										case 4:
											record.setAttribute(STATE, formatCell("Finished"));
											
											break;
										}																			
									} else {
										record.setAttribute(STATE, formatCell("Working"));
									}
								}
								
								recs[i] = record;	
							}
							
							backupsGrid.setRecords(recs);							
							
						} catch (Exception exc) {
							System.out.println("Unhandled exception. " + exc.getMessage());
							exc.printStackTrace();
						}						
					} else {
						//Window.alert("STATUS " + response.getStatusCode());
						SC.warn("Can't refresh backup list! Status code: " + response.getStatusCode(), null);
					}
				}
			});

		} catch (Exception exc) {
			System.out.println("Unhandled exception. " + exc.getMessage());
			exc.printStackTrace();
		}
	}
	
	protected void loadBackupManagerConfig() {
		try {
			String requestURL = URL_PREFIX + "/rest/jcr-backup/info/?" + Random.nextDouble();
			RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, requestURL);
			requestBuilder.sendRequest("", new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					Window.alert("Request error!");
				}

				public void onResponseReceived(Request request, Response response) {
					JSONValue value = JSONParser.parse(response.getText());
					JSONObject v = value.isObject();
					
					backupLogDir = v.get(BACKUP_LOG_DIR).isString().stringValue();
					
					//Window.alert("LogDIR: " + backupLogDir);
					
					defaultIncrementalJobPeriod = (int)v.get(DEFAULT_INCREMENTAL_JOB_PERIOD).isNumber().doubleValue();
					fullBackupTypeClass = v.get(FULL_BACKUP_TYPE).isString().stringValue();
					incrementalBackupTypeClass = v.get(INCREMENTAL_BACKUP_TYPE).isString().stringValue();
				}
			});
		} catch (Exception exc) {
			System.out.println("Unhandled exception. " + exc.getMessage());
			exc.printStackTrace();
		}		
	}

	protected static HashMap<String, Integer> headerWidths = new HashMap<String, Integer>();
	
	static {
		headerWidths.put(BACKUP_ID, 15);
		//headerWidths.put(WORKSPACE_NAME, 25);
		headerWidths.put(DISPLAY_WORKSPACENAME, 25);
		headerWidths.put(BACKUP_TYPE, 15);
		headerWidths.put(STATE, 15);
		headerWidths.put(STARTED_TIME, 15);
	}
	
	protected void tuneGridField(ListGridField field, int tableWidth) {
		if (headerWidths.get(field.getName()) == null) {
			field.setWidth("*");			
		} else {
			int percent = headerWidths.get(field.getName());
			int width = tableWidth / 100 * percent;
			field.setWidth(width);
		}
	}
	
	protected ListGrid createGrid(int width) {
		ListGrid grid = new ListGrid();

		ListGridField fId = new ListGridField(BACKUP_ID, "ID");		
		fId.setAlign(Alignment.CENTER);
		tuneGridField(fId, width);
		
		//ListGridField fWs = new ListGridField(WORKSPACE_NAME, "Workspace");
		ListGridField fWs = new ListGridField(DISPLAY_WORKSPACENAME, "Workspace");
		fWs.setAlign(Alignment.LEFT);
		tuneGridField(fWs, width);

		ListGridField fType = new ListGridField(BACKUP_TYPE, "Type");
		fType.setAlign(Alignment.LEFT);
		tuneGridField(fType, width);
		
		ListGridField fState = new ListGridField(STATE, "State");
		fState.setAlign(Alignment.LEFT);
		tuneGridField(fState, width);
		
		ListGridField fStart = new ListGridField(STARTED_TIME, "Start", 100);
		fStart.setAlign(Alignment.LEFT);
		tuneGridField(fStart, width);
		
		ListGridField fFinish = new ListGridField(FINISHED_TIME, "Finish", 100);
		fFinish.setAlign(Alignment.LEFT);
		tuneGridField(fFinish, width);

		grid.setFields(fId, fWs, fType, fState, fStart, fFinish);
		
		return grid;
	}	

}

interface BMUserPreferences extends UserPreferences {
}	
