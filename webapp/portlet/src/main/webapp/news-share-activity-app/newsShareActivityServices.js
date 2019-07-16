import {spacesConstants} from '../js/spacesConstants.js';

export function findUserSpaces(spaceName){
  return fetch(`${spacesConstants.SOCIAL_SPACES_API}suggest.${spacesConstants.format}?conditionToSearch=${spaceName}&currentUser=${spacesConstants.userName}&typeOfRelation=${spacesConstants.typeOfRelation}`,{
    headers:{
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then(resp =>  resp.json()).then(json => json.options);
}