insert into push_app_credentials(id, app_id) values
    (1001, '1');

insert into push_device_registration (id, platform, push_token, timestamp_last_registered, app_id) values
    (2001, 'ios', 'token1', now(), 1001);
