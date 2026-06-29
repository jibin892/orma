const sep = "/";
const delimiter = ":";

function assertPath(value) {
  if (typeof value !== "string") {
    throw new TypeError("Path must be a string.");
  }
}

function normalizeParts(parts, allowAboveRoot) {
  const result = [];
  for (const part of parts) {
    if (!part || part === ".") continue;
    if (part === "..") {
      if (result.length && result[result.length - 1] !== "..") {
        result.pop();
      } else if (allowAboveRoot) {
        result.push("..");
      }
    } else {
      result.push(part);
    }
  }
  return result;
}

function normalize(path) {
  assertPath(path);
  if (!path) return ".";
  const isAbs = path.charAt(0) === sep;
  const trailing = path.endsWith(sep);
  const parts = normalizeParts(path.split(sep), !isAbs);
  let output = parts.join(sep);
  if (!output && !isAbs) output = ".";
  if (output && trailing) output += sep;
  return (isAbs ? sep : "") + output;
}

function join(...paths) {
  return normalize(paths.filter(Boolean).join(sep));
}

function resolve(...paths) {
  let resolved = "";
  let absolute = false;
  for (let index = paths.length - 1; index >= -1 && !absolute; index -= 1) {
    const current = index >= 0 ? paths[index] : sep;
    assertPath(current);
    if (!current) continue;
    resolved = `${current}${sep}${resolved}`;
    absolute = current.charAt(0) === sep;
  }
  const output = normalizeParts(resolved.split(sep), !absolute).join(sep);
  return (absolute ? sep : "") + output || ".";
}

function isAbsolute(path) {
  assertPath(path);
  return path.charAt(0) === sep;
}

function dirname(path) {
  assertPath(path);
  if (!path) return ".";
  const normalized = normalize(path);
  if (normalized === sep) return sep;
  const index = normalized.lastIndexOf(sep);
  if (index <= 0) return isAbsolute(normalized) ? sep : ".";
  return normalized.slice(0, index);
}

function basename(path, ext) {
  assertPath(path);
  let base = normalize(path).split(sep).filter(Boolean).pop() || "";
  if (ext && base.endsWith(ext)) {
    base = base.slice(0, -ext.length);
  }
  return base;
}

function extname(path) {
  const base = basename(path);
  const index = base.lastIndexOf(".");
  return index > 0 ? base.slice(index) : "";
}

function relative(from, to) {
  const fromParts = resolve(from).slice(1).split(sep).filter(Boolean);
  const toParts = resolve(to).slice(1).split(sep).filter(Boolean);
  let same = 0;
  while (same < fromParts.length && same < toParts.length && fromParts[same] === toParts[same]) {
    same += 1;
  }
  return [
    ...fromParts.slice(same).map(() => ".."),
    ...toParts.slice(same),
  ].join(sep);
}

function parse(path) {
  assertPath(path);
  const root = isAbsolute(path) ? sep : "";
  const dir = dirname(path);
  const base = basename(path);
  const ext = extname(base);
  const name = ext ? base.slice(0, -ext.length) : base;
  return { root, dir, base, ext, name };
}

function format(pathObject) {
  const dir = pathObject.dir || pathObject.root || "";
  const base = pathObject.base || `${pathObject.name || ""}${pathObject.ext || ""}`;
  return dir ? join(dir, base) : base;
}

const pathApi = {
  basename,
  delimiter,
  dirname,
  extname,
  format,
  isAbsolute,
  join,
  normalize,
  parse,
  posix: null,
  relative,
  resolve,
  sep,
};

pathApi.posix = pathApi;

module.exports = pathApi;
