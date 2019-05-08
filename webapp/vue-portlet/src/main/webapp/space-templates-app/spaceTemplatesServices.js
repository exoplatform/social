import { spacesConstants } from '../js/spacesConstants.js';

export function getTemplates(){
  return fetch(`${spacesConstants.SPACES_TEMPLATES_API}/templates`, {credentials: 'include'}).then(resp => resp.json());
}