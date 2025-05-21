INSERT INTO push_app_credentials (id, app_id) VALUES (1, 'my_app');

INSERT INTO push_device_registration (id, activation_id, platform, push_token, timestamp_last_registered, app_id)
    VALUES (1, 'a1', 'android', 't1', now(), 1),
           (2, 'a2', 'ios',     't1', now(), 1);
