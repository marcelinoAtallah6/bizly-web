-- Optional profile photo on UM user (base64 stored as BLOB raw bytes)
ALTER TABLE um.um_user ADD (
  profile_image_mime VARCHAR2(64),
  profile_image_data BLOB
);
