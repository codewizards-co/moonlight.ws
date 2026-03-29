# moonlight.ws.rs -- workspace-setup

In order to setup a developer-workspace, just follow this document step by step from top to bottom.


## Eclipse

It's recommended to use Eclipse (which is the only IDE currently tested), but you may use whatever IDE you like.


## Git

The project is hosted on [https://gitlab.codewizards.co/ml/moonlight.ws](https://gitlab.codewizards.co/ml/moonlight.ws) -- just clone it. You'll find the backend-part in the folder `moonlight.ws.rs`.


## Maven

The backend is written in Java and managed as Maven-project. Just run

```
mvn install
```
in this folder (`moonlight.ws.rs`) to compile the entire project.

 
## PostgreSQL (docker)
 
There's a `docker-compose.yml` in this folder. Just run
 
```
docker-compose up -d
```
here in this folder. This starts up a PostgreSQL-server which is going to be accessible with the following coordinates:
* host: **localhost**
* port: **15432**
* user: **postgres**
* password: **mypw123**


## Database

Start your favorite SQL-query-tool (e.g. pgAdmin or dbeaver) and **create the database** `moonlight` manually. Do not create any table. The database must be completely empty. The structure and initial data both are created by the first start of the app-server with the WAR being deployed.


## WildFly

We use WildFly as application-server. Setting up a local WildFly-instance is a semi-automatic process, consisting of these steps:

1. Go into the sub-directory `mlws.wildfly` and run `mvn install`. This creates a new folder named `wildfly` containing your app-server.

2. Do one of these:
    1. *either:* Add this new app-server to your IDE (e.g. to Eclipse' "Servers"-view) and start your server there.
    2. *or:* Start this new app-server in a shell/terminal/console via `wildfly/bin/standalone.sh`.

3. Run the script `./finalize-wildfly.sh` inside the folder `mlws.wildfly` (which must be your working-directory).


## WildFly -- environment-variables for database

The WildFly accepts environment-variables to configure the database-connection. The defaults should be OK and work fine with the PostgreSQL setup before. But if you want to change the configuration, just pass the env-vars specified in [Configuration](./configuration.md) (no need to change the `standalone.xml`).


## WildFly -- JVM-arguments

You must add the following JVM-arguments to Wildfly's launch-configuration:

```
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/sun.net.www.protocol.https=ALL-UNNAMED
```


## Deploy WAR

You may use your IDE's tooling (the WTP) for deploying the web-service or you may create a symlink. I prefer the symlink and I recommend to
do the same: Just create a symlink in `mlws.wildfly/wildfly/standalone/deployments/` pointing to `mlws.webservice/target/mlws.webservice`.


## OpenID Connect / OAuth 2.0 / Keycloak

We support only 1 single authentication-mechanism: OpenID Connect, preferrably via Keycloak

You must setup a Keycloak with a realm and a client for Liferay and moonlight.logistics. When your Keycloak is working fine, you must pass the coordinates as env-vars as specified in [Configuration](./configuration.md) (no need to change the `standalone.xml`).


## Restfox

Install [Restfox](https://github.com/flawiddsouza/Restfox) **as desktop-application**. Do **not** use it as a **web-app** hosted by someone else as this is going to leak your passwords!

Start your local Restfox and add the folder `mlws.restfox-workspace` as workspace. Note that this workspace is shared! It is essentially important never to store any password or other secret information in this workspace.

This means, we cannot directly store passwords in the environment. Instead, we add a plugin as follows:

Click on "Plugins", then "Add Plugin":

* Name: `Secret data`
* Scope: `All workspaces`

**Important!** Adding the plugin as a **global plugin** (i.e. scope "All workspaces") stores its data **outside of the workspace**. This is essential to prevent passwords from leaking (the workspace is public, because every git-repo may sooner or later be public).

* Code:

```
function preRequest() {
    if (rf.getEnvVar('mlws_url')) { 
        rf.setEnvVar('username', 'my_real_login')
        rf.setEnvVar('password', 'my_real_user-password')
    }
}

function postRequest() {
    if (rf.getEnvVar('mlws_url')) { 
        rf.setEnvVar('username', '')
        rf.setEnvVar('password', '')
    }
}

if('request' in rf) {
    preRequest()
}

if('response' in rf) {
    postRequest()
}
```

In order to invoke a request to the moonlight.logistics-service, you must first call `OpenID`/`getToken`. Then, you can access actual moonlight.logistics-resources.

**Warning!** This operation saves your bearer-token inside the public environment (=> git). This, however, is not a great danger, because such token is valid for only a short time. You should still be aware of it and possibly clear it when committing.
