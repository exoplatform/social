<template>
  <v-app id="NotificationPopoverPortlet" class="VuetifyApp">
    <v-flex>
      <v-layout>
        <v-btn
          icon
          class="text-xs-center"
          @click="openDrawer()" >
          <v-badge
            :value="badge > 0"
            :content="badge"
            flat
            color="red"
            overlap>
            <v-icon
              class="grey-color">
              notifications
            </v-icon>
          </v-badge>
        </v-btn>
        <v-navigation-drawer
          v-model="drawerNotification"
          right
          absolute
          temporary
          width="420"
          height="100vh"
          max-height="100vh"
          max-width="100vw"
          class="notifDrawer">
          <v-row class="mx-0 px-3 notifDrawerHeader">
            <v-list-item>
              <v-list-item-content>
                <span class="notifDrawerTitle">{{ $t('UIIntranetNotificationsPortlet.title.notifications') }}</span>
              </v-list-item-content>
              <v-list-item-action class="notifDrawerIcons">
                <i class="uiSettingsIcon notifDrawerSettings mr-3" @click="navigateTo('settings')"></i>
                <i class="uiCloseIcon notifDrawerClose" @click="closeDrawer()"></i>
              </v-list-item-action>
            </v-list-item>
          </v-row>

          <v-divider
            :inset="inset" 
            class="my-0"/>
          <div v-if="notificationsSize > 0" class="notifDrawerItems">
            <div
              v-for="(notif, i) in notifications"
              :key="i"
              :id="'notifItem-'+i"
              class="notifDrawerItem"
              @mouseenter="applyActions(`notifItem-`+i)"
              v-html="notif.notification">
            </div>
          </div>
          <div v-else class="noNoticationWrapper">
            <div class="noNotificationsContent">
              <i class="uiNoNotifIcon"></i>
              <p>{{ $t('UIIntranetNotificationsPortlet.label.NoNotifications') }}</p>
            </div>
          </div>
          <v-row v-if="notificationsSize > 0" class="notifFooterActions mx-0">
            <v-card 
              flat
              tile 
              class="d-flex flex justify-end mx-2">
              <v-btn 
                text
                small
                class="text-uppercase caption markAllAsRead"
                color="primary"
                @click="markAllAsRead()">{{ $t('UIIntranetNotificationsPortlet.label.MarkAllAsRead') }}</v-btn>
              <v-btn 
                class="text-uppercase caption primary--text seeAllNotif"
                outlined 
                small
                @click="navigateTo('allNotifications/')">{{ $t('UIIntranetNotificationsPortlet.label.seeAll') }}</v-btn>
            </v-card>
          </v-row>
        </v-navigation-drawer>
      </v-layout>
    </v-flex>
  </v-app>
</template>
<script>
import * as notificationlAPI from '../js/notificationsAPI';

const SLIDE_UP = 300;
const SLIDE_UP_MORE = 600;

export default {
  data () {
    return {
      drawerNotification: null,
      notifications: [],
      badge: 0,
      notificationsSize: 0
    };
  },
  watch: {
    badge() {
      return this.badge;
    },
    drawerNotification() {
      if (this.drawerNotification) {
        $('body').addClass('hide-scroll');
      } else {
        $('body').removeClass('hide-scroll');
      }
      this.$nextTick().then(() => {
        $('#NotificationPopoverPortlet .v-overlay').click(() => {
          this.drawerNotification = false;
        });
      });
    },
  },
  created() {
    this.getNotifications();
    notificationlAPI.getUserToken().then(
      (data) => {
        notificationlAPI.initCometd(data);
      }
    );
    document.addEventListener('cometdNotifEvent', this.notificationUpdated);
    $(document).on('keydown', (event) => {
      if (event.key === 'Escape') {
        this.drawerNotification = false;
      }
    });
  },
  methods: {
    getNotifications() {
      notificationlAPI.getNotifications().then((data) => {
        this.notifications = data.notifications;
        this.badge = data.badge;
        this.notificationsSize = this.notifications.length;
      });
    },

    markAllAsRead() {
      notificationlAPI.updateNotification(null, 'markAllAsRead');
      $('.notifDrawerItems').find('li').each(function() {
        if($(this).hasClass('unread')) {
          $(this).removeClass('unread').addClass('read');
        }
      });
    },

    openDrawer() {
      this.drawerNotification = !this.drawerNotification;
      $('body').addClass('open-notif-drawer');
      if(this.badge > 0) {
        notificationlAPI.updateNotification(null, 'resetNew');
        this.badge = 0;
      }
    },

    closeDrawer() {
      this.drawerNotification = !this.drawerNotification;
      $('body').removeClass('open-notif-drawer');
    },

    navigateTo(pagelink) {
      location.href=`${ eXo.env.portal.context }/${ eXo.env.portal.portalName }/${ pagelink }` ;
    },
    applyActions(item) {
      $(`#${item}`).find('li').each(function () {
        const dataLink = $(this).find('.contentSmall:first').data('link');
        const linkId = dataLink.split('/portal/intranet/');
        const dataId = $(this).data('id').toString();

        // ----------------- Mark as read

        $(this).on('click', function(evt) {
          evt.stopPropagation();
          if($(this).hasClass('unread')) {
            $(this).removeClass('unread').addClass('read');
          }
          notificationlAPI.updateNotification(dataId,'markAsRead');
          if(linkId.length >1 ) {
            location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/${linkId[1]}`;
          } else {
            const id = linkId[0].split('/view_full_activity/')[1];
            location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${id}`;
          }
        });

        // ------------- hide notif
        $(this).find('.remove-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            notificationlAPI.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP);
          });

        // ------------- Accept request

        $(this).find('.action-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            let restURl = $(this).data('rest');
            if(restURl.indexOf('?') >= 0 ) {
              restURl += '&';
            } 
            else {
              restURl += '?';
            }
            restURl += `portal:csrf=${eXo.env.portal.csrfToken}`;
            if(restURl && restURl.length > 0) {
              $.ajax(restURl).done(function () {
                $(document).trigger('exo-invitation-updated');
              });
            }
            notificationlAPI.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP_MORE);
          });

        // ------------- Refuse request

        $(this).find('.cancel-item').off('click')
          .on('click', function(evt) {
            evt.stopPropagation();
            let restCancelURl = $(this).data('rest');
            if(restCancelURl.indexOf('?') >= 0 ) {
              restCancelURl += '&';
            } 
            else {
              restCancelURl += '?';
            }
            restCancelURl += `portal:csrf=${eXo.env.portal.csrfToken}`;
            if(restCancelURl && restCancelURl.length > 0) {
              $.ajax(restCancelURl).done(function () {
                $(document).trigger('exo-invitation-updated');
              });
            }
            notificationlAPI.updateNotification(dataId,'hide');
            $(this).parents('li:first').slideUp(SLIDE_UP_MORE);
          });
      });
    },

    notificationUpdated(event) {
      if(event && event.detail) {
        this.badge = event.detail.data.numberOnBadge;
        this.getNotifications();
      }
    },
  }
};
</script>