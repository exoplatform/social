import ExoProfileHamburgerNavigation from './components/ExoProfileHamburgerNavigation.vue';

const components = {
  'exo-profile-hamburger-menu-navigation': ExoProfileHamburgerNavigation,
};

for(const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationProfile',
      priority: -1,
      vueComponent: ExoProfileHamburgerNavigation,
    },
  );
}
