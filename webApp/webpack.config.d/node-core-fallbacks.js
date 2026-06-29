const path = require("path");

config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign({}, config.resolve.fallback || {}, {
  os: path.resolve(__dirname, "node-os-browser.js"),
  path: path.resolve(__dirname, "node-path-browser.js"),
});

if (config.mode === "production") {
  config.devtool = false;
}
