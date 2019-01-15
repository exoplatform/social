import './components/initComponents.js';
import {spaceConstants} from './spaceAdministrationConstants.js';
import * as spacesAdministrationDirectives from './spacesAdministrationDirectives.js';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `${spaceConstants.PORTAL}/${spaceConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SpacesAdministrationPortlet-${lang}.json`;

Vue.directive('exo-tooltip', spacesAdministrationDirectives.tooltip);

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesAdministration');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  // init Vue app when locale ressources are ready
    new Vue({
      el: '#spacesAdministration',
      template: '<exo-spaces-administration-spaces></exo-spaces-administration-spaces>',
      i18n
    });
  });
}