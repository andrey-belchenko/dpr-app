1. **Adding Shared npm Dependency:**

   If you have a dependency that is used across multiple packages, you can add it at the root level. Navigate to the root directory of your monorepo and run:

   ```
   yarn add <dependency-name> -W
   ```

   The `-W` or `--ignore-workspace-root-check` flag is used to allow the addition of dependencies at the workspace root level.

   Alternatively, if you want to add a dependency to the shared-components package, navigate to the `shared-components` directory and run:

   ```
   yarn add <dependency-name>
   ```

2. **Adding Dependency in Specific App:**

   If you want to add a dependency to a specific app, navigate to the directory of that app and run:

   ```
   yarn add <dependency-name>
   ```

   For example, if you want to add a dependency to `app1`, you would navigate to the `app1` directory and run the `yarn add` command.

   ```
   cd packages/app1
   yarn add <dependency-name>
   ```

   This will add the dependency to the `package.json` file of `app1` and install it in the `node_modules` directory of `app1`.

Remember to run `lerna bootstrap` after adding new dependencies. This command will ensure that all the dependencies are correctly linked across your packages.