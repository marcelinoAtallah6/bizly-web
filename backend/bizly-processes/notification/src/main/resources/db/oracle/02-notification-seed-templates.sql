-- Default DB-backed templates (idempotent inserts by TEMPLATE_KEY).
SET DEFINE OFF;

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'welcome_email' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'welcome_email',
    'Welcome to Bizly, {{firstName}}!',
    q'[<html><body><p>Hi {{firstName}} {{lastName}},</p><p>Your account <strong>{{username}}</strong> is ready.</p><p>Sign in with this email: {{email}}</p><p>— Bizly</p></body></html>]',
    q'[Hi {{firstName}} {{lastName}}, Your account {{username}} is ready. Email: {{email}}. — Bizly]',
    'Y'
  );

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'birthday_email' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'birthday_email',
    'Happy Birthday, {{firstName}}! Your gift inside',
    q'[<html><body><p>Happy birthday, {{firstName}}!</p><p>Enjoy your exclusive promo: <strong>{{promoCode}}</strong></p><p>— Bizly</p></body></html>]',
    q'[Happy birthday {{firstName}}! Your promo code: {{promoCode}} — Bizly]',
    'Y'
  );

COMMIT;
