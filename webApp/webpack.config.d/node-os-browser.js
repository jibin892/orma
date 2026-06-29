const os = {
  EOL: "\n",
  arch: () => "wasm",
  cpus: () => [],
  endianness: () => "LE",
  freemem: () => 0,
  homedir: () => "/",
  hostname: () => "",
  loadavg: () => [0, 0, 0],
  networkInterfaces: () => ({}),
  platform: () => "browser",
  release: () => "",
  tmpdir: () => "/tmp",
  totalmem: () => 0,
  type: () => "Browser",
  uptime: () => 0,
  userInfo: () => ({
    gid: -1,
    homedir: "/",
    shell: null,
    uid: -1,
    username: "",
  }),
};

module.exports = os;
