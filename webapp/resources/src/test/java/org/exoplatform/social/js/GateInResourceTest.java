/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.js;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.common.io.IOTools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.script.Module;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Oct
 * 22, 2013
 */
public class GateInResourceTest extends TestCase {

  private InputStream is;

  private ClassLoader classLoader;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    classLoader = GateInResourceTest.class.getClassLoader();
    is = classLoader.getResourceAsStream("WEB-INF/gatein-resources.xml");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    is.close();
  }

  public void testReadXML() throws Exception {
    JavascriptConfigParser parser = new JavascriptConfigParser("eXoSocialResources");
    List<ScriptResourceDescriptor> scriptResources = parser.parseConfig(is);
    assertTrue(scriptResources.size() > 0);

    StringBuilder msg = new StringBuilder();

    for (ScriptResourceDescriptor js : scriptResources) {
      msg.append(verify(js));
    }
    
    String result = msg.toString();
    if (result.length() > 0) {
      fail(result);
    }
  }

  private String verify(ScriptResourceDescriptor resource) throws Exception {
    StringBuilder msg = new StringBuilder();
    for (Javascript js : resource.getModules()) {
      Javascript.Local local = (Javascript.Local) js;
      Module.Local.Content[] contents = local.getContents();
      if (contents.length > 0) {
        Module.Local.Content content = contents[0];
        msg.append(compile(content.getSource(), js.getResource()));
      }
    }
    
    return msg.toString();

  }

  private String compile(String jsPath, ResourceId key) throws Exception {

    String sourceName = key.getScope() + "/" + key.getName() + ".js";
    jsPath = jsPath.substring(1);
    InputStream in = classLoader.getResourceAsStream(jsPath);
    Reader script = new InputStreamReader(in);

    CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
    CompilerOptions options = new CompilerOptions();
    level.setOptionsForCompilationLevel(options);
    StringWriter code = new StringWriter();
    IOTools.copy(script, code);
    com.google.javascript.jscomp.Compiler compiler = new Compiler();
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode(sourceName, code.toString()) };
    Result res = compiler.compile(new JSSourceFile[0], inputs, options);
    StringBuilder msg = new StringBuilder();
    if (res.success == false) {
      msg.append("Handle me gracefully JS errors\n");
      for (JSError error : res.errors) {
        msg.append(error.sourceName).append(":").append(error.lineNumber).append(" ").append(error.description).append("\n");
      }
    }
    
    return msg.toString();
  }

}
