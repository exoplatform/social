import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarLogo');
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

export function init() {
  new Vue({
    template: '<exo-top-bar-logo></exo-top-bar-logo>',
    vuetify,
  }).$mount('#TopBarLogo');
}
