/**
 * tesseract.js typings reference Node's Buffer; Angular browser builds use `types: []`
 * and do not load @types/node. This minimal global keeps `ng build` / `ng serve` clean.
 */
interface Buffer extends Uint8Array {}

declare const Buffer: {
  from(
    arrayBuffer: ArrayBuffer | SharedArrayBuffer | ArrayLike<number>,
    byteOffset?: number,
    length?: number
  ): Buffer;
  isBuffer(obj: unknown): obj is Buffer;
};
