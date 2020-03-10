export function getNotifications() {
  return fetch('/rest/notifications/webNotifications', {
    method: 'GET',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting notification list');
    }
  });
}

export function updateNotification(id, operation) {
  return fetch(`/portal/rest/notifications/webNotifications/${id}`, {
    headers: {
      'Content-Type': 'text/plain'
    },
    method: 'PATCH',
    body: operation
  });
}

export function getUserToken() {
  return fetch(`/rest/continuation/gettoken/${eXo.env.portal.userName}`, {
    method: 'GET',
  }).then((resp) => {
    if(resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error('Error when getting user token');
    }
  });
}

export function initCometd(userToken) {
  const loc = window.location;
  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${loc.port && ':' || ''}${loc.port || ''}/cometd/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: userToken,
  });

  cCometd.subscribe('/eXo/Application/web/NotificationMessage', null, (event) => {
    const data = JSON.parse(event.data);
    document.dispatchEvent(new CustomEvent('cometdNotifEvent', {'detail': {'data' : data}}));
  });
}