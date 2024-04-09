insert into push_app_credentials(id, app_id) values
    (1, '1');

insert into push_device_registration (id, platform, push_token, timestamp_last_registered, app_id) values
    (1, 'ios', 'token1', now(), 1),
    (2, 'xxx', 'token2', now(), 1);

ALTER SEQUENCE push_device_registration_seq RESTART WITH 3;
