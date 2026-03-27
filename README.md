# PowerAuth Push Server

PowerAuth Push Server is an optional application that facilitates sending APNs / FCM messages alongside the [PowerAuth Server](https://github.com/wultra) installation. It uses PowerAuth Server to obtain information about users, device activation state and to provide extra security features.

## Documentation

For the most recent documentation and tutorials, please visit [PowerAuth Push Server Documentation on GitHub](./docs/Readme.md) or visit [developers.wultra.com](https://developers.wultra.com/docs/develop/powerauth-push-server/).

## Getting Started

The easiest way to run PowerAuth Push Server is using the provided Docker image:

```bash
docker run -it -p 8080:8080 wultra/powerauth-push-server
```

For manual installation, you can download the latest `war` file from the [Releases](https://github.com/wultra/powerauth-push-server/releases) page and deploy it to a Java Servlet container (such as Apache Tomcat) with a properly configured `application.properties` file.

# License

All sources are licensed using Apache 2.0 license, you can use them with no restriction. If you are using PowerAuth, please let us know. We will be happy to share and promote your project.