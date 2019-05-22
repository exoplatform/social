import {spacesConstants} from '../js/spacesConstants.js';

export function saveNewsActivity(activity) {
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}${spacesConstants.SPACE_ID}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(activity)
  });
}