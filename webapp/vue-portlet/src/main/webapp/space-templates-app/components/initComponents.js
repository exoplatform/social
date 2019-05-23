import ExoSpacesTemplatesSpaces  from './ExoSpacesTemplatesSpaces.vue';

const components = {
  'exo-space-templates-spaces' : ExoSpacesTemplatesSpaces
};

for(const key in components) {
  Vue.component(key, components[key]);
}