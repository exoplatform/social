import './initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

//getting language of the PLF 
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarNotification');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

//getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      el: '#TopBarNotification',
      template: '<exo-top-bar-notification></exo-top-bar-notification>',
      i18n,
      vuetify,
    });
  });
}