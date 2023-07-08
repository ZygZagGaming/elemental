var webpack = require('webpack');

config.devtool = false;
config.performance = {
    hints: false,
    maxAssetSize: 512000,
    maxEntrypointSize: 512000
};
config.optimization = {
    minimize: false
};

config.plugins.push(new webpack.debug.ProfilingPlugin());