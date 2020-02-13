import ExoModal from './modal/ExoModal.vue';
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './drawer/ExoGroupBindingDrawer.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions' : ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces' : ExoSpacesAdministrationSpaces,
  'exo-modal' : ExoModal,
  'exo-group-binding-drawer' : ExoGroupBindingDrawer
};

for(const key in components) {
  Vue.component(key, components[key]);
}