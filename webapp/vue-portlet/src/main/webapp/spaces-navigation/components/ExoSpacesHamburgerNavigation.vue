<template>
  <v-container px-0 pt-0 class="border-box-sizing">
    <v-row class="mx-0 clickable spacesNavigationTitle">
      <v-list-item link @mouseover="openDrawer()" @click="openDrawer()">
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon">
          <i class="uiIcon uiIconToolbarNavItem spacesIcon"></i>
        </v-list-item-icon>
        <v-list-item-content class="subtitle-1 titleLabel">
          {{ $t('menu.spaces.lastVisitedSpaces') }}
        </v-list-item-content>
        <v-list-item-action class="my-0">
          <i class="uiIcon uiArrowRightIcon" color="grey lighten-1"></i>
        </v-list-item-action>
      </v-list-item>
    </v-row>
    <exo-spaces-navigation-content :limit="spacesLimit" shaped />
  </v-container>
</template>
<script>
import RecentSpacesHamburgerNavigation from './ExoRecentSpacesHamburgerNavigation.vue';

export default {
  data() {
    return {
      spaces : [],
      spacesLimit: 5,
      secondLevelVueInstance: null,
    };
  },
  methods: {
    mountSecondLevel(parentId) {
      if (!this.secondLevelVueInstance) {
        const VueHamburgerMenuItem = Vue.extend(RecentSpacesHamburgerNavigation);
        const vuetify = this.vuetify;
        this.secondLevelVueInstance = new VueHamburgerMenuItem({
          i18n: new VueI18n({
            locale: this.$i18n.locale,
            messages: this.$i18n.messages,
          }),
          vuetify,
          el: parentId,
        });
      } else {
        const element = $(parentId)[0];
        element.innerHTML = '';
        element.appendChild(this.secondLevelVueInstance.$el);
      }
      this.$nextTick().then(() => {
        this.secondLevelVueInstance.$on('close-menu', () => {
          this.$emit('close-second-level');
        });
      });
    },
    openDrawer() {
      this.$emit('open-second-level');
    },
  },
};
</script>
