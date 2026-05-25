-- Fix: CUSTOMER_CREATED_SUCCESS fires (UM_WORKFLOW_TRIGGER_QUEUE = DONE) but no notification
-- because UM_WORKFLOW_DEFINITION is missing for EP:{customer_endpoint_id}.
--
-- Your data shows endpoints ~20=customer, 21=user, 26=birthday user, 27=birthday customer.
-- Definitions present: EP:21, EP:26, EP:27, EP:30 — often missing EP:20 (customer welcome).
--
-- Run this after patch-um-workflow-engine-oracle.sql and 02-notification-seed-templates.sql

SET DEFINE OFF;

DECLARE
  v_ep_id NUMBER;
  v_action VARCHAR2(80);
  v_def_id NUMBER;
BEGIN
  SELECT ID INTO v_ep_id
  FROM UM.UM_WORKFLOW_API_ENDPOINT
  WHERE ENGINE_ACTION_CODE = 'CUSTOMER_CREATED_SUCCESS' AND ROWNUM = 1;

  v_action := 'EP:' || v_ep_id;

  BEGIN
    SELECT ID INTO v_def_id
    FROM UM.UM_WORKFLOW_DEFINITION
    WHERE UPPER(ACTION_CODE) = UPPER(v_action)
      AND VERSION_NO = 1
      AND BUSINESS_ID IS NULL
      AND ROWNUM = 1;
  EXCEPTION
    WHEN NO_DATA_FOUND THEN
      INSERT INTO UM.UM_WORKFLOW_DEFINITION (
        ID, ACTION_CODE, VERSION_NO, STATUS, BUSINESS_ID, DISPLAY_NAME, NOTES,
        PUBLISHED_AT, PUBLISHED_BY, CREATED_BY, CREATED_AT, UPDATED_AT
      ) VALUES (
        UM.S_UM_WORKFLOW_DEFINITION.NEXTVAL,
        v_action, 1, 'PUBLISHED', NULL,
        'Welcome email — new customer',
        'Created by fix-workflow-engine-missing-customer-welcome-oracle.sql',
        SYSTIMESTAMP, 'SYSTEM', 'fix-sql', SYSTIMESTAMP, SYSTIMESTAMP
      )
      RETURNING ID INTO v_def_id;
  END;

  UPDATE UM.UM_WORKFLOW_DEFINITION
  SET ACTION_CODE = v_action, STATUS = 'PUBLISHED', DISPLAY_NAME = 'Welcome email — new customer'
  WHERE ID = v_def_id;

  MERGE INTO UM.UM_WORKFLOW_STEP t
  USING (
    SELECT v_def_id AS DEFINITION_ID, 10 AS STEP_ORDER, 'NOTIFICATION' AS STEP_TYPE,
           q'[{
             "timing": "ON_ACTION_SUCCESS",
             "channels": ["EMAIL"],
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
    VALUES (UM.S_UM_WORKFLOW_STEP.NEXTVAL, s.DEFINITION_ID, s.STEP_ORDER, s.STEP_TYPE, 1, s.CONFIG_JSON)
  WHEN MATCHED THEN
    UPDATE SET STEP_TYPE = s.STEP_TYPE, IS_ACTIVE = 1, CONFIG_JSON = s.CONFIG_JSON;

  DBMS_OUTPUT.PUT_LINE('Customer welcome pipeline: ACTION_CODE=' || v_action || ' DEF_ID=' || v_def_id);
  COMMIT;
END;
/

-- Verify mapping (run manually):
-- SELECT ep.ID, ep.ENGINE_ACTION_CODE, d.ID AS DEF_ID, d.ACTION_CODE, d.STATUS
-- FROM UM.UM_WORKFLOW_API_ENDPOINT ep
-- LEFT JOIN UM.UM_WORKFLOW_DEFINITION d
--   ON UPPER(d.ACTION_CODE) = 'EP:' || ep.ID AND d.STATUS = 'PUBLISHED' AND d.BUSINESS_ID IS NULL
-- WHERE ep.ENGINE_ACTION_CODE IN (
--   'USER_CREATED_SUCCESS','CUSTOMER_CREATED_SUCCESS',
--   'BIRTHDAY_USER_SUCCESS','BIRTHDAY_CUSTOMER_SUCCESS','BUSINESS_REGISTRATION'
-- )
-- ORDER BY ep.ID;
