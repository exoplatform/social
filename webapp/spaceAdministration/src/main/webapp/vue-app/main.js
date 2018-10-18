import Vue from 'vue';
import app from './components/app.vue';

// getting language of the PLF 
//const lang = eXo ? eXo.env ? eXo.env.portal ? eXo.env.portal.language ? eXo.env.portal.language : 'en' : 'en' : 'en' : 'en';
  
// should expose the locale ressources as REST API
//const url = `/vue-webpack-sample/mocks/locale_${lang}.json`;

// getting locale ressources
//exoi18n.loadLanguageAsync(lang, url).then(i18n => {
// init Vue app when locale ressources are ready
new Vue({
  el: '#space-administration',
  render: h => h(app)
});
//})