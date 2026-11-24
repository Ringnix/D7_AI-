/**
 * 解析 JSON 文本，并把超出 JavaScript 安全整数范围的整数字面量保留为字符串。
 *
 * 订单调用服务 B 会把订单主键（Long）序列化为 JSON 数字，例如：
 *   { "id": 736323163322843136 }
 * 而 JavaScript 的 Number 最大安全整数是 9007199254740991，直接 JSON.parse 会得到
 * 736323163322843100 这类丢失精度的值，导致后续编辑、删除订单时使用了错误的 ID。
 * 该函数在 JSON.parse 之前扫描文本，把不安全的大整数加上引号，从而保留完整主键。
 *
 * @param text 原始 JSON 文本
 * @returns 解析后的对象
 */
export function parseJsonKeepLongAsString(text: string): unknown {
  let result = '';
  let index = 0;
  let inString = false;
  let escaped = false;

  while (index < text.length) {
    const char = text[index];

    if (inString) {
      result += char;
      if (escaped) {
        escaped = false;
      } else if (char === '\\') {
        escaped = true;
      } else if (char === '"') {
        inString = false;
      }
      index += 1;
      continue;
    }

    if (char === '"') {
      inString = true;
      result += char;
      index += 1;
      continue;
    }

    if (char === '-' || (char >= '0' && char <= '9')) {
      let end = index + 1;
      while (end < text.length && text[end] >= '0' && text[end] <= '9') {
        end += 1;
      }
      const numberText = text.slice(index, end);
      const nextChar = text[end] ?? '';
      const isInteger =
        /^-?\d+$/.test(numberText) && nextChar !== '.' && nextChar !== 'e' && nextChar !== 'E';
      if (isInteger && numberText.length >= 16 && !Number.isSafeInteger(Number(numberText))) {
        result += `"${numberText}"`;
      } else {
        result += numberText;
      }
      index = end;
      continue;
    }

    result += char;
    index += 1;
  }

  return JSON.parse(result);
}
