/**
 * Local / dev defaults. Override in `environment.prod.ts` for production builds.
 * Set `googleWebClientId` to the same **Web application** OAuth client ID as in Google Cloud Console
 * (must end with `.apps.googleusercontent.com`) so `accounts.google.com/gsi/status` returns 200.
 */
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
  /** Web OAuth 2.0 client ID from Google Cloud Console → Credentials → OAuth 2.0 Client IDs. */
  googleWebClientId: '645145027382-rd3m9lvdlfi5rl2vaobg44eofuc7m9jq.apps.googleusercontent.com',
  /**
   * After the access token expires, the user may choose refresh or sign-out only within this window.
   * Beyond it, the app signs out automatically (no prompt).
   */
  sessionExtendGraceMs: 15 * 60 * 1000,
};
