// 本地启动中台用户中心（cyberway-mp-uc-web）静态包，用于配置菜单/资源/角色。
// 用法：yarn uc:serve，然后浏览器访问 http://127.0.0.1:3001/
// 说明：uc 包内 dist 的 env.json 默认取 prd（旧地址），这里在服务端改写为当前开发环境地址，
// 避免修改 node_modules 内的包文件，重新安装依赖后仍可直接使用。
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const distDir = path.join(projectRoot, 'node_modules', 'cyberway-mp-uc-web', 'dist');
const port = Number(process.env.UC_PORT || 3001);

const ENV_CONTENT = {
  prd: {
    mp_cli: 'http://192.168.90.25:51671/assets/mp_cli.js',
    mp_bdc: 'http://192.168.90.25:47012/assets/mp_bdc.js',
  },
  local: {
    mp_cli: 'http://192.168.90.25:51671/assets/mp_cli.js',
    mp_bdc: 'http://192.168.90.25:47012/assets/mp_bdc.js',
  },
  dev: {
    mp_cli: 'http://192.168.90.25:51671/assets/mp_cli.js',
    mp_bdc: 'http://192.168.90.25:47012/assets/mp_bdc.js',
  },
};

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.mjs': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.woff': 'font/woff',
  '.woff2': 'font/woff2',
  '.ttf': 'font/ttf',
  '.map': 'application/json; charset=utf-8',
};

const server = http.createServer((req, res) => {
  const urlPath = decodeURIComponent((req.url || '/').split('?')[0]);

  if (urlPath === '/env.json') {
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify(ENV_CONTENT));
    return;
  }

  let filePath = path.join(distDir, urlPath === '/' ? 'index.html' : urlPath);
  if (!filePath.startsWith(distDir)) {
    res.writeHead(403);
    res.end('forbidden');
    return;
  }
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    // 前端使用 hash 路由，非资源路径回退到 index.html
    filePath = path.join(distDir, 'index.html');
  }
  const ext = path.extname(filePath).toLowerCase();
  res.writeHead(200, { 'Content-Type': MIME_TYPES[ext] || 'application/octet-stream' });
  fs.createReadStream(filePath).pipe(res);
});

server.listen(port, '127.0.0.1', () => {
  console.log(`用户中心已启动: http://127.0.0.1:${port}/`);
  console.log('登录后可在「资源/菜单/权限角色」中为应用配置菜单资源。');
});
