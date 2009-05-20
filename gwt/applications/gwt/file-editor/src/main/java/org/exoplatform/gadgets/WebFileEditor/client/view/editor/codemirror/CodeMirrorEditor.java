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

package org.exoplatform.gadgets.WebFileEditor.client.view.editor.codemirror;

import org.exoplatform.gadgets.WebFileEditor.client.WebFileEditor;
import org.exoplatform.gadgets.WebFileEditor.client.view.editor.ContentEditor;

abstract class CodeMirrorEditor implements ContentEditor {
  
  public WebFileEditor mainPanel;  
  
  public CodeMirrorEditor(WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }
  
  public void init(String content) { 
    String parserName = getParserName();
    String tokenizerName = getTokenizerName();
    String stylesheetName = getStylesheetName();
    
    javaScriptInit(content, parserName, tokenizerName, stylesheetName);
  }
  
  public native void javaScriptInit(String content, String parserName, String tokenizerName, String stylesheetName) /*-{
    var intervalId = setInterval(function() {
      var editorTextArea = $wnd.document.getElementById('WebFileEditor_editorTextArea');
      if (editorTextArea == null) return;
      if ($wnd.CodeMirror == null) return;
      clearInterval(intervalId);
      // gathering the gadget's URL from the properties url of document.URL
      var gadgetsURL = '';
      if ($wnd.gadgets != null) {
        gadgetsURL = $wnd.gadgets.util.getUrlParameters().url.match(/(.*)\//)[1] +  '/';
      }
      var meditor = new $wnd.MirrorFrame($wnd.CodeMirror.replace(editorTextArea), {
        content: content,
        parserfile: [tokenizerName + '.js', parserName + '.js'],
        stylesheet: gadgetsURL + 'CodeMirror/css/' + stylesheetName + '.css',
        path: gadgetsURL + 'CodeMirror/js/',
        autoMatchParens: false,
        width: "100%",
        height: "394px",
        onChange: function() {
          debugger;
          // set Save button enabled after content of editor is changed    
          var temp = typeof this.@org.exoplatform.gadgets.WebFileEditor.client.view.editor.codemirror.CodeMirrorEditor::setSaveButtonEnabled()();
          eval("org_exoplatform_gadgets_WebFileEditor_client_view_editor_codemirror_CodeMirrorEditor_setSaveButtonEnabled__();");
        },
        linesPerPass: 5,
        passDelay: 5              
      });
      $wnd.editor = meditor;
      var CodeMirror = $wnd.editor.mirror.win.frameElement.CodeMirror;

      var intervalIdsecond = setInterval(function() {          
        if ( CodeMirror.win.document.body == null ) return;
        clearInterval(intervalIdsecond);
        CodeMirror.editor = new $wnd.editor.mirror.win.Editor(CodeMirror);
        CodeMirror.editor.history.storedLength = CodeMirror.editor.history.history.length;
      }, 1000);
    }, 1000);
  }-*/;

  public native void setCode(String content) /*-{
    if (typeof $wnd.editor == "undefined") return;
    $wnd.editor.mirror.win.frameElement.CodeMirror.setCode(content);
    $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.storedLength = $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.history.length;
  }-*/;

  public native String getCode() /*-{
    if (typeof $wnd.editor == "undefined") return "";
    return $wnd.editor.mirror.win.frameElement.CodeMirror.getCode();
  }-*/;  

  public native boolean isContentModified() /*-{
    if (typeof $wnd.editor == "undefined") return;
    return $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.history.length != $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.storedLength;
  }-*/;
  
  public native void clearEditorHistory() /*-{
    if (typeof $wnd.editor == "undefined") return;
    $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.storedLength = $wnd.editor.mirror.win.frameElement.CodeMirror.editor.history.history.length;
  }-*/;
  
  public void setSaveButtonEnabled() {
    mainPanel.menuPanel.saveButton.setDisabled(false);
  }
  
  abstract protected String getParserName();
  
  abstract protected String getTokenizerName();

  abstract protected String getStylesheetName();
}
