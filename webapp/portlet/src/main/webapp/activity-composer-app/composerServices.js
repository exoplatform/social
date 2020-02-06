export function postMessageInSpace(message, spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': '',
      'templateParams': {}
    })
  }).then((data) => {
    return data.json();
  });
}

export function postMessageInUserStream(message, userName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${userName}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': '',
      'templateParams': {}
    })
  }).then((data) => {
    return data.json();
  });
}