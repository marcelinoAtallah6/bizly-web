/**
 * Client-side profile-image normaliser. Browsers happily hand back the original 4–10 MB photo a
 * user picked from their phone — every backend that stores avatars then has to defend against it
 * (UM caps uploads at 2 MB and rejects with "Profile image exceeds maximum size"). We resize the
 * picture in-canvas to a small square JPEG before the request leaves the browser so:
 *
 *   1. Uploads always fit under the server-side size cap, regardless of the source file.
 *   2. The resulting bytes are tiny enough (typically 5–25 KB) that the JWT can embed the
 *      avatar directly via {@code profileImageBase64}, and the navbar shows it instantly with
 *      zero extra network calls.
 *
 * Output is base64 of a JPEG body (no {@code data:} prefix) — same shape the existing
 * `pendingProfileImage` field uses, so callers stay one-line.
 */

export interface CompressedImage {
  mime: string;
  /** Raw base64 (no `data:` prefix). */
  base64: string;
}

export interface CompressOptions {
  /** Maximum square edge in pixels. Larger images are scaled down preserving aspect ratio. */
  maxEdge?: number;
  /** JPEG quality, 0..1. 0.85 is the sweet spot for photos at 256 px. */
  quality?: number;
}

const DEFAULT_MAX_EDGE = 256;
const DEFAULT_QUALITY = 0.85;

/**
 * Reads a {@link File} (or `Blob`) chosen via {@code <input type="file">}, downscales it on a
 * canvas to at most {@code maxEdge}×{@code maxEdge}, and re-encodes as JPEG. Throws when the file
 * isn't a decodable image so callers can show a friendly "please pick an image" toast.
 */
export async function compressProfileImage(
  file: File | Blob,
  options: CompressOptions = {}
): Promise<CompressedImage> {
  const maxEdge = Math.max(32, options.maxEdge ?? DEFAULT_MAX_EDGE);
  const quality = Math.min(1, Math.max(0.4, options.quality ?? DEFAULT_QUALITY));

  const dataUrl = await readAsDataUrl(file);
  const img = await loadImage(dataUrl);

  const { width, height } = scaleToFit(img.naturalWidth, img.naturalHeight, maxEdge);
  const canvas = document.createElement('canvas');
  canvas.width = width;
  canvas.height = height;
  const ctx = canvas.getContext('2d');
  if (!ctx) {
    throw new Error('Canvas not available');
  }
  // White background avoids transparent PNGs becoming black blocks once we flatten to JPEG.
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, width, height);
  ctx.drawImage(img, 0, 0, width, height);

  const out = canvas.toDataURL('image/jpeg', quality);
  const comma = out.indexOf(',');
  return {
    mime: 'image/jpeg',
    base64: comma >= 0 ? out.slice(comma + 1) : out,
  };
}

function readAsDataUrl(file: File | Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(reader.error ?? new Error('Failed to read file'));
    reader.onload = () => resolve(String(reader.result ?? ''));
    reader.readAsDataURL(file);
  });
}

function loadImage(src: string): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    img.onerror = () => reject(new Error('Not a decodable image'));
    img.onload = () => resolve(img);
    img.src = src;
  });
}

function scaleToFit(srcW: number, srcH: number, maxEdge: number): { width: number; height: number } {
  if (!srcW || !srcH) {
    return { width: maxEdge, height: maxEdge };
  }
  if (srcW <= maxEdge && srcH <= maxEdge) {
    return { width: srcW, height: srcH };
  }
  const ratio = srcW / srcH;
  if (srcW >= srcH) {
    return { width: maxEdge, height: Math.round(maxEdge / ratio) };
  }
  return { width: Math.round(maxEdge * ratio), height: maxEdge };
}
