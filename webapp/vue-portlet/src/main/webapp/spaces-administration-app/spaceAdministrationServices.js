import {spaceConstants} from './spaceAdministrationConstants.js';

export function getSpaces(){
  return fetch(`/portal/rest/v1/social/spaces?limit=${spaceConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
}

export function searchSpaces(search){
  return fetch(`/portal/rest/v1/social/spaces?q=${search}&limit=${spaceConstants.SPACES_PER_PAGE}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesPerPage(offset){
  return fetch(`/portal/rest/v1/social/spaces?offset=${offset}&limit=${spaceConstants.SPACES_PER_PAGE}&returnSize=true`, {credentials: 'include'}).then(resp => resp.json());
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
  return fetch(`/portal/rest/social/people/suggest.json?nameToSearch=${query}`, {credentials: 'include'}).then(resp => resp.json(
  ));
}

export function getGroups(query) {
  return fetch(`/portal/rest/social/people/getGroups/suggest.json?search=${query}`, {credentials: 'include'}).then(resp => resp.json());
}

export function createSetting(context,scope,settingKey,valueKey){
  return fetch(`/rest/v1/settings/${context}/${scope}/${settingKey}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify({"value": valueKey})
  });
}

export function getsSettingValue(context,scope,settingKey) {
  return fetch(`/rest/v1/settings/${context}/${scope}/${settingKey}`, {
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
