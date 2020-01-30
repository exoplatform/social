import { spacesConstants } from '../js/spacesConstants.js';

export function getOnlineUsers(spaceId){
  if (spaceId) {
    return fetch(`${spacesConstants.SOCIAL_USER_API}?status=online&spaceId=${spaceId}`, {credentials: 'include'}).then(resp => resp.json());
  }
  return fetch(`${spacesConstants.SOCIAL_USER_API}?status=online`, {credentials: 'include'}).then(resp => resp.json());
}

