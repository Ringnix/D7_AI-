//@ts-nocheck
import orderManagement from './order-management';
import purchaseRequest from './purchase-request';
import myPackage from '../../package.json';
import { combineRoutes } from 'mp-cli/router/utils';
export default [...combineRoutes([...orderManagement, ...purchaseRequest], myPackage)];
