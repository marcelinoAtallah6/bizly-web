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

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'WELCOME_USER' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'WELCOME_USER',
    'Welcome to Bizly, {{firstName}}!',
    q'[<html><body><p>Hi {{firstName}} {{lastName}},</p><p>Your account <strong>{{username}}</strong> is ready.</p><p>Email: {{email}}</p><p>— Bizly</p></body></html>]',
    q'[Hi {{firstName}} {{lastName}}, Your account {{username}} is ready. Email: {{email}}. — Bizly]',
    'Y'
  );

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'WELCOME_CUSTOMER' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'WELCOME_CUSTOMER',
    'Welcome to Bizly, {{name}}!',
    q'[<html><body><p>Hi {{name}},</p><p>Thank you for joining Bizly.</p><p>We will reach you at {{email}}.</p><p>— Bizly</p></body></html>]',
    q'[Hi {{name}}, Thank you for joining Bizly. Email: {{email}}. — Bizly]',
    'Y'
  );

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'BIRTHDAY_USER' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'BIRTHDAY_USER',
    'Happy Birthday, {{firstName}}!',
    q'[<html><body><p>Happy birthday, {{firstName}}!</p><p>Your exclusive promo: <strong>{{promoCode}}</strong></p><p>— Bizly</p></body></html>]',
    q'[Happy birthday {{firstName}}! Promo: {{promoCode}} — Bizly]',
    'Y'
  );

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'BIRTHDAY_CUSTOMER' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'BIRTHDAY_CUSTOMER',
    'Happy Birthday, {{name}}!',
    q'[<html><body><p>Happy birthday, {{name}}!</p><p>Enjoy: <strong>{{promotion}}</strong></p><p>— Bizly</p></body></html>]',
    q'[Happy birthday {{name}}! {{promotion}} — Bizly]',
    'Y'
  );

MERGE INTO UM.NOTIF_EMAIL_TEMPLATE t
USING (
  SELECT 'BROADCAST_DEFAULT' AS TEMPLATE_KEY FROM dual
) s ON (t.TEMPLATE_KEY = s.TEMPLATE_KEY)
WHEN NOT MATCHED THEN
  INSERT (ID, TEMPLATE_KEY, SUBJECT_TMPL, HTML_BODY, TEXT_BODY, ACTIVE_IND)
  VALUES (
    UM.NOTIF_EMAIL_TEMPLATE_SEQ.NEXTVAL,
    'BROADCAST_DEFAULT',
    'Message from Bizly',
    q'[<html><body><p>Hello {{name}},</p><p>{{promotion}}</p><p>— Bizly</p></body></html>]',
    q'[Hello {{name}}, {{promotion}} — Bizly]',
    'Y'
  );

COMMIT;
