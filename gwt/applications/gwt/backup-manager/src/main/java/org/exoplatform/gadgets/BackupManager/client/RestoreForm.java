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

import org.exoplatform.gadgets.BackupManager.client.config.WorkspaceConfig;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SpacerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class RestoreForm extends Window {

	protected static int WINDOW_WIDTH = 600;

	protected static int WINDOW_HEIGHT = 460;

	protected String backupId;
	
	protected TextItem txtId;
	
	protected boolean isNewRestore = true;
	
	protected WorkspaceConfig configurationForm;
	
	protected TextItem txtRepository;
	
	protected TextItem txtWorkspace;

	public RestoreForm() {
		setWidth(WINDOW_WIDTH);
		setHeight(WINDOW_HEIGHT);
		setTitle("Restore");
		setShowMinimizeButton(false);
		setIsModal(true);
		centerInPage();
	}

	public void createNew(String jsonDefaultConfig, String backupId) {
		this.backupId = backupId;

		createRestoreInfoForm();

		configurationForm = new WorkspaceConfig(WINDOW_WIDTH - 30, 260);
		addItem(configurationForm);
		configurationForm.parseConfiguration(jsonDefaultConfig);
		configurationForm.refreshTreeContent();
		
		createButtons();
		
		show();
	}

	public void showINfo() {
		isNewRestore = false;
		show();
	}

	protected void createRestoreInfoForm() {
		DynamicForm form1 = new DynamicForm();
		form1.setWidth(350);
		form1.setPadding(15);
		form1.setColWidths("150px", "*");
		form1.setLayoutAlign(VerticalAlignment.BOTTOM);
		form1.setLayoutAlign(Alignment.CENTER);
		// form1.setCellBorder(1);

		int textFieldsWidth = 200;

		txtId = new TextItem();
		txtId.setTitle("ID");
		txtId.setTitleAlign(Alignment.LEFT);
		txtId.setWidth(textFieldsWidth);
		txtId.setValue(backupId);
		// txtId.setAttribute("canEdit", false);

		txtRepository = new TextItem();
		txtRepository.setTitle("Repository");
		txtRepository.setTitleAlign(Alignment.LEFT);
		txtRepository.setWidth(textFieldsWidth);

		txtWorkspace = new TextItem();
		txtWorkspace.setTitle("Workspace:");
		txtWorkspace.setTitleAlign(Alignment.LEFT);
		txtWorkspace.setWidth(textFieldsWidth);
		
		setReadOnlyTimer.schedule(1);

		form1.setFields(txtId, txtRepository, txtWorkspace);
		addItem(form1);
	}
	
	protected void createButtons() {
		DynamicForm spaceForm = new DynamicForm();
    SpacerItem spaceItem = new SpacerItem();
    spaceItem.setHeight(5);		
		spaceForm.setFields(spaceItem);
		addItem(spaceForm);
		
		DynamicForm butForm = new DynamicForm();
		butForm.setWidth(150);
		butForm.setLayoutAlign(VerticalAlignment.BOTTOM);
		butForm.setLayoutAlign(Alignment.CENTER);
		//butForm.setCellBorder(1);
		
        
    IButton butStart = new IButton("Start");          
    butStart.setWidth("80px");
    if (!isNewRestore) {
    	butStart.disable();
    }
    butStart.addClickHandler(startButtonClickHandler);
    
    IButton butStop = new IButton("Stop");
    butStop.setWidth("80px");
    butStop.disable();
    
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
	}
	
  protected Timer setReadOnlyTimer = new Timer() {
		public void run() {
			setReadOnly(txtId.getName());
		}
  };
	
	private native void setReadOnly(String objectName) /*-{ 
	  if ($wnd.document.getElementsByName(objectName) == null) {
	    return;
	  }	  
	  var textField = $wnd.document.getElementsByName(objectName)[0];
		textField.setAttribute("readOnly","true");
	}-*/;

	protected ClickHandler startButtonClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			String repositoryName = "" + txtRepository.getValue();
			System.out.println("Repository: " + repositoryName);
			
			String workspaceName = "" + txtWorkspace.getValue();
			System.out.println("Workspace: " + workspaceName);
			
			JSONObject workspaceConfig = configurationForm.getConfiguration();
			
			String requestURL = BackupManager.getInstance().getURLPrefix() + "/rest/jcr-backup/restore/" + repositoryName + "/" + backupId + "?" + Random.nextDouble();

			try {
				com.google.gwt.user.client.Window.alert(workspaceConfig.toString());
				
				
			} catch (Exception exc) {
				System.out.println("Unhandled exception. " + exc.getMessage());
				exc.printStackTrace();
			}
			
		}
	};
	
}
