<template>
  <v-flex :class="shaped && 'ml-12'" class="mx-0 spacesNavigationContent" flat>
    <v-list :shaped="shaped" dense>
      <v-list-item-group v-model="selectedSpaceIndex">
        <v-list-item
          v-for="space in filteredSpaces"
          :key="space.id"
          :href="space.spaceUrl"
          link
          class="px-2 spaceItem">
          <v-list-item-avatar 
            size="26"
            class="mr-3 tile my-0 spaceAvatar"
            tile>
            <v-img :src="space.avatarUrl" />
          </v-list-item-avatar>
          <v-list-item-content>
            <v-list-item-title class="body-2" v-text="space.displayName" />
          </v-list-item-content>
        </v-list-item>
      </v-list-item-group>
    </v-list>
    <v-row v-if="canShowMore" class="mx-0 my-4 justify-center">
      <v-btn
        small
        depressed
        @click="loadNextPage()">
        {{ $t('menu.spaces.showMore') }}
      </v-btn>
    </v-row>
  </v-flex>
</template>
<script>
export default {
  props: {
    offset: {
      type: Number,
      default: 0,
    },
    limit: {
      type: Number,
      default: 10,
    },
    pageSize: {
      type: Number,
      default: 10,
    },
    keyword: {
      type: Object,
      default: null,
    },
    showMoreButton: {
      type: Boolean,
      default: false,
    },
    shaped: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 400,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    spaces: [],
    loadingSpaces: false,
    limitToFetch: 0,
    originalLimitToFetch: 0,
  }),
  computed:{
    canShowMore() {
      return this.showMoreButton && !this.loadingSpaces && this.spaces.length >= this.limitToFetch;
    },
    filteredSpaces() {
      if (!this.keyword) {
        return this.spaces;
      } else {
        return this.spaces.slice().filter(space => space.displayName && space.displayName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    selectedSpaceIndex() {
      return this.spaces.findIndex(space => space.spaceUrl === eXo.env.server.portalBaseURL || eXo.env.server.portalBaseURL.indexOf(`${space.spaceUrl}/`) === 0);
    },
  },
  watch: {
    keyword() {
      if (!this.keyword) {
        this.resetSearch();
        this.searchSpaces();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loadingSpaces) {
        this.loadingSpaces = true;
        this.waitForEndTyping();
      }
    },
    spaces() {
      this.spaces.forEach(space => {
        space.spaceUrl = `${eXo.env.portal.context}${space.spaceUrl}`;
      });
    },
    limitToFetch() {
      this.searchSpaces();
    },
  }, 
  created() {
    this.originalLimitToFetch = this.limitToFetch = this.limit;
  },
  methods: {
    searchSpaces() {
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/${eXo.env.portal.containerName}/social/spaces/lastVisitedSpace/list.json?offset=${this.offset}&limit=${this.limitToFetch}`)
        .then(resp => resp && resp.ok && resp.json())
        .then(data => {
          this.spaces = data && data.spaces || [];
          return this.$nextTick();
        })
        .then(() => {
          if (this.keyword && this.filteredSpaces.length < this.originalLimitToFetch && this.spaces.length >= this.limitToFetch) {
            this.limitToFetch += this.pageSize;
          }
        })
        .finally(() => this.loadingSpaces = false);
    },
    resetSearch() {
      if (this.limitToFetch !== this.originalLimitToFetch) {
        this.limitToFetch = this.originalLimitToFetch;
      }
    },
    loadNextPage() {
      this.originalLimitToFetch = this.limitToFetch += this.pageSize;
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchSpaces();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  }
};
</script>

