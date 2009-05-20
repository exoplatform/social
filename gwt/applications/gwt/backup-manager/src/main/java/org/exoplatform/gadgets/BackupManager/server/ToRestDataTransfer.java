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
package org.exoplatform.gadgets.BackupManager.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class ToRestDataTransfer extends HttpServlet {

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		try {
			System.out.println("SERVICE called!!!!!!!!");

			String method = request.getMethod();			
			System.out.println("METHOD: " + method);
			
			byte []requestData = new byte[0];
			String sContentLength = request.getHeader("content-length");
			if (sContentLength != null) {
				System.out.println("SContentLength: " + sContentLength);
				int iContentLength = Integer.parseInt(sContentLength);
				requestData = new byte[iContentLength];
				request.getInputStream().read(requestData);
				System.out.println("Request: " + new String(requestData));
			}
			
			
			HttpClient client = new HttpClient("localhost", 8080);
			
			client.setHttpCommand(method);
			
			Enumeration e = request.getHeaderNames();
			while (e.hasMoreElements()) {
				String name = (String)e.nextElement();
				String value = request.getHeader(name);
				client.setRequestHeader(name, value);
				System.out.println("Name [" + name + ": " + value + "]");
			}
			
			String path = request.getPathInfo();
			
			System.out.println("Path to: " + path);
			
			path = path.substring("/org.exoplatform.gadgets.BackupManager.BackupManager".length());
			
			System.out.println("Path after: " + path);

			client.setRequestPath(path);
			
			client.setRequestBody(requestData);
			
			client.conect();
			
			int status = client.execute();
			
			System.out.println("STATUS: " + status);
			
			ArrayList<String> hNames = client.getResponseHeadersNames();
			for (int i = 0; i < hNames.size(); i++) {
				String name = hNames.get(i);
				String value = client.getResponseHeader(name);
				response.setHeader(name, value);
				System.out.println("Response header [" + name + ": " + value + "]");
			}
			
			response.setStatus(status);

			byte []resp = client.getResponseBytes();
			response.getOutputStream().write(resp);
			
			System.out.println("RESPONSE: " + new String(resp));
			
			response.flushBuffer();
			
			
		} catch (Exception exc) {
			System.out.println("Unhandled exception. " + exc.getMessage());
			exc.printStackTrace();
			
			response.setStatus(500);
		}
		
		//super.service(arg0, arg1);
	}
	
	
}
