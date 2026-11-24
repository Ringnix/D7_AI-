import type { RouteRecordRaw } from 'vue-router';
import RouterTransition from 'mp-cli/router/router-transition';

export const routeName = 'purchase-request';
const basePath = '/purchase-request';

const routes: RouteRecordRaw[] = [
  {
    path: basePath,
    name: routeName,
    redirect: `${basePath}/list`,
    component: RouterTransition,
    meta: {
      title: '采购申请单',
      icon: '',
    },
    children: [
      {
        path: 'list',
        name: `${routeName}-list`,
        meta: {
          title: '采购申请单列表',
          icon: '',
        },
        component: () =>
          import(
            /* webpackChunkName: "purchase-request-list" */ '../views/purchase-request/list.vue'
          ),
      },
    ],
  },
];

export default routes;
