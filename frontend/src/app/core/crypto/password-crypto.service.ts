import { Injectable } from '@angular/core';
import { BIZLY_PUBLIC_CERT_PEM } from './public-cert';

@Injectable({
  providedIn: 'root',
})
export class PasswordCryptoService {
  async encryptRsaOaepSha256Base64(plainText: string): Promise<string> {
    const certificateDer = this.pemToArrayBuffer(BIZLY_PUBLIC_CERT_PEM);
    const publicKeySpki = this.extractSpkiFromCertificate(certificateDer);

    const key = await globalThis.crypto.subtle.importKey(
      'spki',
      publicKeySpki,
      {
        name: 'RSA-OAEP',
        hash: 'SHA-256',
      },
      false,
      ['encrypt']
    );

    const encrypted = await globalThis.crypto.subtle.encrypt(
      { name: 'RSA-OAEP' },
      key,
      new TextEncoder().encode(plainText)
    );

    return this.arrayBufferToBase64(encrypted);
  }

  private pemToArrayBuffer(pem: string): ArrayBuffer {
    const base64 = pem
      .replace('-----BEGIN CERTIFICATE-----', '')
      .replace('-----END CERTIFICATE-----', '')
      .replace(/\s/g, '');

    const raw = atob(base64);
    const bytes = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; i += 1) {
      bytes[i] = raw.charCodeAt(i);
    }
    return bytes.buffer;
  }

  private extractSpkiFromCertificate(certDer: ArrayBuffer): ArrayBuffer {
    const certBytes = new Uint8Array(certDer);
    const readTlv = (start: number) => {
      const tag = certBytes[start];
      let lengthByte = certBytes[start + 1];
      let length = 0;
      let lengthBytes = 1;

      if ((lengthByte & 0x80) === 0) {
        length = lengthByte;
      } else {
        const count = lengthByte & 0x7f;
        lengthBytes = 1 + count;
        for (let i = 0; i < count; i += 1) {
          length = (length << 8) | certBytes[start + 2 + i];
        }
      }

      const headerLength = 1 + lengthBytes;
      const valueStart = start + headerLength;

      return {
        tag,
        start,
        valueStart,
        valueLength: length,
        totalLength: headerLength + length,
      };
    };

    const certSeq = readTlv(0);
    if (certSeq.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    const tbsSeq = readTlv(certSeq.valueStart);
    if (tbsSeq.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    let cursor = tbsSeq.valueStart;
    if (certBytes[cursor] === 0xa0) {
      const version = readTlv(cursor);
      cursor += version.totalLength;
    }

    const serial = readTlv(cursor);
    cursor += serial.totalLength;

    const signature = readTlv(cursor);
    cursor += signature.totalLength;

    const issuer = readTlv(cursor);
    cursor += issuer.totalLength;

    const validity = readTlv(cursor);
    cursor += validity.totalLength;

    const subject = readTlv(cursor);
    cursor += subject.totalLength;

    const spki = readTlv(cursor);
    if (spki.tag !== 0x30) {
      throw new Error('Invalid certificate format.');
    }

    return certBytes.slice(spki.start, spki.start + spki.totalLength).buffer;
  }

  private arrayBufferToBase64(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    bytes.forEach((byte) => {
      binary += String.fromCharCode(byte);
    });
    return btoa(binary);
  }
}
