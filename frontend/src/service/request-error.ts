import type { AxiosError } from 'axios';

/** 后端统一响应中的错误字段。 */
interface ServerErrorBody {
  errorCode?: string;
  msg?: string;
}

/**
 * 框架默认拦截器已经弹出提示的 HTTP 状态码，页面无需重复提示。
 * 参数错误 400、服务端异常 500 由拦截器提示，401 会跳转登录。
 */
const PROMPTED_STATUS_LIST = [400, 401, 500, 503];

/**
 * 读取后端统一响应中的业务错误码。
 * @param error 请求异常
 * @returns 错误码；非接口异常时返回空字符串
 */
export function getServerErrorCode(error: unknown): string {
  const axiosError = error as AxiosError<ServerErrorBody>;
  return axiosError?.response?.data?.errorCode ?? '';
}

/**
 * 判断框架默认拦截器是否已展示错误提示，避免页面重复弹窗。
 * @param error 请求异常
 */
export function isErrorPromptedByFramework(error: unknown): boolean {
  const status = (error as AxiosError)?.response?.status;
  return typeof status === 'number' && PROMPTED_STATUS_LIST.includes(status);
}
