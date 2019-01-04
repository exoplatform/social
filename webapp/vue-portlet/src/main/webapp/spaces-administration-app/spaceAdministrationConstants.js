export const spaceConstants = {
  ENV: eXo.env.portal || '',
  PORTAL: eXo.env.portal.context || '',
  PORTAL_NAME: eXo.env.portal.portalName || '',
  PROFILE_SPACE_LINK: '/g/:spaces:',
  SOCIAL_USER_API: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/`,
  SOCIAL_SPACE_API: '/portal/rest/v1/social/spaces',
  SOCIAL_PEOPLE_API: '/portal/rest/social/people',
  SETTING_API: '/rest/v1/settings',
  GROUP_API: '/rest/v1/groups',
  SPACES_PER_PAGE: 30,
  DEFAULT_SPACE_AVATAR: '/eXoSkin/skin/images/system/SpaceAvtDefault.png'
};