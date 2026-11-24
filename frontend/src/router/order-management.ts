import type { RouteRecordRaw } from 'vue-router';
import RouterTransition from 'mp-cli/router/router-transition';

export const routeName = 'order-management';
const basePath = '/order-management';

const routes: RouteRecordRaw[] = [
  {
    path: basePath,
    name: routeName,
    redirect: `${basePath}/list`,
    component: RouterTransition,
    meta: {
      title: '订单管理',
      icon: '',
    },
    children: [
      {
        path: 'list',
        name: `${routeName}-list`,
        meta: {
          title: '订单列表',
          icon: '',
        },
        component: () =>
          import(
            /* webpackChunkName: "order-management-list" */ '../views/order-management/list.vue'
          ),
      },
    ],
  },
];

export default routes;
