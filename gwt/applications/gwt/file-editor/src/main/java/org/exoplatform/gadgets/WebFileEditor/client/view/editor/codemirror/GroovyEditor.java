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

public class GroovyEditor extends CodeMirrorEditor {
  
  public GroovyEditor(WebFileEditor mainPanel) {
    super(mainPanel);
  }
  
  @Override
  protected String getParserName() {
    return "parsegroovy";
  }

  @Override
  protected String getTokenizerName() {
    return "tokenizegroovy";
  }
  
  @Override
  protected String getStylesheetName() {
    return "groovycolors";
  }
}
