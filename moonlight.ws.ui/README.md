# moonlight.ws.ui (Angular-based UI)

## Run tasks

To run the dev server for your app, use:

```sh
npx nx serve moonlight.ws.ui
```

To create a production bundle:

```sh
npx nx build moonlight.ws.ui
```

To see all available targets to run for a project, run:

```sh
npx nx show project moonlight.ws.ui
```

These targets are either [inferred automatically](https://nx.dev/concepts/inferred-tasks?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) or defined in the `project.json` or `package.json` files.

[More about running tasks in the docs &raquo;](https://nx.dev/features/run-tasks?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

## Configuration

To configure a productive system, place the file `moonlight.ws.ui_rest.config.json` as a sibling next to your app-folder `moonlight.ws.ui`. It is not inside the app-folder, but a sibling to it, in order to avoid accidental overwriting the
configuration when re-deploying.

Example:

```json
{
  "restUrl": "https://dragonkingchocolate.com/moonlight.ws.rs",
  "openidUrl": "https://codewizards.co:4443",
  "openidRealm": "codewizards",
  "openidClientId": "dragonkingchocolate-webui"
}
```

The property `restUrl` must point to your backend. The properties prefixed by `openId` correspond to the "OpenID Connect"-configuration explained in the [backend-configuration](../moonlight.ws.rs/configuration.md).