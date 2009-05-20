package org.exoplatform.gadgets.WebFileEditor.client.model;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestPropfindResponseTest extends GWTTestCase {
   private static String XML1 = "<?xml version=\"1.0\" ?><D:multistatus xmlns:D=\"DAV:\" "+
   "xmlns:b=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\"><D:response><D:href>"+
   "http://localhost:8080/lightportal/rest/jcr/lightrep/production/exo:portal/</D:href>"+
   "<D:propstat><D:prop><D:creationdate b:dt=\"dateTime.tz\">2009-05-10T16:19:39Z</D:creationdate>"+
   "<jcr:mixinTypes xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">exo:owneable</jcr:mixinTypes>"+
   "<exo:owner xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\">__system</exo:owner>" +
   "<D:displayname>exo:portal</D:displayname><D:resourcetype><D:collection/></D:resourcetype>" +
   "</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response>" + 
   "<D:href>http://localhost:8080/lightportal/rest/jcr/lightrep/production/exo:portal/exo:pages/</D:href>"+
   "<D:propstat><D:prop><jcr:mixinTypes xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">exo:owneable</jcr:mixinTypes>"+
   "<D:displayname>exo:pages</D:displayname><D:creationdate b:dt=\"dateTime.tz\">2009-05-10T16:19:39Z</D:creationdate>"+
   "<D:resourcetype><D:collection/></D:resourcetype><exo:owner xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\">"+
   "__system</exo:owner></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response>"+
   "<D:response><D:href>http://localhost:8080/lightportal/rest/jcr/lightrep/production/exo:portal/exo:skins/</D:href>"+
   "<D:propstat><D:prop><jcr:mixinTypes xmlns:jcr=\"http://www.jcp.org/jcr/1.0\">exo:owneable</jcr:mixinTypes>"+
   "<D:creationdate b:dt=\"dateTime.tz\">2009-05-10T16:19:40Z</D:creationdate><D:resourcetype>"+
   "<D:collection/></D:resourcetype><D:displayname>exo:skins</D:displayname>"+
   "</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response><D:response>"+
   "<D:href>http://localhost:8080/lightportal/rest/jcr/lightrep/production/exo:portal/exo:resources/</D:href>"+
   "<D:propstat><D:prop><D:creationdate b:dt=\"dateTime.tz\">2009-05-10T16:19:41Z</D:creationdate>"+
   "<D:displayname>exo:resources</D:displayname><D:resourcetype><D:collection/></D:resourcetype>"+
   "</D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
    
    public GwtTestPropfindResponseTest() {
    }    
    
    
    
    @Override
    public String getModuleName() {
      // TODO Auto-generated method stub
      return "org.exopatform.gadgets.WebFileEditor.WebFileEditor";
    }



    public void testParser()  {
        
        PropfindResponse resp = PropfindResponse.parse(XML1);
        //System.out.println(">>>>>>>>"+resp.getParent().getDisplayname());
    }
    
}