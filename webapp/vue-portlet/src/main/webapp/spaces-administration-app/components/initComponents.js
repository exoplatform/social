import ExoModal from './modal/ExoModal.vue';
import ExoSpacesAdministrationManageSpaces from './ExoSpacesAdministrationManageSpaces.vue';
import ExoSpacesAdministrationSpacesPermissions from './ExoSpacesAdministrationSpacesPermissions.vue';
import ExoSpacesAdministrationSpaces  from './ExoSpacesAdministrationSpaces.vue';
import ExoGroupBindingDrawer from './drawer/ExoGroupBindingDrawer.vue';
import ExoGroupBindingSecondLevelDrawer from './drawer/ExoGroupBindingSecondLevelDrawer.vue';
import ExoSpacesAdministrationBindingReports from './ExoSpacesAdministrationBindingReports.vue';
import ExoSuggester from './suggester/ExoSuggester.vue';

const components = {
  'exo-spaces-administration-manage-spaces': ExoSpacesAdministrationManageSpaces,
  'exo-spaces-administration-manage-permissions' : ExoSpacesAdministrationSpacesPermissions,
  'exo-spaces-administration-spaces' : ExoSpacesAdministrationSpaces,
  'exo-modal' : ExoModal,
  'exo-group-binding-drawer' : ExoGroupBindingDrawer,
  'exo-group-binding-second-level-drawer' : ExoGroupBindingSecondLevelDrawer,
  'exo-spaces-administration-binding-reports' : ExoSpacesAdministrationBindingReports,
  'exo-suggester' : ExoSuggester,
};

for(const key in components) {
  Vue.component(key, components[key]);
}