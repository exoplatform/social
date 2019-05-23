import { spacesConstants } from '../js/spacesConstants.js';

export function getTemplates(){
  // getting language of the PLF
  const lang = window && window.eXo && window.eXo.env && window.eXo.env.portal && window.eXo.env.portal.language || 'en';
  return fetch(`${spacesConstants.SPACES_TEMPLATES_API}/templates?lang=${lang}`, {credentials: 'include'}).then(resp => resp.json());
}