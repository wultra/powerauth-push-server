# Migration from 1.6.x to 1.7.x

This guide contains instructions for migration from PowerAuth Push Server version `1.6.x` to version `1.7.x`.


## Database Changes


### Huawei Mobile Services

To support HMS, the columns `hms_project_id`, `hms_client_id`, and `hms_client_secret` have been added into the table `push_app_credentials`.
