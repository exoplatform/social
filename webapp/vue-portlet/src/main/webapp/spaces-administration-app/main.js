import Vue from 'vue';
import ExoSpacesAdministrationSpaces from './components/ExoSpacesAdministrationSpaces.vue';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  
// should expose the locale ressources as REST API 
const url = '/social-vue-portlet/spaces-administration-app/locale_en.json';

// getting locale ressources
exoi18n.loadLanguageAsync(lang, url).then(i18n => {

// init Vue app when locale ressources are ready
  new Vue({
    el: '#spacesAdministration',
    render: h => h(ExoSpacesAdministrationSpaces),
    i18n
  })
});