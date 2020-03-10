<template>
  <v-container px-0 pt-0 class="border-box-sizing" @mouseover="openDrawer()">
    <v-row class="mx-0 spacesNavigationTitle">
      <v-list-item>
        <v-list-item-icon class="mb-2 mt-3 mr-6 titleIcon"><i class="uiIcon uiIconToolbarNavItem spacesIcon"></i></v-list-item-icon>
        <v-list-item-content class="subtitle-1 titleLabel">
          {{ this.$t('homepage.spaces.title') }}
        </v-list-item-content>

        <v-list-item-action class="my-0">
          <i class="uiIcon uiArrowRightIcon" color="grey lighten-1"></i>
        </v-list-item-action>

      </v-list-item>
    </v-row>
    <v-row class="mx-0 spacesNavigationContent">
      <v-list 
        class="flex pa-2" 
        shaped 
        dense>
        <v-list-item
          v-for="space in spacesList"
          :key="space.spaceLink"
          class="ml-12 px-2 spaceItem"
          @click="navigateTo(space.link)">
          <v-list-item-avatar 
            size="26"
            class="mr-3 tile my-0 spaceAvatar"
            tile>
            <v-img :src="space.avatar"/>
          </v-list-item-avatar>
          <v-list-item-content class="body-2">{{ space.spaceName }}</v-list-item-content>
        </v-list-item>
      </v-list>
    </v-row>
  </v-container>
</template>
<script>
export default {
  data() {
    return {
      spacesList : [
        { spaceName: 'eXo Employees',
          avatar: 'https://images.unsplash.com/photo-1534531173927-aeb928d54385?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80',
          spaceLink:'#'},
        { spaceName: 'EXo Product management',
          avatar: 'https://images.unsplash.com/photo-1474564862106-1f23d10b9d72?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=768&q=80',
          spaceLink:'#'
        },
        { spaceName: 'EXo platform',
          avatar: 'https://images.unsplash.com/photo-1466065478348-0b967011f8e0?ixlib=rb-1.2.1&auto=format&fit=crop&w=744&q=80',
          spaceLink:'#'
        },
        { spaceName: 'EXo Designers',
          avatar: 'https://images.unsplash.com/photo-1517923564953-4454fb2c99df?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=750&q=80',
          spaceLink:'#' },
        { spaceName: 'EXo Solidarity',
          avatar: 'https://images.unsplash.com/photo-1437750769465-301382cdf094?ixlib=rb-1.2.1&auto=format&fit=crop&w=755&q=80',
          spaceLink:'#'},
      ],
      drawer: null,
    };
  },
  methods: {
    mountSecondLevel(parentId) {
      const VueHamburgerMenuItem = Vue.extend({
        template: `
          <exo-recent-spaces-hamburger-menu-navigation />
        `,
      });
      const vuetify = this.vuetify;
      new VueHamburgerMenuItem({
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify,
      }).$mount(parentId);
    },
    openDrawer() {
      this.$emit('open-second-level');
    }
  }
};
</script>
