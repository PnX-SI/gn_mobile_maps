# gn_mobile_maps

Small Android Map library based on [osmdroid](http://osmdroid.github.io/osmdroid/index.html).
* `maps`: The library itself
* `mountpoint`: Manage all available internal and external mount points on device
* `app`: Demo app

See [settings documentation](/maps).

## Full Build

A full build can be executed with the following command:

```bash
./gradlew clean assembleDebug
```

## Configure GitHub Packages Access

To publish or consume packages from GitHub Packages, we need to authenticate using a Personal Access
Token (PAT).
1. **Generate a Personal Access Token (PAT):**
   * Go to GitHub Settings > Developer settings > Personal access tokens > Tokens (classic).
   * Generate a new token with the `write:packages` (for publishing) and `read:packages` (for downloading) scopes.
2. **Configure Gradle:**
   * Add your GitHub username and the generated token to your global `local.properties` file (located at `~/local.properties`).
    ```
    gpr.user=YOUR_GITHUB_USERNAME
    gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
    ```

## Publish Android Libraries

Once authenticated, we can publish the library modules to the GitHub Package Registry.
Run the following command in the terminal:

```bash
./gradlew clean assembleRelease publish
```

This command will build the release version of the libraries (`maps`, `mountpoint`) and upload the
artifacts (AARs, POMs) to the configured repository.