import ExoModal from './modal/ExoModal.vue';
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './ExoSpacesAdministrationSpaces.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions' : ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces' : ExoSpacesAdministrationSpaces,
  'exo-modal' : ExoModal
};

for(const key in components) {
  Vue.component(key, components[key]);
}