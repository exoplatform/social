package org.exoplatform.social.portlet.sample;

import java.io.IOException;

import javax.inject.Inject;

import org.exoplatform.social.portlet.sample.templates.index;

import juzu.Path;
import juzu.View;






public class Controller {

  @Inject @Path("index.gtmpl")    index index;
 
  @View
  public void index() throws IOException, Exception {
    index.render();
  }

}
