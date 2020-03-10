import ExoTopBarLogo from './components/ExoTopBarLogo.vue';

const components = {
  'exo-top-bar-logo': ExoTopBarLogo,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
