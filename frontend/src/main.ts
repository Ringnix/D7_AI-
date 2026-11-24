import 'ant-design-vue/dist/antd.variable.min.css';
import 'cyberway-middle-ui-components/lib/style.css';
import 'cyberway-middle-ui-components/lib/theme-sip.css';
import routes from '@/router';
import langPack from '@/locales/lang';
import { App } from 'vue';
import cp from 'cyberway-middle-ui-components';
import Antd from 'ant-design-vue';
import setupApp from 'mp-cli/setup';
import { uuid } from 'mp-cli/utils/text';
import Storage from 'mp-cli/utils/storage';
import { DEVICE_ID } from 'mp-cli/constants/cache-key';
import 'dayjs/locale/zh-cn';

//@ts-ignore
let deviceId = Storage.getCookie(DEVICE_ID);
if (!deviceId) {
  deviceId = uuid();
  //@ts-ignore
  Storage.setCookie(DEVICE_ID, deviceId);
}
const app: App = await setupApp({
  appKey: 'myAppKey',
  deviceId,
  router: {
    //@ts-ignore
    routes,
    usePermissionCheck: false,
  },
  template: {
    template: 'AntdTpm',
  },
  afterLoginRedirect: '/order-management/list',
  env: process.env.VITE_ENV,
  langPack,
  network: {
    baseUrl: process.env.VITE_BASE_API,
    requestTimeout: 0,
  },
  appInstanceId: process.env.VITE_APP_ID,
});
app.config.errorHandler = (err, instance, info) => {
  console.error('[order-management] 未捕获异常', info, err, instance);
};
app.config.warnHandler = () => null;
app.use(cp).use(Antd);
