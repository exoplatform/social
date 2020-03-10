<template>
  <v-container class="recentDrawer elevation-2">
    <v-list-item 
      v-if="showFilter" 
      class="recentSpacesTitle">
      <v-list-item-icon class="d-flex my-3 d-sm-none backToMenu">
        <i class="uiArrowBackIcon"></i>
      </v-list-item-icon>
      <v-list-item-content class="recentSpacesTitleLabel body-1" @click="showFilter=!showFilter">
        {{ $t('menu.spaces.recentSpaces') }}
      </v-list-item-content>
      <v-list-item-action class="recentSpacesTitleIcon my-2" @click="showFilter=!showFilter">
        <v-btn
          text
          icon
          color="blue-grey darken-1"
          size="22">
          <v-icon size="18" class="uiSearchIcon"/>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <div v-else class="filterSpaces ma-2">
      <v-text-field
        v-model="search"
        label="Filter spaces here"
        class="ma-2 py-1 recentSpacesFilter body-1"
        single-line
        hide-details
        required
        autofocus/>
      <v-btn
        text
        icon
        color="blue-grey darken-1 searchClose"
        size="22"
        @click="closeFilter()">
        <v-icon size="18">mdi-close</v-icon>
      </v-btn>
    </div>

    <v-divider class="my-0"/>
    <v-list
      dense
      nav
      class="recentSpacesWrapper">
      <v-list-item class="addSpaces my-2" @click="navigateTo('all-spaces')">
        <v-list-item-avatar 
          class="mr-3"
          size="22"
          tile>

        <i class="uiPlusEmptyIcon"></i></v-list-item-avatar>
        <v-list-item-content class="py-0 body-2 grey--text darken-4">{{ $t('menu.spaces.createSpace') }}</v-list-item-content>
      </v-list-item>
      <v-list-item
        v-for="(item,i) in itemsToShow"
        :key="i"
        class="px-2 spaceItem"
        @click="navigateTo(searching[i].link)">
        <v-list-item-avatar
          size="22"
          class="mr-3 tile my-0 spaceAvatar"
          tile>
          <v-img
            v-if="searching.length > 0"
            :src="searching[i].avatar"/>
        </v-list-item-avatar>
        <v-list-item-content v-if="searching.length > 0" class="py-0 body-2">{{ searching[i].spaceName }}</v-list-item-content>
      </v-list-item>
    </v-list>
    <v-row class="mx-0 my-4 justify-center">
      <v-btn
        v-if="showButton"
        small
        depressed
        @click="getSpacesPage(5)">{{ $t('menu.spaces.showMore') }}</v-btn>
    </v-row>
  </v-container>
</template>
<script>
export default {
  props: {
    drawer: {
      type: Object,
      default: null
    }
  },
  data () {
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
          spaceLink:'#' }
      ],
      itemsToShow: 15,
      right: null,
      showFilter: true,
      search: '',
      showButton: true
    };
  },
  computed: {
    searching () {
      if (!this.search) {
        return this.spacesList;
      } else {
        const search = this.search.toLowerCase();
        return this.spacesList.filter(space => {
          const text = space.spaceName.toLowerCase();
          return text.indexOf(search) > -1;
        });
      }
    },
  },
  methods: {
    closeFilter() {
      this.search = '';
      this.showFilter = true;
      this.itemsToShow = 15;
      this.showButton = true;
    },
    navigateTo(pagelink) {
      location.href=`${eXo.env.portal.context  }/${ eXo.env.portal.portalName }/${  pagelink}` ;
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
