import { spacesConstants } from '../js/spacesConstants.js';

export function getSpaceDescriptionByPrettyName(){
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}${spacesConstants.SPACE_ID}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpaceManagersByPrettyName(){
  return fetch(`${spacesConstants.SOCIAL_SPACE_API}${spacesConstants.SPACE_ID}${spacesConstants.MANAGERS_ROLE}`, {credentials: 'include'}).then(resp => resp.json()).catch(e => {console.log(e);});
}

