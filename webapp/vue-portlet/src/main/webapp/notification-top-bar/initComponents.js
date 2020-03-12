import ExoTopBarNotification from './components/ExoTopBarNotification.vue';

const components = {
  'exo-top-bar-notification': ExoTopBarNotification,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
