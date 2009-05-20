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

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class BackupForm extends Window implements Const {
	
	protected TextItem txtRepository;
	
	protected TextItem txtWorkspace;
	
	protected TextItem txtIncrementalJobPeriod;
	
	protected TextItem txtIncrementalRepetitionNumber;
	
	
	protected ComboBoxItem comboBackupType;

	
	protected String repository;
	
	protected String workspace;
	
	protected String backupType;

	protected int incrementalJobPeriod;	
	
	protected int incrementalRepetitionNumber;
	
	protected String backupDirectory;	

	public BackupForm() {
		setWidth(450);
		setHeight(280);
		
		setTitle("Backup");
		setShowMinimizeButton(false);
		setIsModal(true);
		centerInPage();
	}
	
	protected void createForm(boolean isNewBackup) {
		DynamicForm form = new DynamicForm();
		form.setWidth(370);
    form.setPadding(20);
    form.setColWidths("200px", "*");
    form.setLayoutAlign(VerticalAlignment.BOTTOM);
    form.setLayoutAlign(Alignment.CENTER);
    //form.setCellBorder(1);

    int textFieldsWidth = 180;
    
    txtRepository = new TextItem();
    txtRepository.setTitle("Repository");
    txtRepository.setTitleAlign(Alignment.LEFT);
    txtRepository.setWidth(textFieldsWidth);
    
    txtWorkspace = new TextItem();
    txtWorkspace.setTitle("Workspace");
    txtWorkspace.setTitleAlign(Alignment.LEFT);
    txtWorkspace.setWidth(textFieldsWidth);
    
    if (!isNewBackup) {
    	txtRepository.setValue(repository);
    	txtWorkspace.setValue(workspace);
    }
    
    SpacerItem s1 = new SpacerItem();
    s1.setHeight(5);
    
    comboBackupType = new ComboBoxItem();
    comboBackupType.setTitle("Backup type");
    comboBackupType.setTitleAlign(Alignment.LEFT);
    comboBackupType.setWidth(textFieldsWidth);
    
    comboBackupType.setValueMap("full only", "full and incremental");
    comboBackupType.setAttribute("type", "enum");
    //comboBackupType.setValueField(0);
    if (isNewBackup) {
        comboBackupType.setValue("full only");        	
    } else {
    	comboBackupType.setValue(backupType);
    	comboBackupType.setType("TextItem");
    	
    	//comboBackupType.setAttribute("canEdit", false);
    	//comboBackupType.setHeight(20);
    }

    
    
    SpacerItem s2 = new SpacerItem();
    s2.setHeight(5);

    txtIncrementalJobPeriod = new TextItem();
    txtIncrementalJobPeriod.setTitle("Incremental job period");
    txtIncrementalJobPeriod.setTitleAlign(Alignment.LEFT);
    txtIncrementalJobPeriod.setWidth(textFieldsWidth);
    txtIncrementalJobPeriod.setValue(incrementalJobPeriod);

    txtIncrementalRepetitionNumber = new TextItem();
    txtIncrementalRepetitionNumber.setTitle("Incremental repetition number");
    txtIncrementalRepetitionNumber.setTitleAlign(Alignment.LEFT);
    txtIncrementalRepetitionNumber.setWidth(textFieldsWidth);
    txtIncrementalRepetitionNumber.setValue(0);

    SpacerItem s3 = new SpacerItem();
    s3.setHeight(5);
    
    form.setFields(txtRepository, txtWorkspace, s1, comboBackupType, s2, txtIncrementalJobPeriod, txtIncrementalRepetitionNumber, s3);
    addItem(form);
        
		DynamicForm butForm = new DynamicForm();
		butForm.setWidth(150);
		butForm.setLayoutAlign(VerticalAlignment.BOTTOM);
		butForm.setLayoutAlign(Alignment.CENTER);
		//butForm.setCellBorder(1);
        
    IButton butStart = new IButton("Start");          
    butStart.setWidth("80px");
    butStart.addClickHandler(startButtonClickHandler);
    if (!isNewBackup) {
    	butStart.disable();
    }
    
    IButton butStop = new IButton("Stop");
    butStop.setWidth("80px");
    butStop.addClickHandler(stopButtonClickHandler);
    if (isNewBackup) {
    	butStop.disable();
    } else {
    	if (!isWorking) {
    		butStop.disable();
    	}
    }
    
    IButton butClose = new IButton("Close");
    butClose.setWidth("80px");
    
    butClose.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				destroy();
			}
    });
		
		ToolbarItem tbi = new ToolbarItem();
		tbi.setButtons(butStart, butStop, butClose);
		butForm.setFields(tbi);

		addItem(butForm);

		if (!isNewBackup) {
			setReadOnlyFieldsTimer.schedule(10);
		}
		
		show();
	}
	
  protected Timer setReadOnlyFieldsTimer = new Timer() {
		public void run() {
			setReadOnly(txtRepository.getName());
			setReadOnly(txtWorkspace.getName());
			setReadOnly(comboBackupType.getName());
			setReadOnly(txtIncrementalJobPeriod.getName());
			setReadOnly(txtIncrementalRepetitionNumber.getName());
		}
  };
	
	private native void setReadOnly(String objectName) /*-{ 
	  if ($wnd.document.getElementsByName(objectName) == null) {
	    return;
	  }	  
	  var textField = $wnd.document.getElementsByName(objectName)[0];
		textField.setAttribute("readOnly","true");
	}-*/;
	
	
	public void newBackup(int defaultIncrementalJobPeriod, String backupDirectory) {
		incrementalJobPeriod = defaultIncrementalJobPeriod;
		this.backupDirectory = backupDirectory;
		createForm(true);        
	}
	
	protected ClickHandler startButtonClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			//JsObject j = JS
			
			//com.google.gwt.user.client.Window.alert("BackupType: " + comboBackupType.getValue());

			JSONObject incrementalBackupJobConfig = new JSONObject();
			JSONObject fullBackupJobConfig = new JSONObject();
			
			JSONObject obj = new JSONObject();
			obj.put(INCREMENTAL_REPETITION_NUMBER, new JSONNumber(Integer.parseInt("" + txtIncrementalRepetitionNumber.getValue())));
			obj.put(INCREMENTAL_BACKUP_JOB_CONFIG, incrementalBackupJobConfig);
			obj.put(BACKUP_TYPE, new JSONNumber("full only".equals(comboBackupType.getValue()) ? 0 : 1));
			obj.put(FULL_BACKUP_JOB_CONFIG, fullBackupJobConfig);
			obj.put(INCREMENTAL_JOB_PERIOD, new JSONNumber(Integer.parseInt("" + txtIncrementalJobPeriod.getValue())));
			//obj.put(BACKUP_DIR, new JSONString("/444"));
			obj.put(BACKUP_DIR, new JSONString(backupDirectory));
			
			//String test = obj.toString();
			//com.google.gwt.user.client.Window.alert("JSON: " + test);
			
//			com.google.gwt.user.client.Window.alert("Repo: " + txtRepository.getValue());
//			com.google.gwt.user.client.Window.alert("Ws: " + txtWorkspace.getValue());
			
			String requestURL = BackupManager.getInstance().getURLPrefix() + "/rest/jcr-backup/start/" + txtRepository.getValue() + "/" + txtWorkspace.getValue();
			//com.google.gwt.user.client.Window.alert("URL: " + requestURL);
			
//			if (true) {
//				return;
//			}
			
			try {
				RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, requestURL);
				requestBuilder.setHeader("Content-Type", "application/json; charset=UTF-8");
				requestBuilder.sendRequest(obj.toString(), new RequestCallback() {

					public void onError(Request request, Throwable throwable) {
						SC.warn("Can't connect the server!", null);
					}

					public void onResponseReceived(Request request, Response response) {
						if (response.getStatusCode() == 200) {
							SC.say("New backup started!");
							delayedUpdateBackupsTimer.schedule(1000);
							destroy();
						} else {
							SC.warn("Can't start new backup! Status code: " + response.getStatusCode(), null);
						}
					}
					
				});
				
			} catch (Exception exc) {
				System.out.println("Unhandled exception. " + exc.getMessage());
				exc.printStackTrace();
				SC.warn("Unknown error!", null);
			}
			
			
		}
	};
	
	protected Timer delayedUpdateBackupsTimer = new Timer() {
		public void run() {
			BackupManager.getInstance().refreshBaskupsList();			
		}
	};
	
	protected boolean isWorking = false;
	
	protected String backupId;
	
	public void editBackup(ListGridRecord backup) {
		String []ws = backup.getAttribute(WORKSPACE_NAME).split("/");
		repository = ws[0];
		workspace = ws[1];		
		backupType = backup.getAttribute(BACKUP_TYPE);
		
		if ("Working".equals(backup.getAttribute(STATE))) {
			isWorking = true;
			backupId = backup.getAttribute(BACKUP_ID);
		}
		
		createForm(false);
	}
	
	
	
	protected ClickHandler stopButtonClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {

			try {
				String requestURL = BackupManager.getInstance().getURLPrefix() + "/rest/jcr-backup/stop/" + backupId + "?" + Random.nextDouble();

				RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, requestURL);

				requestBuilder.sendRequest("", new RequestCallback() {
					public void onError(Request request, Throwable throwable) {
						SC.warn("Can't connect the server!", null);
					}

					public void onResponseReceived(Request request, Response response) {
						if (response.getStatusCode() == 200) {
							SC.say("Backup stopped!");
							delayedUpdateBackupsTimer.schedule(1000);
							destroy();
						} else {
							SC.warn("Can't stop backup! Status code: " + response.getStatusCode(), null);
						}
					}
				});
				
				
			} catch (Exception exc) {
				System.out.println("Unhandled exception. " + exc.getMessage());
				exc.printStackTrace();
				SC.warn("Unknown error!", null);
			}
			
		}
	};

}
