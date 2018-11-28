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

export function getGuests(query) {
  return fetch(`/portal/rest/social/people/suggest.json?nameToSearch=${query}`, {credentials: 'include'}).then(resp => resp.json());
}