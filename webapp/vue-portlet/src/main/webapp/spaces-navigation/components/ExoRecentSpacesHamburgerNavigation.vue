<template>
  <v-container class="recentDrawer" flat>
    <v-flex class="filterSpaces">
      <v-list-item class="recentSpacesTitle">
        <v-list-item-icon class="d-flex d-sm-none backToMenu" @click="closeMenu()">
          <i class="uiIcon uiArrowBackIcon"></i>
        </v-list-item-icon>
        <v-list-item-content v-if="showFilter" class="recentSpacesTitleLabel body-1">
          <v-text-field
            v-model="keyword"
            placeholder="Filter spaces here"
            class="recentSpacesFilter body-1 pt-0"
            single-line
            hide-details
            required
            autofocus/>
        </v-list-item-content>
        <v-list-item-content v-else class="recentSpacesTitleLabel body-1" @click="showFilter = true">
          {{ $t('menu.spaces.recentSpaces') }}
        </v-list-item-content>
        <v-list-item-action class="recentSpacesTitleIcon">
          <v-btn
            v-if="showFilter"
            text
            icon
            color="blue-grey darken-1"
            size="22"
            @click="closeFilter()">
            <v-icon size="18">mdi-close</v-icon>
          </v-btn>
          <v-btn
            v-else
            text
            icon
            color="blue-grey darken-1"
            size="20"
            @click="showFilter = true">
            <v-icon size="18" class="uiSearchIcon"/>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-flex>
    <v-divider class="my-0"/>
    <v-list
      dense
      nav
      class="recentSpacesWrapper">
      <v-list-item :href="allSpacesLink" class="addSpaces my-2">
        <v-list-item-avatar class="mr-3" size="22" tile>
          <i class="uiPlusEmptyIcon"></i>
        </v-list-item-avatar>
        <v-list-item-content class="py-0 body-2 grey--text darken-4">
          {{ $t('menu.spaces.createSpace') }}
        </v-list-item-content>
      </v-list-item>
    </v-list>
    <exo-spaces-navigation-content :limit="itemsToShow" :page-size="itemsToShow" :keyword="keyword" show-more-button class="recentSpacesWrapper" />
  </v-container>
</template>
<script>
export default {
  data () {
    return {
      itemsToShow: 15,
      showFilter: false,
      allSpacesLink: `${eXo.env.portal.context}/${ eXo.env.portal.portalName }/all-spaces`,
      keyword: '',
    };
  },
  methods: {
    closeMenu() {
      this.$emit('close-menu');
    },
    closeFilter() {
      this.keyword = '';
      this.showFilter = false;
    },
    getSpacesPage(item) {
      if (this.itemsToShow <= this.spacesList.length) {
        const l = this.spacesList.length - this.itemsToShow;
        if( l > item ) {
          this.itemsToShow+=item;
        } else {
          this.itemsToShow+=l;
          this.showButton = false;
        }
      }
    }
  }
};
</script>
