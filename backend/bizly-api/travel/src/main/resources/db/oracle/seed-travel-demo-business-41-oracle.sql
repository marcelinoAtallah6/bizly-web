-- =============================================================================
-- Horizon Voyages demo dataset for travel agency (business_id = 41)
-- Scenario: boutique agency selling Europe + Asia packages, visa services,
--           supplier contracts, trip requests, and end-to-end bookings.
--
-- Prerequisites (run first if not applied):
--   travel-schema-oracle.sql
--   patch-travel-package-details-oracle.sql
--   patch-travel-business-extensions-oracle.sql
--
-- Usage (as UM user):
--   @seed-travel-demo-business-41-oracle.sql
--
-- Re-runnable: deletes existing travel rows for business_id = 41, then reloads.
-- Fixed surrogate IDs in range 4101–4299 (safe to reference in docs/tests).
-- =============================================================================

SET DEFINE OFF;

DECLARE
  v_cnt NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_cnt FROM um.um_business WHERE id = 41;
  IF v_cnt = 0 THEN
    RAISE_APPLICATION_ERROR(-20041, 'Business id 41 does not exist in um.um_business. Create the tenant first.');
  END IF;
END;
/

-- Remove prior demo data (children first)
DELETE FROM um.travel_payment            WHERE business_id = 41;
DELETE FROM um.travel_invoice            WHERE business_id = 41;
DELETE FROM um.travel_consultant_commission WHERE business_id = 41;
DELETE FROM um.travel_document           WHERE business_id = 41;
DELETE FROM um.travel_follow_up          WHERE business_id = 41;
DELETE FROM um.travel_visa_application   WHERE business_id = 41;
DELETE FROM um.travel_booking_passenger  WHERE business_id = 41;
DELETE FROM um.travel_booking_component  WHERE business_id = 41;
DELETE FROM um.travel_trip_request       WHERE business_id = 41;
DELETE FROM um.travel_booking            WHERE business_id = 41;
DELETE FROM um.travel_package            WHERE business_id = 41;
DELETE FROM um.travel_supplier           WHERE business_id = 41;
DELETE FROM um.travel_commission_rule    WHERE business_id = 41;
DELETE FROM um.travel_client             WHERE business_id = 41;
DELETE FROM um.travel_destination        WHERE business_id = 41;

COMMIT;

-- -----------------------------------------------------------------------------
-- DESTINATIONS (4101–4106)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4101, 41, 'Paris', 'France', 'Western Europe',
  'Schengen visa required for most non-EU nationals. 90-day tourist stay typical.',
  'Routine vaccines recommended. No mandatory yellow fever unless arriving from endemic area.',
  'Jun–Aug peak; book hotels 3+ months ahead. Fashion Week (Sep) drives rates up.',
  'Nov–Feb lower rates except Christmas week. Many museums still open.',
  'Pickpocket risk in tourist zones; metro strikes occasional.',
  'LOW',
  'French MFA: standard Schengen travel advisory. Register with embassy for long stays.',
  1
);

INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4102, 41, 'Rome', 'Italy', 'Southern Europe',
  'Schengen visa. Hotel booking confirmation often requested at border.',
  'COVID-era health forms discontinued; check current EU entry rules.',
  'Apr–Jun and Sep–Oct ideal weather. Easter week extremely busy.',
  'Aug hot; some shops close. Jan–Feb quiet, good for Vatican museums.',
  'Crowds at Colosseum; pre-book skip-the-line tickets.',
  'LOW',
  'Italy entry: passport valid 3+ months beyond stay.',
  1
);

INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4103, 41, 'Bali', 'Indonesia', 'Southeast Asia',
  'Visa on arrival / e-VOA for many nationalities (30 days, extendable).',
  'Hepatitis A and typhoid recommended. Dengue risk in rainy season.',
  'Jul–Aug and Dec peak for Australians/Europeans.',
  'Apr–May and Oct–Nov shoulder season; fewer crowds.',
  'Monsoon Nov–Mar on west coast; check surf/road conditions.',
  'MEDIUM',
  'Respect local customs; use licensed drivers on mountain roads.',
  1
);

INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4104, 41, 'Dubai', 'UAE', 'Middle East',
  'Visa on arrival for many passports; GCC rules apply for regional travelers.',
  'Heat stress in summer; stay hydrated.',
  'Nov–Mar cool season; Expo/events can spike hotel prices.',
  'Jun–Sep extreme heat; indoor attractions preferred.',
  'Strict laws on alcohol and public behavior.',
  'LOW',
  'UAE: follow local regulations; travel insurance recommended.',
  1
);

INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4105, 41, 'Tokyo', 'Japan', 'East Asia',
  'Visa-free short stay for many Western passports; work/study requires visa.',
  'No special vaccines required for standard tourism.',
  'Cherry blossom (Mar–Apr) and autumn foliage (Nov) sell out fast.',
  'Jan quiet after New Year; great for skiing in Hokkaido add-ons.',
  'Earthquake-aware; download disaster alert apps.',
  'LOW',
  'Japan National Tourism Organization: excellent rail pass for multi-city trips.',
  1
);

INSERT INTO um.travel_destination (
  id, business_id, name, country, region,
  visa_requirements, health_requirements,
  high_season_notes, low_season_notes, travel_warnings,
  risk_level, travel_advisory, active
) VALUES (
  4106, 41, 'Cairo', 'Egypt', 'North Africa',
  'E-visa or visa on arrival for many tourists. Passport 6+ months validity.',
  'Hepatitis A recommended. Drink bottled water.',
  'Oct–Apr best weather for pyramids and Nile cruises.',
  'May–Sep very hot; early morning tours only.',
  'Use registered guides; check regional travel notices.',
  'MEDIUM',
  'Stay on tourist routes; verify tour operator licenses.',
  1
);

-- -----------------------------------------------------------------------------
-- CLIENTS (4111–4115) + travel profiles
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_client (
  id, business_id, full_name, email, phone, passport_no, notes, active, travel_profile_json
) VALUES (
  4111, 41, 'Elena Vasquez', 'elena.vasquez@email.com', '+1-305-555-0142', 'P8849201US',
  'Repeat Europe traveler; prefers boutique hotels.',
  1,
  '{"savedDestinations":["Paris","Rome","Barcelona"],"preferredAirlines":["Air France","Emirates"],"preferredHotels":["Relais & Châteaux","Marriott"],"loyaltyProgram":"Flying Blue","loyaltyPoints":62400,"loyaltyStatus":"Gold"}'
);

INSERT INTO um.travel_client (
  id, business_id, full_name, email, phone, passport_no, notes, active, travel_profile_json
) VALUES (
  4112, 41, 'James & Sarah Mitchell', 'mitchell.family@email.com', '+44-20-7946-0958', 'P7723109GB',
  'Family of 4; need adjoining rooms.',
  1,
  '{"savedDestinations":["Bali","Dubai"],"preferredAirlines":["Qatar Airways","Singapore Airlines"],"preferredHotels":["Club Med","Jumeirah"],"loyaltyProgram":"Qatar Privilege Club","loyaltyPoints":18200,"loyaltyStatus":"Silver"}'
);

INSERT INTO um.travel_client (
  id, business_id, full_name, email, phone, passport_no, notes, active, travel_profile_json
) VALUES (
  4113, 41, 'Ahmed Al-Rashid', 'ahmed.rashid@email.com', '+971-50-123-4567', 'N9921847AE',
  'Corporate traveler; often books business class.',
  1,
  '{"savedDestinations":["Dubai","Tokyo","Paris"],"preferredAirlines":["Emirates","ANA"],"preferredHotels":["Four Seasons","Ritz-Carlton"],"loyaltyProgram":"Emirates Skywards","loyaltyPoints":118500,"loyaltyStatus":"Platinum"}'
);

INSERT INTO um.travel_client (
  id, business_id, full_name, email, phone, passport_no, notes, active, travel_profile_json
) VALUES (
  4114, 41, 'Sophie Laurent', 'sophie.laurent@email.com', '+33-6-12-34-56-78', '15AF88231FR',
  'Honeymoon specialist referrals.',
  1,
  '{"savedDestinations":["Paris","Bali"],"preferredAirlines":["Air France"],"preferredHotels":["Hôtel Plaza Athénée"],"loyaltyProgram":"Accor Live Limitless","loyaltyPoints":8900,"loyaltyStatus":"Classic"}'
);

INSERT INTO um.travel_client (
  id, business_id, full_name, email, phone, passport_no, notes, active, travel_profile_json
) VALUES (
  4115, 41, 'Robert Chen', 'robert.chen@email.com', '+1-415-555-8890', 'E4412098US',
  'Adventure travel; interested in Egypt & Japan.',
  1,
  '{"savedDestinations":["Tokyo","Cairo"],"preferredAirlines":["JAL","Turkish Airlines"],"preferredHotels":["Park Hyatt"],"loyaltyProgram":"OneWorld","loyaltyPoints":34100,"loyaltyStatus":"Gold"}'
);

-- -----------------------------------------------------------------------------
-- SUPPLIERS (4121–4126)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4121, 41, 'Emirates Airlines', 'AIRLINE', 'contracts@emirates.com', '+971-4-294-4444', 1,
  '2025 corporate agreement: 8% agency commission on published fares; net fares on request.',
  'Base 8% on international; 5% on regional. Bonus 2% above $250k quarterly sales.',
  'DXB-Europe Y: from $420 net; DXB-Asia J: from $1,850 net (sample Q2 2025).'
);

INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4122, 41, 'Marriott Europe DMC', 'HOTEL', 'dmc@marriott-eu.partner', '+33-1-45-00-0000', 1,
  'Preferred partner rates Paris/Rome: 15% off BAR, breakfast included for 3+ nights.',
  '10% commission on FIT; 12% on groups 10+ rooms.',
  'Paris 4*: €145/night; Rome 4*: €132/night shoulder season.'
);

INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4123, 41, 'Bali Paradise Tours', 'DMC', 'sales@baliparadise.id', '+62-361-555-0100', 1,
  'Ground handler for UBUD/SEMINYAK packages; includes transfers and temple tours.',
  '12% on land-only; guide upgrades net +15%.',
  'Private driver 8h: $85; Ubud day tour: $45 pax.'
);

INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4124, 41, 'Global Travel Insurance Co', 'INSURANCE', 'b2b@globaltravelins.com', '+1-800-555-0199', 1,
  'Agency policy: sell Comprehensive & Schengen plans; instant certificate API.',
  '25% commission on premium; no commission on claims handling.',
  'Schengen 14-day: $42; Worldwide 30-day: $89 per adult.'
);

INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4125, 41, 'Luxury Coach Europe', 'TRANSPORT', 'fleet@luxcoach.eu', '+39-06-555-0200', 1,
  'Motorcoach and private van contracts for Italy/France multi-city.',
  '8% on charter; fuel surcharge passed through at cost.',
  'Mercedes V-Class day: €380; 49-seat coach day: €950.'
);

INSERT INTO um.travel_supplier (
  id, business_id, name, supplier_type, contact_email, contact_phone, active,
  contracts_json, commission_notes, rate_table_notes
) VALUES (
  4126, 41, 'Japan Rail Partners', 'OTHER', 'groups@jrp.co.jp', '+81-3-5555-0100', 1,
  'JR Pass and Shinkansen group allocations for FIT and small groups.',
  '7% on pass sales; 5% on reserved seats.',
  '14-day JR Pass adult: ¥50,000; Tokyo-Kyoto reserved: ¥13,320.'
);

-- -----------------------------------------------------------------------------
-- PACKAGES (4131–4134)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_package (
  id, business_id, code, name, description, destination, duration_days,
  base_price, currency, active,
  inclusions, exclusions, itinerary_json, media_json,
  destinations_json, addons_json, max_capacity_per_day
) VALUES (
  4131, 41, 'EUR-PR-ROM-7', 'Paris & Rome Romance — 7 Nights',
  'Classic honeymoon route: 3 nights Paris, 4 nights Rome with high-speed train connection.',
  'Paris / Rome', 7, 2899.00, 'USD', 1,
  'Hotels 4*, daily breakfast, train Paris-Rome, Seine cruise, Vatican skip-line, airport transfers.',
  'International flights, lunches/dinners, personal expenses, travel insurance (optional add-on).',
  '[{"dayNumber":1,"title":"Arrive Paris","details":"Private transfer, Seine evening cruise"},{"dayNumber":2,"title":"Paris highlights","details":"Eiffel Tower summit, Louvre guided tour"},{"dayNumber":3,"title":"Train to Rome","details":"TGV connection, hotel check-in Trastevere"},{"dayNumber":4,"title":"Ancient Rome","details":"Colosseum, Roman Forum, Palatine Hill"},{"dayNumber":5,"title":"Vatican","details":"Museums and St. Peter''s Basilica"},{"dayNumber":6,"title":"Free day Rome","details":"Optional Tuscany day trip"},{"dayNumber":7,"title":"Departure","details":"Transfer FCO airport"}]',
  '[{"fileName":"paris-rome-hero.jpg","storageRef":"uploads/packages/demo/paris-rome-hero.jpg"}]',
  '[{"destinationId":4101,"destinationName":"Paris","nights":3},{"destinationId":4102,"destinationName":"Rome","nights":4}]',
  '[{"code":"INS","name":"Comprehensive travel insurance","description":"Covers medical and trip cancel","price":189.00},{"code":"GUIDE","name":"Private English guide — Rome","description":"Full day","price":320.00}]',
  12
);

INSERT INTO um.travel_package (
  id, business_id, code, name, description, destination, duration_days,
  base_price, currency, active,
  inclusions, exclusions, itinerary_json, media_json,
  destinations_json, addons_json, max_capacity_per_day
) VALUES (
  4132, 41, 'ASIA-BALI-10', 'Bali Wellness Retreat — 10 Days',
  'Ubud yoga, spa, rice terraces and beach finale in Seminyak.',
  'Bali', 10, 1999.00, 'USD', 1,
  '4* resorts, daily breakfast, 3 spa treatments, private tours, airport transfers.',
  'Flights, visa fees, alcohol, optional diving.',
  '[{"dayNumber":1,"title":"Arrival Denpasar","details":"Transfer to Ubud"},{"dayNumber":2,"title":"Ubud culture","details":"Temples and rice terraces"},{"dayNumber":3,"title":"Wellness","details":"Yoga and spa"},{"dayNumber":4,"title":"Free Ubud","details":"Optional cooking class"},{"dayNumber":5,"title":"South coast","details":"Transfer Seminyak"},{"dayNumber":6,"title":"Beach day","details":"Relaxation"},{"dayNumber":7,"title":"Snorkel option","details":"Nusa Penida day trip"},{"dayNumber":8,"title":"Spa & sunset","details":"Jimbaran dinner"},{"dayNumber":9,"title":"Leisure","details":"Pool and shopping"},{"dayNumber":10,"title":"Departure","details":"Airport transfer"}]',
  '[{"fileName":"bali-retreat.jpg","storageRef":"uploads/packages/demo/bali-retreat.jpg"}]',
  '[{"destinationId":4103,"destinationName":"Bali","nights":10}]',
  '[{"code":"INS","name":"Travel insurance","price":79.00},{"code":"DIVE","name":"Diving package (2 dives)","price":145.00}]',
  8
);

INSERT INTO um.travel_package (
  id, business_id, code, name, description, destination, duration_days,
  base_price, currency, active,
  inclusions, exclusions, itinerary_json, media_json,
  destinations_json, addons_json, max_capacity_per_day
) VALUES (
  4133, 41, 'ME-DXB-5', 'Dubai Luxury Long Weekend',
  'Burj Khalifa, desert safari, marina dinner cruise.',
  'Dubai', 5, 1599.00, 'USD', 1,
  '5* hotel, breakfast, safari, city tour, AT THE TOP tickets.',
  'Flights, visa, minibar, premium shopping.',
  '[{"dayNumber":1,"title":"Arrival","details":"VIP meet & greet"},{"dayNumber":2,"title":"City tour","details":"Old Dubai and souks"},{"dayNumber":3,"title":"Desert safari","details":"BBQ dinner"},{"dayNumber":4,"title":"Marina","details":"Yacht dinner cruise"},{"dayNumber":5,"title":"Departure","details":"Late checkout option"}]',
  '[]',
  '[{"destinationId":4104,"destinationName":"Dubai","nights":5}]',
  '[{"code":"LIMO","name":"Airport limousine","price":95.00}]',
  15
);

INSERT INTO um.travel_package (
  id, business_id, code, name, description, destination, duration_days,
  base_price, currency, active,
  inclusions, exclusions, itinerary_json, media_json,
  destinations_json, addons_json, max_capacity_per_day
) VALUES (
  4134, 41, 'ASIA-TYO-8', 'Tokyo & Kyoto Express',
  'JR Pass, Shinkansen, guided temples and food tour.',
  'Japan', 8, 3299.00, 'USD', 1,
  'Hotels 3–4*, JR Pass 7-day, guided tours, pocket WiFi.',
  'Flights, some lunches, personal shopping.',
  '[{"dayNumber":1,"title":"Tokyo arrival","details":"Asakusa and Shibuya"},{"dayNumber":2,"title":"Tokyo","details":"TeamLab and Tsukiji outer market"},{"dayNumber":3,"title":"Hakone","details":"Day trip Mt. Fuji views"},{"dayNumber":4,"title":"Kyoto","details":"Shinkansen, Gion evening"},{"dayNumber":5,"title":"Kyoto temples","details":"Fushimi Inari, Kiyomizu"},{"dayNumber":6,"title":"Nara","details":"Deer park day trip"},{"dayNumber":7,"title":"Tokyo return","details":"Akihabara free time"},{"dayNumber":8,"title":"Departure","details":"Narita/Haneda transfer"}]',
  '[]',
  '[{"destinationId":4105,"destinationName":"Tokyo","nights":5},{"destinationId":4105,"destinationName":"Kyoto","nights":3}]',
  '[{"code":"INS","name":"Japan travel insurance","price":65.00},{"code":"GUIDE","name":"Private Kyoto guide","price":280.00}]',
  10
);

-- -----------------------------------------------------------------------------
-- BOOKINGS (4141–4146) — full workflow variety
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4141, 41, 4111, 4131, 'HV-2025-0141', 'CONFIRMED',
  DATE '2025-09-10', DATE '2025-09-17', 5898.00, 'USD',
  'Honeymoon upgrade: Seine private yacht. Deposit received.',
  'INVOICING', 'APPROVED', 1,
  '[{"label":"Deposit 30%","amount":1769.40,"dueDate":"2025-06-01","paid":true},{"label":"Balance","amount":4128.60,"dueDate":"2025-08-15","paid":false}]'
);

INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4142, 41, 4112, 4132, 'HV-2025-0142', 'CONFIRMED',
  DATE '2025-07-20', DATE '2025-07-30', 7996.00, 'USD',
  'Family of 4 — adjoining rooms confirmed with DMC.',
  'TRAVEL', 'APPROVED', 1,
  '[{"label":"Deposit","amount":2000.00,"dueDate":"2025-05-01","paid":true},{"label":"Installment 2","amount":3000.00,"dueDate":"2025-06-15","paid":true},{"label":"Final","amount":2996.00,"dueDate":"2025-07-01","paid":true}]'
);

INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4143, 41, 4113, 4133, 'HV-2025-0143', 'QUOTED',
  DATE '2025-11-01', DATE '2025-11-06', 3198.00, 'USD',
  'Corporate retreat — awaiting client PO.',
  'QUOTATION', 'PENDING', 1,
  '[{"label":"Deposit on confirmation","amount":959.40,"dueDate":"2025-09-01","paid":false}]'
);

INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4144, 41, 4114, 4131, 'HV-2025-0144', 'ENQUIRY',
  DATE '2026-02-14', DATE '2026-02-21', 5798.00, 'USD',
  'Valentine honeymoon enquiry — quote sent.',
  'QUOTATION', 'NOT_REQUIRED', 0,
  NULL
);

INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4145, 41, 4115, 4134, 'HV-2025-0145', 'COMPLETED',
  DATE '2025-03-01', DATE '2025-03-09', 6598.00, 'USD',
  'Trip completed successfully; feedback 5 stars.',
  'CLOSURE', 'APPROVED', 1,
  '[{"label":"Paid in full","amount":6598.00,"dueDate":"2025-02-01","paid":true}]'
);

INSERT INTO um.travel_booking (
  id, business_id, client_id, package_id, reference_no, status,
  departure_date, return_date, total_amount, currency, notes,
  timeline_stage, approval_status, requires_approval, payment_schedule_json
) VALUES (
  4146, 41, 4115, NULL, 'HV-2025-0146', 'CANCELLED',
  DATE '2025-05-10', DATE '2025-05-18', 4200.00, 'USD',
  'Egypt custom tour cancelled — client health issue. Refund processed.',
  'CLOSURE', 'NOT_REQUIRED', 0,
  NULL
);

-- Passengers
INSERT INTO um.travel_booking_passenger (id, business_id, booking_id, full_name, passport_no, date_of_birth, nationality, seat_preference)
VALUES (4151, 41, 4141, 'Elena Vasquez', 'P8849201US', DATE '1988-04-12', 'US', 'Window');

INSERT INTO um.travel_booking_passenger (id, business_id, booking_id, full_name, passport_no, date_of_birth, nationality, seat_preference)
VALUES (4152, 41, 4141, 'Marco Vasquez', 'P8849202US', DATE '1986-11-03', 'US', 'Aisle');

INSERT INTO um.travel_booking_passenger (id, business_id, booking_id, full_name, passport_no, date_of_birth, nationality, seat_preference)
VALUES (4153, 41, 4142, 'James Mitchell', 'P7723109GB', DATE '1980-01-20', 'GB', NULL);
INSERT INTO um.travel_booking_passenger (id, business_id, booking_id, full_name, passport_no, date_of_birth, nationality, seat_preference)
VALUES (4154, 41, 4142, 'Sarah Mitchell', 'P7723110GB', DATE '1982-07-08', 'GB', NULL);

-- Booking components (supplier costs)
INSERT INTO um.travel_booking_component (id, business_id, booking_id, component_type, description, supplier_id, amount, status)
VALUES (4161, 41, 4141, 'HOTEL', 'Paris 3 nights Marriott', 4122, 980.00, 'CONFIRMED');
INSERT INTO um.travel_booking_component (id, business_id, booking_id, component_type, description, supplier_id, amount, status)
VALUES (4162, 41, 4141, 'TRANSPORT', 'Train + transfers', 4125, 420.00, 'CONFIRMED');
INSERT INTO um.travel_booking_component (id, business_id, booking_id, component_type, description, supplier_id, amount, status)
VALUES (4163, 41, 4142, 'DMC', 'Bali land package', 4123, 3200.00, 'CONFIRMED');
INSERT INTO um.travel_booking_component (id, business_id, booking_id, component_type, description, supplier_id, amount, status)
VALUES (4164, 41, 4142, 'INSURANCE', 'Family comprehensive', 4124, 316.00, 'CONFIRMED');

-- -----------------------------------------------------------------------------
-- TRIP REQUESTS (4241–4245) — quotation workflow
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_trip_request (
  id, business_id, client_id, destination_id, destination_name,
  departure_date, return_date, budget_amount, currency, traveler_count,
  traveler_details, status, quoted_amount, notes, assigned_to, booking_id
) VALUES (
  4241, 41, 4114, 4101, 'Paris',
  DATE '2026-02-14', DATE '2026-02-21', 6000.00, 'USD', 2,
  'Honeymoon couple; prefer river-view hotel.',
  'QUOTED', 5798.00, 'Matched to package EUR-PR-ROM-7', 'consultant.marie', NULL
);

INSERT INTO um.travel_trip_request (
  id, business_id, client_id, destination_id, destination_name,
  departure_date, return_date, budget_amount, currency, traveler_count,
  traveler_details, status, quoted_amount, notes, assigned_to, booking_id
) VALUES (
  4242, 41, 4115, 4106, 'Cairo & Nile cruise',
  DATE '2025-11-20', DATE '2025-11-30', 4500.00, 'USD', 2,
  'Interested in 4* cruise and pyramids tour.',
  'PENDING', NULL, 'Awaiting DMC quote from Egypt partner.', 'consultant.ali', NULL
);

INSERT INTO um.travel_trip_request (
  id, business_id, client_id, destination_id, destination_name,
  departure_date, return_date, budget_amount, currency, traveler_count,
  traveler_details, status, quoted_amount, notes, assigned_to, booking_id
) VALUES (
  4243, 41, 4113, 4104, 'Dubai',
  DATE '2025-11-01', DATE '2025-11-06', 3500.00, 'USD', 1,
  'Executive solo trip; business class flights separate.',
  'ACCEPTED', 3198.00, 'Client accepted — convert to booking HV-2025-0143', 'consultant.ali', 4143
);

INSERT INTO um.travel_trip_request (
  id, business_id, client_id, destination_id, destination_name,
  departure_date, return_date, budget_amount, currency, traveler_count,
  traveler_details, status, quoted_amount, notes, assigned_to, booking_id
) VALUES (
  4244, 41, 4112, 4103, 'Bali',
  DATE '2026-01-05', DATE '2026-01-15', 8500.00, 'USD', 4,
  'Repeat family trip; kids ages 8 and 11.',
  'CONVERTED', 7996.00, 'Converted to booking HV-2025-0142', 'consultant.marie', 4142
);

INSERT INTO um.travel_trip_request (
  id, business_id, client_id, destination_id, destination_name,
  departure_date, return_date, budget_amount, currency, traveler_count,
  traveler_details, status, quoted_amount, notes, assigned_to, booking_id
) VALUES (
  4245, 41, 4111, 4105, 'Tokyo',
  DATE '2025-12-01', DATE '2025-12-10', 7000.00, 'USD', 2,
  'Cherry blossom alternative — autumn foliage.',
  'REJECTED', 6899.00, 'Client chose competitor quote.', 'consultant.marie', NULL
);

-- -----------------------------------------------------------------------------
-- VISA APPLICATIONS (4171–4175)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_visa_application (
  id, business_id, client_id, booking_id, country, visa_type, status,
  submitted_at, decision_at, notes
) VALUES (
  4171, 41, 4111, 4141, 'France', 'TOURIST', 'APPROVED',
  TIMESTAMP '2025-06-10 09:00:00', TIMESTAMP '2025-06-20 14:30:00', 'Schengen visa issued 90 days.'
);

INSERT INTO um.travel_visa_application (
  id, business_id, client_id, booking_id, country, visa_type, status,
  submitted_at, decision_at, notes
) VALUES (
  4172, 41, 4112, 4142, 'Indonesia', 'TOURIST', 'APPROVED',
  TIMESTAMP '2025-05-15 10:00:00', TIMESTAMP '2025-05-18 11:00:00', 'VOA pre-registered.'
);

INSERT INTO um.travel_visa_application (
  id, business_id, client_id, booking_id, country, visa_type, status,
  submitted_at, decision_at, notes
) VALUES (
  4173, 41, 4113, 4143, 'UAE', 'BUSINESS', 'SUBMITTED',
  TIMESTAMP '2025-08-01 08:30:00', NULL, 'Corporate sponsorship letter attached.'
);

INSERT INTO um.travel_visa_application (
  id, business_id, client_id, booking_id, country, visa_type, status,
  submitted_at, decision_at, notes
) VALUES (
  4174, 41, 4115, 4145, 'Japan', 'TOURIST', 'APPROVED',
  TIMESTAMP '2025-01-10 09:00:00', TIMESTAMP '2025-01-18 16:00:00', NULL
);

INSERT INTO um.travel_visa_application (
  id, business_id, client_id, booking_id, country, visa_type, status,
  submitted_at, decision_at, notes
) VALUES (
  4175, 41, 4114, NULL, 'France', 'TOURIST', 'PENDING',
  NULL, NULL, 'Honeymoon 2026 — documents collection in progress.'
);

-- -----------------------------------------------------------------------------
-- DOCUMENTS (4181–4192)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4181, 41, 4111, 4141, 'PASSPORT', 'elena_vasquez_passport.pdf', 'uploads/demo/41/elena_passport.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4182, 41, 4111, 4141, 'VISA', 'elena_schengen_visa.pdf', 'uploads/demo/41/elena_visa.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4183, 41, 4111, 4141, 'TICKET', 'vasquez_af_flight.pdf', 'uploads/demo/41/vasquez_flights.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4184, 41, 4112, 4142, 'PASSPORT', 'mitchell_james_passport.pdf', 'uploads/demo/41/mitchell_j_passport.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4185, 41, 4112, 4142, 'INSURANCE', 'mitchell_family_insurance.pdf', 'uploads/demo/41/mitchell_insurance.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4186, 41, 4113, 4143, 'PASSPORT', 'ahmed_passport.pdf', 'uploads/demo/41/ahmed_passport.pdf', 'consultant.ali');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4187, 41, 4113, 4143, 'ID', 'ahmed_emirates_id.pdf', 'uploads/demo/41/ahmed_id.pdf', 'consultant.ali');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4188, 41, 4115, 4145, 'PASSPORT', 'robert_chen_passport.pdf', 'uploads/demo/41/chen_passport.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4189, 41, 4115, 4145, 'TICKET', 'chen_jal_tickets.pdf', 'uploads/demo/41/chen_tickets.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4190, 41, 4111, 4141, 'INVOICE', 'HV-INV-2025-041.pdf', 'uploads/demo/41/invoice_4141.pdf', 'finance');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4191, 41, 4114, NULL, 'PASSPORT', 'sophie_laurent_passport.pdf', 'uploads/demo/41/laurent_passport.pdf', 'consultant.marie');
INSERT INTO um.travel_document (id, business_id, client_id, booking_id, doc_type, file_name, storage_ref, uploaded_by)
VALUES (4192, 41, NULL, NULL, 'CONTRACT', 'emirates_agency_2025.pdf', 'uploads/demo/41/supplier_emirates_contract.pdf', 'admin');

-- (travel_document allows null client_id for supplier contracts)

-- -----------------------------------------------------------------------------
-- COMMISSION RULES (4221–4223)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_commission_rule (id, business_id, name, rule_type, rate_percent, flat_amount, active)
VALUES (4221, 41, 'Standard package commission', 'PERCENT', 10.0000, NULL, 1);
INSERT INTO um.travel_commission_rule (id, business_id, name, rule_type, rate_percent, flat_amount, active)
VALUES (4222, 41, 'Visa service flat fee', 'FLAT', NULL, 35.00, 1);
INSERT INTO um.travel_commission_rule (id, business_id, name, rule_type, rate_percent, flat_amount, active)
VALUES (4223, 41, 'High-value booking bonus', 'PERCENT', 2.5000, NULL, 1);

-- -----------------------------------------------------------------------------
-- INVOICES (4201–4205) + PAYMENTS (4211–4214)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_invoice (id, business_id, booking_id, invoice_no, amount, currency, status, issued_at, due_at)
VALUES (4201, 41, 4141, 'HV-INV-2025-041', 5898.00, 'USD', 'PARTIAL',
  TIMESTAMP '2025-06-15 10:00:00', TIMESTAMP '2025-08-15 23:59:59');
INSERT INTO um.travel_invoice (id, business_id, booking_id, invoice_no, amount, currency, status, issued_at, due_at)
VALUES (4202, 41, 4142, 'HV-INV-2025-042', 7996.00, 'USD', 'PAID',
  TIMESTAMP '2025-05-01 09:00:00', TIMESTAMP '2025-07-01 23:59:59');
INSERT INTO um.travel_invoice (id, business_id, booking_id, invoice_no, amount, currency, status, issued_at, due_at)
VALUES (4203, 41, 4143, 'HV-INV-2025-043', 3198.00, 'USD', 'ISSUED',
  TIMESTAMP '2025-08-20 11:00:00', TIMESTAMP '2025-09-15 23:59:59');
INSERT INTO um.travel_invoice (id, business_id, booking_id, invoice_no, amount, currency, status, issued_at, due_at)
VALUES (4204, 41, 4145, 'HV-INV-2025-045', 6598.00, 'USD', 'PAID',
  TIMESTAMP '2025-02-01 10:00:00', TIMESTAMP '2025-02-28 23:59:59');
INSERT INTO um.travel_invoice (id, business_id, booking_id, invoice_no, amount, currency, status, issued_at, due_at)
VALUES (4205, 41, 4141, 'HV-INV-2025-041B', 350.00, 'USD', 'OVERDUE',
  TIMESTAMP '2025-05-01 10:00:00', TIMESTAMP '2025-05-31 23:59:59');

INSERT INTO um.travel_payment (id, business_id, invoice_id, amount, payment_method, paid_at, reference_no)
VALUES (4211, 41, 4201, 1769.40, 'BANK_TRANSFER', TIMESTAMP '2025-06-01 14:22:00', 'WIRE-20250601-ELENA');
INSERT INTO um.travel_payment (id, business_id, invoice_id, amount, payment_method, paid_at, reference_no)
VALUES (4212, 41, 4202, 2000.00, 'CARD', TIMESTAMP '2025-05-01 11:05:00', 'CC-4482-MITCHELL');
INSERT INTO um.travel_payment (id, business_id, invoice_id, amount, payment_method, paid_at, reference_no)
VALUES (4213, 41, 4202, 5996.00, 'BANK_TRANSFER', TIMESTAMP '2025-06-20 09:30:00', 'WIRE-20250620-MITCHELL');
INSERT INTO um.travel_payment (id, business_id, invoice_id, amount, payment_method, paid_at, reference_no)
VALUES (4214, 41, 4204, 6598.00, 'ONLINE', TIMESTAMP '2025-02-05 16:45:00', 'PAYPAL-CHEN-2025');

-- Consultant commission ledger
INSERT INTO um.travel_consultant_commission (
  id, business_id, booking_id, consultant_user_id, commission_rule_id,
  base_amount, commission_amount, status, notes
) VALUES (
  4251, 41, 4142, NULL, 4221, 7996.00, 799.60, 'ACCRUED', 'Marie — Bali family package'
);
INSERT INTO um.travel_consultant_commission (
  id, business_id, booking_id, consultant_user_id, commission_rule_id,
  base_amount, commission_amount, status, paid_at, notes
) VALUES (
  4252, 41, 4145, NULL, 4221, 6598.00, 659.80, 'PAID', TIMESTAMP '2025-03-15 10:00:00', 'Marie — Japan trip closed'
);

-- -----------------------------------------------------------------------------
-- FOLLOW-UPS (4231–4235)
-- -----------------------------------------------------------------------------
INSERT INTO um.travel_follow_up (id, business_id, client_id, booking_id, subject, due_at, status, notes, assigned_to)
VALUES (4231, 41, 4111, 4141, 'Collect balance payment', TIMESTAMP '2025-08-10 17:00:00', 'OPEN',
  'Balance $4,128.60 due before departure.', 'consultant.marie');
INSERT INTO um.travel_follow_up (id, business_id, client_id, booking_id, subject, due_at, status, notes, assigned_to)
VALUES (4232, 41, 4113, 4143, 'Confirm corporate PO', TIMESTAMP '2025-09-05 12:00:00', 'IN_PROGRESS',
  'Waiting for Ahmed''s finance department.', 'consultant.ali');
INSERT INTO um.travel_follow_up (id, business_id, client_id, booking_id, subject, due_at, status, notes, assigned_to)
VALUES (4233, 41, 4114, NULL, 'Send Valentine package options', TIMESTAMP '2025-09-20 10:00:00', 'OPEN',
  'Link to trip request 4241.', 'consultant.marie');
INSERT INTO um.travel_follow_up (id, business_id, client_id, booking_id, subject, due_at, status, notes, assigned_to)
VALUES (4234, 41, 4115, NULL, 'Egypt DMC quote follow-up', TIMESTAMP '2025-08-25 15:00:00', 'OPEN',
  'Trip request 4242 — call Cairo partner for Nile cruise rates.', 'consultant.ali');
INSERT INTO um.travel_follow_up (id, business_id, client_id, booking_id, subject, due_at, status, notes, assigned_to)
VALUES (4235, 41, 4112, 4142, 'Post-trip feedback call', TIMESTAMP '2025-08-01 11:00:00', 'DONE',
  'Family loved Ubud resort — referral obtained.', 'consultant.marie');

COMMIT;

-- Optional: advance identity seeds past demo IDs (run if new inserts fail on duplicate id)
-- ALTER TABLE um.travel_destination MODIFY (id GENERATED BY DEFAULT ON NULL AS IDENTITY (START WITH LIMIT VALUE));

PROMPT Travel demo data loaded for business_id = 41 (Horizon Voyages scenario).
PROMPT Clients 4111-4115 | Packages 4131-4134 | Bookings 4141-4146 | Destinations 4101-4106
