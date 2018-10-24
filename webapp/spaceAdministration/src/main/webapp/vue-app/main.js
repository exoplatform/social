import Vue from 'vue';
import ExoSpacesAdministrationApp from './components/ExoSpacesAdministrationApp.vue';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  
// should expose the locale ressources as REST API 
const url = '/spaceAdministration/vue-app/locale_en.json';

// getting locale ressources
exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  console.log(i18n)
// init Vue app when locale ressources are ready
  new Vue({
    el: '#spacesAdministration',
    render: h => h(ExoSpacesAdministrationApp),
    i18n
  })
});