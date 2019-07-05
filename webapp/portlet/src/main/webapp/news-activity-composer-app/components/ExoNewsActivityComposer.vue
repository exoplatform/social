<template>
  <div id="newsActivityComposer" :class="newsFormExtendedClass" class="uiBox">
    <p v-show="extendedForm" class="createNews">{{ $t("activity.composer.news.createNews") }}</p>
    <form id="newsForm" :class="newsFormExtendedClass" class="newsForm" @submit.prevent="postNews">

      <div class="newsFormLabelsInputs">
        <div class="newsFormColumn newsFormLabels">
          <label class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("activity.composer.news.title") }}
            * : </label>
          <label v-show="extendedForm" class="newsFormLabel newsFormSummaryLabel" for="newsSummary"> {{
          $t("activity.composer.news.summary") }} : </label>
          <label class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("activity.composer.news.content") }}
            * : </label>
        </div>

        <div class="newsFormColumn newsFormInputs">
          <div class="newsInputsRows">
            <input id="newsTitle" v-model="newsActivity.title" :maxlength="max"
                   :placeholder="$t('activity.composer.news.placeholderTitleInput')" class="newsFormInput" type="text">
            <textarea v-show="extendedForm" id="newsSummary" v-model="newsActivity.summary" :maxlength="max"
                      :placeholder="$t('activity.composer.news.placeholderSummaryInput')"
                      class="newsFormInput" type="text"/>
            <div v-show="extendedForm" class="control-group attachments">
              <div class="controls">
                <filedrop v-model="newsActivity.illustration"/>
              </div>
            </div>
          </div>
          <div v-show="extendedForm" id="UINewsSummaryDescription" class="UINewsSummaryDescription">
            <i class="uiIconInformation"></i>
            {{ $t("activity.composer.news.summaryDescription") }}
          </div>
          <textarea id="newsContent" v-model="newsActivity.content"
                    :placeholder="$t('activity.composer.news.placeholderContentInput')" type="text"
                    class="newsFormInput" name="newsContent"></textarea>

          <div class="newsFormButtons">
            <span class="uiCheckbox">
              <input id="pinArticle" v-model="pinArticle" type="checkbox" class="checkbox ">
              <span>{{ $t("activity.composer.news.pinArticle") }}</span>
            </span>
            <button id="newsPost" :disabled="postDisabled" class="btn btn-primary"> {{ $t("activity.composer.news.post")
            }}
            </button>
            <a id="newsPlus" :data-original-title="extendFormButtonTooltip" class="btn btn-primary"
               rel="tooltip" data-placement="bottom"
               @click="extendedForm = !extendedForm;">
              <i :class="extendFormButtonClass"></i>
            </a>
          </div>
        </div>
      </div>

    </form>
  </div>
</template>

<script>
import * as  newsActivityComposerServices from '../newsActivityComposerServices';
import FileDrop from './ExoNewsFileDrop.vue';

export default {
  components: {
    'filedrop': FileDrop
  },
  data() {
    return {
      newsActivity: {
        title: '',
        content: '',
        summary: '',
        illustration: []
      },
      pinArticle: false,
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      TITLE_MAX_LENGTH: 150,
      extendedForm: false,
      extendFormButtonClass: 'uiIconSimplePlus',
      extendFormButtonTooltip: this.$t('activity.composer.news.moreOptions'),
      newsFormExtendedClass: '',
      newsFormContentHeight: '',
    };
  },
  computed: {
    postDisabled: function () {
      return !this.newsActivity.title || !this.newsActivity.title.trim() || !this.newsActivity.content || !this.newsActivity.content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim();
    }
  },
  watch: {
    extendedForm: function() {
      this.extendFormButtonClass = this.extendedForm ? 'uiIconMinimize' : 'uiIconSimplePlus';
      this.extendFormButtonTooltip = this.extendedForm ? this.$t('activity.composer.news.lessOptions') : this.$t('activity.composer.news.moreOptions');
      document.getElementById('UISpaceMenu').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('ActivityComposerExt').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('UISpaceActivitiesDisplay').style.display = this.extendedForm ? 'none' : '';
      const spaceHomePortletColumn = document.getElementsByClassName('SpaceHomePortletsTDContainer');
      if(spaceHomePortletColumn.length > 0) {
        spaceHomePortletColumn[0].style.display = this.extendedForm ? 'none' : '';
      }
      const portletsContainers = document.getElementById('UIPage').getElementsByClassName('PORTLET-FRAGMENT');
      Array.from(portletsContainers).forEach(portletContainer => {
        if(portletContainer.getElementsByClassName('uiSpaceActivityStreamPortlet').length === 0) {
          portletContainer.style.display = this.extendedForm ? 'none' : '';
        }
      });
      this.newsFormExtendedClass = this.extendedForm ? 'extended' : '';
      this.newsFormContentHeight = this.extendedForm ? '250' : '110';
      CKEDITOR.instances['newsContent'].resize('100%', this.newsFormContentHeight);
    }
  },
  created() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if (textarea && shareButton) {
      textarea.style.display = 'none';
      shareButton.style.display = 'none';
    }
  },
  mounted() {
    $('[rel="tooltip"]').tooltip();
    this.initCKEditor();
  },
  beforeDestroy() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if (textarea && shareButton) {
      textarea.style.display = 'block';
      shareButton.style.display = 'block';
    }
  },
  methods: {
    initCKEditor: function () {
      let extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }

      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';

      const self = this;

      const composerInput = $('textarea#newsContent');
      composerInput.ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        height: this.newsFormContentHeight ,
        on: {
          change: function (evt) {
            self.newsActivity.content = evt.editor.getData();
          }
        }
      });
    },
    postNews: function () {
      const activity = {
        title: this.newsActivity.title,
        body: this.newsActivity.content,
        type: 'news',
        templateParams: {
          summary: this.newsActivity.summary
        }
      };

      if(this.newsActivity.illustration.length > 0) {
        activity.files = [{ uploadId: this.newsActivity.illustration[0].uploadId }];
      }

      newsActivityComposerServices.saveNewsActivity(activity).then(() => {
        // reset form
        this.newsActivity.title = '';
        this.newsActivity.content = '';
        CKEDITOR.instances['newsContent'].setData('');
        this.pinArticle = false;
        this.extendedForm = false;

        // refresh activity stream
        const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
        if (refreshButton) {
          refreshButton.click();
        }
      });
    }
  }
};
</script>
