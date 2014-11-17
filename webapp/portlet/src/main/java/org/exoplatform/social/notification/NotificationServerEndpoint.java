package org.exoplatform.social.notification;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.exoplatform.commons.notification.impl.service.Message;
import org.exoplatform.commons.notification.impl.service.Message.MessageDecoder;
import org.exoplatform.commons.notification.impl.service.Message.MessageEncoder;


@ServerEndpoint(value = "/notify/{remoteId}", encoders = { MessageEncoder.class }, decoders = { MessageDecoder.class })
public class NotificationServerEndpoint {
  
  private static Map<String, Session> connectedUserSessions = new ConcurrentHashMap<String, Session>();
  public NotificationServerEndpoint() {
    System.out.println("Constructed!");
  }
  
  @OnOpen
  public void onOpen (Session session, @PathParam("remoteId") final String remoteId) {
     System.out.println ("WebSocket opened: "+session.getId());
     if (!connectedUserSessions.containsKey(remoteId)) {
       connectedUserSessions.put(remoteId, session);
       System.out.println("onOpen " + remoteId + ":" + session.getId() + " number of users: " + connectedUserSessions.size());
     } else if (!connectedUserSessions.get(remoteId).isOpen()) {
       connectedUserSessions.remove(remoteId);
       connectedUserSessions.put(remoteId, session);
     }
  }

  @OnMessage
  public void onMessage (Session session, Message message) {
    System.out.println("Received: " + message.getTo());
    try {
      Session targetSession = connectedUserSessions.get(message.getTo());
      if (targetSession != null) {
        targetSession.getBasicRemote().sendObject(message);
        System.out.println("SendTo " + message.getTo() + ":" + targetSession.getId());
      }
    } catch (IOException e) {
      System.out.println("Error when sending message on server 1");
    } catch (EncodeException e) {
      System.out.println("Error when sending message on server 2");
    } 
  } 
  
  @OnClose
  public void onClose (Session session, CloseReason reason) {
     System.out.println ("Closing a WebSocket due to "+reason.getReasonPhrase());
  }
  
  @OnError
  public void onError (Session session, Throwable throwable) {
  }
}