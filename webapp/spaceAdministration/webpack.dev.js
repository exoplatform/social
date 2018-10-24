const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'spacesAdministration';

// add the server path to your server location path
const exoServerPath = "/home/exo/dev/spaceadminServ/plfent-5.2.x-spaces-administration-20181018.205559-9/platform-5.2.x-spaces-administration-SNAPSHOT";

let config = merge(webpackCommonConfig, {
  output: {
    path: path.resolve(`${exoServerPath}/webapps/${app}/`),
    filename: 'js/[name].bundle.js'
  },
  devtool: 'inline-source-map'
});

module.exports = config;
