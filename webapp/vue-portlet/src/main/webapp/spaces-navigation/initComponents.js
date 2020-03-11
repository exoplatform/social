import ExoSpacesHamburgerNavigation from './components/ExoSpacesHamburgerNavigation.vue';
import ExoRecentSpacesHamburgerNavigation from './components/ExoRecentSpacesHamburgerNavigation.vue';
import ExoSpacesNavigationContent from './components/ExoSpacesNavigationContent.vue';

const components = {
  'exo-spaces-hamburger-menu-navigation': ExoSpacesHamburgerNavigation,
  'exo-recent-spaces-hamburger-menu-navigation': ExoRecentSpacesHamburgerNavigation,
  'exo-spaces-navigation-content': ExoSpacesNavigationContent,
};

for(const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationSpaces',
      priority: 20,
      secondLevel: true,
      vueComponent: ExoSpacesHamburgerNavigation,
    },
  );
}
