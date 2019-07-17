<template>
  <div id="newsShareActivity">
    <a id="newsShareButton" :data-original-title="$t('activity.news.shareNews.share')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="showShareNews = true">
      <i class="uiIconShare"></i>
    </a>
    <exo-modal :show="showShareNews" :title="$t('activity.news.shareNews.popupTitle')" @close="closeShareNews">
      <div class="newsShareForm">
        <label class="newsTitle"> {{ newsTitle }}</label>
        <div class="shareSpaces">
          <label class="newsShareWith">{{ $t('activity.news.shareNews.sharewith') }} :</label>
          <div class="control-group">
            <div class="controls">
              <space-selector v-model="spaces" :source-providers="['exo:share-spaces']" />
            </div>
          </div>
        </div>
        <textarea v-model="description" :placeholder="$t('activity.news.shareNews.sharedActivityPlaceholder')" class="newsShareDescription"></textarea>
        <div class="shareButtons">
          <button :disabled="shareDisabled" class="btn btn-primary">{{ $t('activity.news.shareNews.share') }}</button>
          <button class="btn" @click="closeShareNews">{{ $t('activity.news.shareNews.cancel') }}</button>
        </div>
      </div>
    </exo-modal>
  </div>
</template>

<script>
import ExoModal from './ExoModal.vue';
import spaceSelector from './ExoShareNewsSpaceSelector.vue';

export default {
  components: {
    'exo-modal': ExoModal,
    'space-selector': spaceSelector,
  },
  data() {
    return {
      showShareNews: false,
      spaces: [],
      description: '',
      newsTitle: ''
    };
  },
  computed:{
    shareDisabled: function(){
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    }
  },
  mounted(){
    this.initNewsTitle();
    $('[rel="tooltip"]').tooltip();
  },
  methods: {
    closeShareNews: function(){
      this.showShareNews = false;
      this.spaces = [];
      this.description = '';

    },
    initNewsTitle: function(){
      this.newsTitle = $('.newsTitle a').text();
    }
  }
};
</script>
