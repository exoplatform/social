import {spaceConstants} from './spaceAdministrationConstants.js';

export function getSpaces(){
  return fetch(`${spaceConstants.SOCIAL_SPACE_API}?limit=${spaceConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function searchSpaces(search){
  return fetch(`${spaceConstants.SOCIAL_SPACE_API}?q=${search}&limit=${spaceConstants.SPACES_PER_PAGE}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesPerPage(offset){
  return fetch(`${spaceConstants.SOCIAL_SPACE_API}?offset=${offset}&limit=${spaceConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function deleteSpaceById(id){
  return fetch(`/rest/v1/social/spaces/${id}`, {
    credentials: 'include', 
    method: 'delete'});
}

export function getSpaceLinkSetting(spaceDisplayName){
  const nameSpace = spaceDisplayName.toLowerCase().split(' ').join('_');
  return `${spaceConstants.PORTAL}${spaceConstants.PROFILE_SPACE_LINK}${nameSpace}/${nameSpace}/settings`;
}

export function getAvatar(user) {
  return `${spaceConstants.SOCIAL_USER_API}${user}/avatar`;
}

export function getUsers(query) {
  return fetch(`${spaceConstants.SOCIAL_PEOPLE_API}/suggest.json?nameToSearch=${query}`, {credentials: 'include'}).then(resp => resp.json(
  ));
}

export function getGroups(query) {
  return fetch(`${spaceConstants.GROUP_API}?q=${query}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesAdministrationSetting(key){
  return fetch(`${spaceConstants.SPACES_ADMINISTRATION_API}/${key}`, {
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
  return fetch(`${spaceConstants.SPACES_ADMINISTRATION_API}/${key}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(value)
  });
}

