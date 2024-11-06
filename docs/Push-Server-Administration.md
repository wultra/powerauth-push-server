# Push Server Administration

PowerAuth Push Server can be administered using a RESTful API.

The RESTful API is documented in [a dedicated chapter](./Push-Server-API.md).

## Administration using Insomnia

Insomnia is an easy to use RESTful API client. You can get Insomnia from [https://insomnia.rest](https://insomnia.rest)

To import the Push Server workspace into Insomnia, create a workspace using menu in the top left corner. Use the `Create Workspace` option and then click `Import/Export`. You can import the workspace with all requests from [provided workspace export file](./data/Push_Server_Insomnia.zip).

All requests which are described below are already prepared in the provided Insomnia worskpace, so you can easily update the JSON requests and execute them.

## Administration using cURL

Curl is a command line HTTP client. You can get cURL from [https://curl.haxx.se](https://curl.haxx.se)

### Retrieve Application List

```sh
curl --request GET \
  --url http://localhost:8080/powerauth-push-server/admin/app/list
```

### Retrieve Unconfigured Application List

```sh
curl --request GET \
  --url http://localhost:8080/powerauth-push-server/admin/app/unconfigured/list
```

### Create an Application

```sh
curl --request POST \
  --url http://localhost:8080/powerauth-push-server/admin/app/create \
  --json '{
  "requestObject": {
    "appId": 1
  }
}'
```

Update the `appId` value with requested PowerAuth application ID. The value `appId` from response object will be used for identification of the Push Server application.

### Get Application Detail

```sh
curl --request POST \
  --url http://localhost:8080/powerauth-push-server/admin/app/detail \
  --json '{
  "requestObject": {
    "appId": 1,
    "includeApns": true,
    "includeFcm": true,
    "includeHms": true
  }
}'
```

Update the `appId` value with requested Push Server application ID.

### Update APNs Configuration

```sh
curl --request POST \
  --url http://localhost:8080/powerauth-push-server/admin/app/apns \
  --json '{
  "requestObject": {
    "appId": 1,
    "bundle": "com.wultra.myApp",
    "keyId": "keyId",
    "teamId": "teamId",
    "privateKeyBase64": "a2V5"
  }
}'
```

Set the `appId` value for Push Server application ID to want to update.

Enter the base64-encoded value of APNs private key into `privateKeyBase64`.

You can encode the file using `base64` command on Mac. You can also use `Certutil.exe` on Windows or OpenSSL on all platforms.

```sh
base64 -i <in-file> -o <outfile>
```

### Remove APNs Configuration

```sh
curl --request DELETE \
  --url http://localhost:8080/powerauth-push-server/admin/app/apns?appId=mobile-app
```

Set the `appId` value for the Push Server application ID you want to update.

### Update FCM Configuration

```sh
curl --request POST \
  --url http://localhost:8080/powerauth-push-server/admin/app/fcm \
  --json '{
  "requestObject": {
    "appId": 1,
    "projectId": "projectId",
    "privateKeyBase64": "a2V5"
  }
}'
```

Set the `appId` value for Push Server application ID to want to update.

Enter the base64-encoded value of FCM private key into `privateKeyBase64`.

You can encode the file using `base64` command on Mac. You can also use `Certutil.exe` on Windows or OpenSSL on all platforms.

```sh
base64 -i <in-file> -o <outfile>
```

### Remove FCM Configuration

```sh
curl --request DELETE \
  --url http://localhost:8080/powerauth-push-server/admin/app/fcm?appId=mobile-app
```

Set the `appId` value for the Push Server application ID you want to delete.


### Update HMS Configuration

```sh
curl --request POST \
  --url http://localhost:8080/powerauth-push-server/admin/app/hms \
  --json '{
  "requestObject": {
    "appId": 1,
    "projectId": "projectId",
    "clientId": "oAuth 2.0 client ID",
    "clientSecret": "oAuth 2.0 client secret"
  }
}'
```

Set the `appId` value for Push Server application ID to want to update.

### Remove HMS Configuration

```sh
curl --request DELETE \
  --url http://localhost:8080/powerauth-push-server/admin/app/hms?appId=mobile-app
```

Set the `appId` value for the Push Server application ID you want to delete.

## Administration using SQL Database

Push server can be also administered by updating the `push_app_credentials` table.

See the [PowerAuth Push Server Database Structure](./Push-Server-Database.md) for more details.
