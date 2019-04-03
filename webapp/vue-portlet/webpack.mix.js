const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');
const dev = process.env.NODE_ENV === "development";

// the display name of the war
const app = 'social-vue-portlet';

// add the server path to your server location path
const exoServerPath = "/home/exo/Documents/Projects_eXo/servers/spacesPaginator/platform-5.2.x-SNAPSHOT-trial";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  devtool: dev ? 'inline-source-map' : false,
});
module.exports = config;
