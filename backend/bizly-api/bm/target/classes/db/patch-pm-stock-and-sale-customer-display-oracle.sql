-- Optional product stock (NULL = unlimited). Sale snapshot customer display name.
ALTER TABLE um.pm_product ADD (stock_quantity NUMBER(10));

ALTER TABLE um.pm_customer_sale ADD (customer_display_name VARCHAR2(300));
