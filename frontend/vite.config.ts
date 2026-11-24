import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import federation from '@originjs/vite-plugin-federation';
//@ts-ignore
import path from 'node:path';
import compressPlugin from 'vite-plugin-compression';
const timeStamp = Date.now();
export default ({ mode }) => {
  const envConfig = loadEnv(mode, process.cwd());
  return defineConfig({
    define: {
      'process.env': {
        VITE_BASE_URL: envConfig.VITE_BASE_URL,
        VITE_BASE_API: envConfig.VITE_BASE_API,
        VITE_APP_ID: envConfig.VITE_APP_ID,
        VITE_ENV: envConfig.VITE_ENV,
      },
    },
    plugins: [
      vue({}),
      vueJsx({}),
      federation({
        name: 'mp_web',
        filename: 'mp_web.js',
        remotes: {
          'mp-cli': {
            external: `Promise.resolve(window.mpEnv.mp_cli)`,
            externalType: 'promise',
          },
        },
        exposes: {},
        shared: [
          'vue',
          'vue-router',
          'ant-design-vue',
          'axios',
          '@ant-design/icons-vue',
          'vue-i18n',
          'dayjs',
          'pinia',
          '@vueuse/core',
        ],
      }),
      compressPlugin({
        algorithm: 'gzip',
        ext: '.gz',
        threshold: 100,
        deleteOriginFile: false,
      }),
    ],
    optimizeDeps: {
      include: [
        'dayjs',
        'dayjs/plugin/localeData',
        'dayjs/plugin/weekday',
        'dayjs/locale/zh-cn',
        'dayjs/locale/zh-hk',
        '@babel/runtime/regenerator',
        'dayjs/plugin/advancedFormat',
        'dayjs/plugin/customParseFormat',
        'dayjs/plugin/weekOfYear',
        'dayjs/plugin/weekYear',
        'lodash',
        'lodash/debounce',
        'lodash/cloneDeep',
        'lodash/omit',
        'sm-crypto/src/index',
        'sm-crypto',
      ],
    },
    envDir: './',
    base: envConfig.VITE_BASE_URL,
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    css: {
      preprocessorOptions: {
        less: {
          javascriptEnabled: true,
        },
      },
    },
    build: {
      target: 'esnext',
      minify: mode === 'production' ? 'terser' : false,
      cssCodeSplit: true,
      rollupOptions: {
        output: {
          format: 'esm',
          entryFileNames: `assets/[name]-[hash]-${timeStamp}.js`,
          minifyInternalExports: false,
        },
      },
      terserOptions: {
        compress: {
          drop_console: mode === 'production',
          drop_debugger: mode === 'production',
        },
      },
      sourcemap: mode != 'production',
    },
    server: {
      //@ts-ignore
      port: envConfig.VITE_PORT,
      // 端口被占用时直接报错，避免 Vite 自动换到 3001 等端口导致访问地址混乱
      strictPort: true,
      // 本地开发代理：订单查询经服务 B 调用服务 A，写接口直连服务 A，采购申请单直连服务 A，其余接口转发到中台开发环境
      proxy: {
        '/api/orders': {
          target: envConfig.VITE_PROXY_ORDER_API || 'http://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/api/, ''),
        },
        '/api/purchase-requests': {
          target: envConfig.VITE_PROXY_ORDER_API || 'http://127.0.0.1:8081',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/api/, ''),
        },
        '/api/consumer': {
          target: envConfig.VITE_PROXY_CONSUMER_API || 'http://127.0.0.1:8082',
          changeOrigin: true,
          rewrite: (path: string) => path.replace(/^\/api/, ''),
        },
        '/api': {
          target: envConfig.VITE_PROXY_PLATFORM_API || 'http://192.168.90.25:46807',
          changeOrigin: true,
        },
      },
    },
    preview: {
      port: 8000,
    },
  });
};
