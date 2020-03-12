import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ProfileHamburgerNavigation');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//add menu entry in Hamburger Menu
document.dispatchEvent(new CustomEvent('exo-hamburger-menu-navigation-refresh'));
