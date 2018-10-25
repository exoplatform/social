import {spaceConstants} from './spaceAdministrationConstants.js';

export function getSpaces(){
  return fetch('/rest/v1/social/spaces/', {credentials: 'include'}).then(resp => resp.json());
}

export function deleteSpaceById(id){
  return fetch(`/rest/v1/social/spaces/${id}`, {
    credentials: 'include', 
    method: 'delete'});
}

export function getSpaceSetting(spaceDisplayName){
  const nameSpace = spaceDisplayName.toLowerCase().split(' ').join('_');
  return `${spaceConstants.PORTAL}${spaceConstants.PROFILE_SPACE_LINK}${nameSpace}/${nameSpace}/settings`;
}