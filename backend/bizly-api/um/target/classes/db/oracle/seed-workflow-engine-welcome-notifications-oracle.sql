-- =============================================================================
-- Seed: Generic workflow engine — welcome notifications for user & customer
--
-- Domain triggers live in UM_WORKFLOW_API_ENDPOINT (TRIGGER_SOURCE = DOMAIN).
-- Pipelines bind via canonical ACTION_CODE = EP:{endpointId}.
--
-- Prerequisites:
--   patch-um-workflow-engine-oracle.sql
--   patch-um-workflow-api-endpoints-oracle.sql
--   patch-um-workflow-endpoint-unified-catalog-oracle.sql
--   02-notification-seed-templates.sql (WELCOME_USER, WELCOME_CUSTOMER)
--
-- Runtime:
--   - UM UserServiceImpl → WorkflowEngineOrchestratorService (USER_CREATED_SUCCESS)
--   - KYC CustomerServiceImpl → UM_WORKFLOW_TRIGGER_QUEUE → UM processor
-- =============================================================================

SET DEFINE OFF;

-- ---------------------------------------------------------------------------
-- 1) Domain trigger catalog (UM_WORKFLOW_API_ENDPOINT)
-- ---------------------------------------------------------------------------
MERGE INTO UM.UM_WORKFLOW_API_ENDPOINT t
USING (
  SELECT 'USER_CREATED_SUCCESS' AS ENGINE_ACTION_CODE,
         'User created successfully' AS DISPLAY_NAME,
         'Fires after a UM user is persisted (add/register).' AS DESCRIPTION,
         'um' AS SERVICE_KEY,
         'IMMEDIATE' AS TRIGGER_KIND,
         'DOMAIN' AS TRIGGER_SOURCE,
         '/um/workflow-engine' AS SCREEN_ROUTE,
         'EVENT' AS ACTION_CODE,
         '/um/internal/trigger/USER_CREATED_SUCCESS' AS PATH_ANT_PATTERN,
         'INTERNAL' AS HTTP_METHOD
  FROM dual
) s ON (t.ENGINE_ACTION_CODE = s.ENGINE_ACTION_CODE)
WHEN NOT MATCHED THEN
  INSERT (ID, SERVICE_KEY, HTTP_METHOD, PATH_ANT_PATTERN, SCREEN_ROUTE, ACTION_CODE,
          PRIORITY, IS_ACTIVE, CREATED_AT, ENGINE_ACTION_CODE, DISPLAY_NAME, DESCRIPTION,
          TRIGGER_KIND, TRIGGER_SOURCE)
  VALUES (UM.S_UM_WORKFLOW_API_ENDPOINT.NEXTVAL, s.SERVICE_KEY, s.HTTP_METHOD, s.PATH_ANT_PATTERN,
          s.SCREEN_ROUTE, s.ACTION_CODE, 5, 1, SYSTIMESTAMP, s.ENGINE_ACTION_CODE, s.DISPLAY_NAME,
          s.DESCRIPTION, s.TRIGGER_KIND, s.TRIGGER_SOURCE);

MERGE INTO UM.UM_WORKFLOW_API_ENDPOINT t
USING (
  SELECT 'CUSTOMER_CREATED_SUCCESS' AS ENGINE_ACTION_CODE,
         'Customer created successfully' AS DISPLAY_NAME,
         'Fires after a KYC customer is persisted (add).' AS DESCRIPTION,
         'kyc' AS SERVICE_KEY,
         'IMMEDIATE' AS TRIGGER_KIND,
         'DOMAIN' AS TRIGGER_SOURCE,
         '/um/workflow-engine' AS SCREEN_ROUTE,
         'EVENT' AS ACTION_CODE,
         '/um/internal/trigger/CUSTOMER_CREATED_SUCCESS' AS PATH_ANT_PATTERN,
         'INTERNAL' AS HTTP_METHOD
  FROM dual
) s ON (t.ENGINE_ACTION_CODE = s.ENGINE_ACTION_CODE)
WHEN NOT MATCHED THEN
  INSERT (ID, SERVICE_KEY, HTTP_METHOD, PATH_ANT_PATTERN, SCREEN_ROUTE, ACTION_CODE,
          PRIORITY, IS_ACTIVE, CREATED_AT, ENGINE_ACTION_CODE, DISPLAY_NAME, DESCRIPTION,
          TRIGGER_KIND, TRIGGER_SOURCE)
  VALUES (UM.S_UM_WORKFLOW_API_ENDPOINT.NEXTVAL, s.SERVICE_KEY, s.HTTP_METHOD, s.PATH_ANT_PATTERN,
          s.SCREEN_ROUTE, s.ACTION_CODE, 5, 1, SYSTIMESTAMP, s.ENGINE_ACTION_CODE, s.DISPLAY_NAME,
          s.DESCRIPTION, s.TRIGGER_KIND, s.TRIGGER_SOURCE);

MERGE INTO UM.UM_WORKFLOW_API_ENDPOINT t
USING (
  SELECT 'USER_REGISTRATION_APPROVED' AS ENGINE_ACTION_CODE,
         'Business registration approved' AS DISPLAY_NAME,
         'Fires when onboarding workflow instance is fully approved.' AS DESCRIPTION,
         'um' AS SERVICE_KEY,
         'AFTER_APPROVAL' AS TRIGGER_KIND,
         'DOMAIN' AS TRIGGER_SOURCE,
         '/um/workflow-engine' AS SCREEN_ROUTE,
         'EVENT' AS ACTION_CODE,
         '/um/internal/trigger/USER_REGISTRATION_APPROVED' AS PATH_ANT_PATTERN,
         'INTERNAL' AS HTTP_METHOD
  FROM dual
) s ON (t.ENGINE_ACTION_CODE = s.ENGINE_ACTION_CODE)
WHEN NOT MATCHED THEN
  INSERT (ID, SERVICE_KEY, HTTP_METHOD, PATH_ANT_PATTERN, SCREEN_ROUTE, ACTION_CODE,
          PRIORITY, IS_ACTIVE, CREATED_AT, ENGINE_ACTION_CODE, DISPLAY_NAME, DESCRIPTION,
          TRIGGER_KIND, TRIGGER_SOURCE)
  VALUES (UM.S_UM_WORKFLOW_API_ENDPOINT.NEXTVAL, s.SERVICE_KEY, s.HTTP_METHOD, s.PATH_ANT_PATTERN,
          s.SCREEN_ROUTE, s.ACTION_CODE, 5, 1, SYSTIMESTAMP, s.ENGINE_ACTION_CODE, s.DISPLAY_NAME,
          s.DESCRIPTION, s.TRIGGER_KIND, s.TRIGGER_SOURCE);

-- ---------------------------------------------------------------------------
-- 2) Published global pipelines (version 1) — ACTION_CODE = EP:{id}
-- ---------------------------------------------------------------------------
DECLARE
  v_action_user VARCHAR2(80);
  v_action_cust VARCHAR2(80);
  v_def_user_id NUMBER;
  v_def_cust_id NUMBER;
BEGIN
  SELECT 'EP:' || ID INTO v_action_user
  FROM UM.UM_WORKFLOW_API_ENDPOINT
  WHERE ENGINE_ACTION_CODE = 'USER_CREATED_SUCCESS' AND ROWNUM = 1;

  SELECT 'EP:' || ID INTO v_action_cust
  FROM UM.UM_WORKFLOW_API_ENDPOINT
  WHERE ENGINE_ACTION_CODE = 'CUSTOMER_CREATED_SUCCESS' AND ROWNUM = 1;

  -- USER_CREATED_SUCCESS
  BEGIN
    SELECT ID INTO v_def_user_id
    FROM UM.UM_WORKFLOW_DEFINITION
    WHERE ACTION_CODE IN (v_action_user, 'USER_CREATED_SUCCESS')
      AND VERSION_NO = 1
      AND BUSINESS_ID IS NULL
      AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      INSERT INTO UM.UM_WORKFLOW_DEFINITION (
        ID, ACTION_CODE, VERSION_NO, STATUS, BUSINESS_ID, DISPLAY_NAME, NOTES,
        PUBLISHED_AT, PUBLISHED_BY, CREATED_BY
      ) VALUES (
        UM.S_UM_WORKFLOW_DEFINITION.NEXTVAL,
        v_action_user, 1, 'PUBLISHED', NULL,
        'Welcome email — new user', 'Migrated from NOTIF_WELCOME_FLAG polling',
        SYSTIMESTAMP, 'SYSTEM', 'seed-workflow-engine'
      )
      RETURNING ID INTO v_def_user_id;
  END;

  UPDATE UM.UM_WORKFLOW_DEFINITION
  SET ACTION_CODE = v_action_user
  WHERE ID = v_def_user_id AND ACTION_CODE <> v_action_user;

  MERGE INTO UM.UM_WORKFLOW_STEP t
  USING (
    SELECT v_def_user_id AS DEFINITION_ID,
           10 AS STEP_ORDER,
           'NOTIFICATION' AS STEP_TYPE,
           q'[{
             "timing": "ON_ACTION_SUCCESS",
             "channels": ["EMAIL", "INBOX"],
             "templateKey": "WELCOME_USER",
             "audience": {
               "mode": "CONTEXT",
               "contextRecipient": "createdUser"
             }
           }]' AS CONFIG_JSON
    FROM dual
  ) s ON (t.DEFINITION_ID = s.DEFINITION_ID AND t.STEP_ORDER = s.STEP_ORDER)
  WHEN NOT MATCHED THEN
    INSERT (ID, DEFINITION_ID, STEP_ORDER, STEP_TYPE, IS_ACTIVE, CONFIG_JSON)
    VALUES (UM.S_UM_WORKFLOW_STEP.NEXTVAL, s.DEFINITION_ID, s.STEP_ORDER,
            s.STEP_TYPE, 1, s.CONFIG_JSON);

  -- CUSTOMER_CREATED_SUCCESS
  BEGIN
    SELECT ID INTO v_def_cust_id
    FROM UM.UM_WORKFLOW_DEFINITION
    WHERE ACTION_CODE IN (v_action_cust, 'CUSTOMER_CREATED_SUCCESS')
      AND VERSION_NO = 1
      AND BUSINESS_ID IS NULL
      AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      INSERT INTO UM.UM_WORKFLOW_DEFINITION (
        ID, ACTION_CODE, VERSION_NO, STATUS, BUSINESS_ID, DISPLAY_NAME, NOTES,
        PUBLISHED_AT, PUBLISHED_BY, CREATED_BY
      ) VALUES (
        UM.S_UM_WORKFLOW_DEFINITION.NEXTVAL,
        v_action_cust, 1, 'PUBLISHED', NULL,
        'Welcome email — new customer', 'Migrated from KYC NOTIF_WELCOME_FLAG polling',
        SYSTIMESTAMP, 'SYSTEM', 'seed-workflow-engine'
      )
      RETURNING ID INTO v_def_cust_id;
  END;

  UPDATE UM.UM_WORKFLOW_DEFINITION
  SET ACTION_CODE = v_action_cust
  WHERE ID = v_def_cust_id AND ACTION_CODE <> v_action_cust;

  MERGE INTO UM.UM_WORKFLOW_STEP t
  USING (
    SELECT v_def_cust_id AS DEFINITION_ID,
           10 AS STEP_ORDER,
           'NOTIFICATION' AS STEP_TYPE,
           q'[{
             "timing": "ON_ACTION_SUCCESS",
             "channels": ["EMAIL", "INBOX"],
             "templateKey": "WELCOME_CUSTOMER",
             "audience": {
               "mode": "CONTEXT",
               "contextRecipient": "createdCustomer"
             }
           }]' AS CONFIG_JSON
    FROM dual
  ) s ON (t.DEFINITION_ID = s.DEFINITION_ID AND t.STEP_ORDER = s.STEP_ORDER)
  WHEN NOT MATCHED THEN
    INSERT (ID, DEFINITION_ID, STEP_ORDER, STEP_TYPE, IS_ACTIVE, CONFIG_JSON)
    VALUES (UM.S_UM_WORKFLOW_STEP.NEXTVAL, s.DEFINITION_ID, s.STEP_ORDER,
            s.STEP_TYPE, 1, s.CONFIG_JSON);

  COMMIT;
END;
/

-- ---------------------------------------------------------------------------
-- 3) UM menus — admin engine builder + business notification manager
-- ---------------------------------------------------------------------------
INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Workflow engine', '/um/workflow-engine', 'settings-automation', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%UM%' OR UPPER(a.NAME) LIKE '%USER MANAGEMENT%' OR UPPER(a.NAME) = 'UM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/um/workflow-engine');

INSERT INTO UM.UM_MENUS (ID, APPLICATION_ID, PARENT_ID, NAME, ROUTE, ICON, IS_ACTIVE, ALLOWED_ROLES)
SELECT UM.S_UM_MENUS.NEXTVAL, a.ID, NULL, 'Notification manager', '/bm/notification-manager', 'bell-ringing', 1, NULL
FROM UM.UM_APPLICATIONS a
WHERE (UPPER(a.NAME) LIKE '%BM%' OR UPPER(a.NAME) LIKE '%BUSINESS%' OR UPPER(a.NAME) = 'BM')
  AND a.IS_ACTIVE = 1
  AND ROWNUM = 1
  AND NOT EXISTS (SELECT 1 FROM UM.UM_MENUS m WHERE m.ROUTE = '/bm/notification-manager');

COMMIT;
