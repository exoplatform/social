import { spacesConstants } from '../js/spacesConstants.js';

export function getSpaces(){
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}?sort=date&order=desc&limit=${spacesConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function searchSpaces(search){
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}?q=${search}&sort=date&order=desc&limit=${spacesConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesPerPage(offset){
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}?offset=${offset}&sort=date&order=desc&limit=${spacesConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function deleteSpaceById(id){
  return fetch(`/rest/v1/social/spaces/${id}`, {
    credentials: 'include', 
    method: 'delete'});
}

export function getSpaceLinkSetting(spaceDisplayName, groupId) {
  if(spaceDisplayName && groupId) {
    const spaceName = spaceDisplayName.toLowerCase().split(' ').join('_');
    const groupIdTab = groupId.toLowerCase().split('/');
    const groupName  = groupIdTab[groupIdTab.length-1];
    return `${spacesConstants.PORTAL}${spacesConstants.PROFILE_SPACE_LINK}${groupName}/${spaceName}/settings`;
  } else {
    return null;
  }
}

export function getUserPermissions(userName) {
  return fetch(`${spacesConstants.USER_API}/${userName}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getGroups(query) {
  return fetch(`${spacesConstants.GROUP_API}?q=${query}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesAdministrationSetting(key){
  return fetch(`${spacesConstants.SPACES_ADMINISTRATION_API}/permissions/${key}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'GET'
  }).then(resp => {
    const HTTP_OK_CODE = 200;
    if(resp.status === HTTP_OK_CODE) {
      return resp.json();
    } else {
      return resp.text();
    }
  });
}

export function updateSpacesAdministrationSetting(key, value){
  return fetch(`${spacesConstants.SPACES_ADMINISTRATION_API}/permissions/${key}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(value)
  });
}

