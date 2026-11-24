//@ts-nocheck
//加时间戳防止cache
const baseUrl = process.env.VITE_BASE_URL;
const now = new Date().getTime();
const res = await fetch(`${baseUrl}/env.json?t=${now}`);
const envConfig = await res.json();
//把属性设置挂载到window.mpEnv 对象上
//如果配置文件中指定了环境 则首选它
const env = envConfig.env ?? process.env.VITE_ENV;
const targetProperties = envConfig[env];

for (const key in targetProperties) {
  const val = targetProperties[key];
  if (val && val.indexOf('.js') > 0) {
    targetProperties[key] = `${val}?t=${now}`;
  }
}

window.mpEnv = targetProperties;
window.env = env;

import('./main');
export default {};
