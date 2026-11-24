import * as zhCN from './zh-CN';
import * as enUS from './en-US';
export default { zh_CN: zhCN, en_US: enUS, zh_HK: {} };

// //其他模块语言包
// //@ts-nocheck
// import { merge } from 'lodash';
// import * as zhCN from './zh-CN';
// import * as enUS from './en-US';
// import mcLangPack from 'mp_mc/langPack';
// import ucLangPack from 'mp_uc/langPack';
// import bdcLangPack from 'mp_bdc/langPack';
// import logLangPack from 'mp_log/langPack';
// import widgetLangPack from 'mp_widget/langPack';
//
// export default {
//     zh_CN: merge(
//         {},
//         zhCN,
//         mcLangPack.zh_CN,
//         ucLangPack.zh_CN,
//         bdcLangPack.zh_CN,
//         logLangPack.zh_CN,
//         widgetLangPack.zh_CN,
//     ),
//     en_US: merge(
//         {},
//         enUS,
//         mcLangPack.en_US,
//         ucLangPack.en_US,
//         bdcLangPack.en_US,
//         logLangPack.en_US,
//         widgetLangPack.zh_CN,
//     ),
//     zh_HK: merge(
//         {},
//         mcLangPack.zh_HK,
//         ucLangPack.zh_HK,
//         bdcLangPack.zh_HK,
//         logLangPack.zh_HK,
//         widgetLangPack.zh_HK,
//     ),
// };
