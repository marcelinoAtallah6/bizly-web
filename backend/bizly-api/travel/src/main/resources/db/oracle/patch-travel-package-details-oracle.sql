-- Extended tour package content (itinerary, media, inclusions/exclusions)

ALTER TABLE um.travel_package ADD (
  inclusions CLOB,
  exclusions CLOB,
  itinerary_json CLOB,
  media_json CLOB
);

COMMIT;
