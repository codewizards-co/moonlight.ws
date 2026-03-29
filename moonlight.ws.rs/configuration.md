# moonlight.ws.rs -- configuration

The backend is a RESTful service, packaged in a WAR, deployed in a WildFly app-server and storing its data in a PostgreSQL-database.

Right now, we do not yet provide moonlight as docker-image. But you still configure moonlight via environment-variables. Hence, you can easily deploy it using a 3rd-party-WildFly-docker-image (see below).


## WildFly (docker)

See [wildfly on Docker Hub](https://hub.docker.com/r/bitnami/wildfly) for how to set up WildFly as a docker-container. Deploy moonlight.ws.rs as WAR in this WildFly.


## PostgreSQL

See [postgres on Docker Hub](https://hub.docker.com/_/postgres/) for how to set it up as a docker-container.

You configure moonlight's database-connection via these env-vars:

* `DB_CONNECTION_URL`
    - default: `jdbc:postgresql://localhost:15432/moonlight`
* `DB_DRIVER`
    - default: `postgres`
* `DB_USER`
    - default: `postgres`
* `DB_PASSWORD`
    - default: `mypw123`

**Important:** The default-password exists only for development and should never be used in production!

You must create an empty database for moonlight, manually. This empty database is populated automatically, when the app-server (with moonlight.ws.rs deployed) starts up.


## OpenID Connect / OAuth 2.0 / Keycloak

We support only 1 single authentication-mechanism: OpenID Connect, preferrably via Keycloak

Consult your authentication-server's manual about how to configure it properly. You must setup at least a realm and a client.

Once you setup a client, there, you pass the coordinates via the following env-vars to moonlight:

* `OPENID_URL`
    - Required! (no default-value)
    - The base-URL of your authentication-server, for example: `https://codewizards.co:4443`
* `OPENID_REALM`
    - Required! (no default-value)
    - The realm in which users for moonlight are managed. For example: `codewizards` 
* `OPENID_CLIENT_ID`
    - Required! (no default-value)
    - You just created this client -- you should know its ID. For example: `dragonkingchocolate-webui`
* `OPENID_CLIENT_SECRET`
     - Optional or required, depending on your configuration. In general: When creating the client, you can choose whether the client must authenticate and how.
     - **Important:** For moonlight.ws, you *must not* use client-authentication! In this project's context, this must always be empty (just don't specify it at all). Client-authentication makes no sense in web-apps and is not supported by the keycloak-js-lib! Moonlight.ws' frontend (moonlight.ws.ui) is a web-app.


## Liferay

Moonlight must talk to your Liferay. Therefore, you must specify the following env-vars:

* `LIFERAY_URL`
    - Required! (no default value)
    - The base-URL of your Liferay-server, for example: `https://dragonkingchocolate.com:1443/liferay`
* `LIFERAY_USER`
    - Required! (no default-value)
    - So far, I didn't succeed to get Liferay accept the OpenID-bearer-token issued by our Keycloak, though OpenID-connect-authentication works via Liferay's web-site. Hence, moonlight currently uses this service-user to communicate with Liferay.
* `LIFERAY_PASSWORD`
    - Required! (no default-value)
    - The password belonging to the service-user-account specified by `LIFERAY_USER`.


## Global

The following global settings can be specified to fine-tune the behaviour of moonlight:

* `DEFAULT_CONNECT_TIMEOUT`
    - Optional.
    - The global JVM-wide TCP-connect-timeout in milliseconds. This is used for Liferay (tested) and it may also be used for other services.
    - Recommended value: `30000` (= 30 s = ½ min)
    - If not specified, the JVM's default value is unchanged (unless WildFly overrides it).
* `DEFAULT_READ_TIMEOUT`
    - Optional.
    - The global JVM-wide TCP-read-timeout in milliseconds. This is used for Liferay (tested) and it may also be used for other services.
    - Recommended value: `60000` (= 60 s = 1 min)
    - If not specified, the JVM's default value is unchanged (unless WildFly overrides it).
