config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign({}, config.resolve.fallback || {}, {
  os: false,
  path: false,
});

if (config.mode === "production") {
  config.devtool = false;
}
