module.exports = function (api) {
    api.cache(true);
    return {
      presets: ["@react-native/babel-preset"],
      plugins: [
        [
          "babel-plugin-module-resolver",
          {
            root: ["./src"],
            alias: {
              _api: "./src/api",
              _hooks: "./src/hooks",
              _utils: "./src/utils",
              _assets: "./src/assets",
              _screens: "./src/screens",
              _contexts: "./src/contexts",
              _formrules: "./src/form-rules",
              _components: "./src/components",
              _globalstyles: "./src/global-styles.js",
              _NavigationService: "./src/NavigationService.js",
            },
          },
        ],
      ],
    };
  };