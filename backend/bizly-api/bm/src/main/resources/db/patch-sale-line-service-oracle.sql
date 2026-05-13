-- Allow sale lines to reference BM services (payment for services) or PM products.
ALTER TABLE um.pm_customer_sale_line MODIFY product_id NULL;

ALTER TABLE um.pm_customer_sale_line ADD (
  line_type VARCHAR2(20) DEFAULT 'PRODUCT' NOT NULL,
  service_id NUMBER
);

ALTER TABLE um.pm_customer_sale_line ADD CONSTRAINT fk_pm_sale_line_service
  FOREIGN KEY (service_id) REFERENCES um.bm_service_item (id);

UPDATE um.pm_customer_sale_line SET line_type = 'PRODUCT' WHERE line_type IS NULL;
