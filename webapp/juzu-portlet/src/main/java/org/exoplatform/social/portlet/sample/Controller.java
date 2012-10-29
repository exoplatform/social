package org.exoplatform.social.portlet.sample;

import org.exoplatform.web.application.RequestContext;
import juzu.Action;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.SessionScoped;
import juzu.View;
import juzu.plugin.ajax.Ajax;
import juzu.template.Template;

import org.exoplatform.social.portlet.sample.templates.index;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class Controller {

  @Inject @Path("index.gtmpl")    index index;
 
  @View
  public void index() throws IOException, Exception {
    index.render();
  }

}
