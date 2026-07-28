# gn_mobile_maps

Small Android Map library based on [osmdroid](http://osmdroid.github.io/osmdroid/index.html).
* `maps`: The library itself
* `mountpoint`: Manage all available internal and external mount points on device
* `app`: Demo app

See [settings documentation](/maps).

Support at least Android 6 (API 23).

## Full Build

A full build can be executed with the following command:

```bash
./gradlew clean assembleDebug
```

## Import the Library

### 1. Configure Gradle

Add your GitHub username and the generated token to your global `local.properties` file (located at
`~/local.properties`):
```
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

### 2. Configure the GitHub Packages repository

Add the GitHub Packages Maven repository to your project's `settings.gradle` (or `settings.gradle.kts`):

**Groovy DSL (`settings.gradle`)**

```groovy
def localProperties = new Properties().tap {
   if (file("local.properties").exists()) {
      it.load(file("local.properties").newInputStream())
   }
}

dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_maps")
            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("USERNAME")
                password = localProperties.getProperty("gpr.key") ?: System.getenv("TOKEN")
            }
        }
    }
}
```

**Kotlin DSL (`settings.gradle.kts`)**

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/PnX-SI/gn_mobile_maps")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("USERNAME")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("TOKEN")
            }
        }
    }
}
```

### 3. Add the dependency

Add the following dependency to your module's `build.gradle` (or `build.gradle.kts`):

**Groovy DSL (`build.gradle`)**

```groovy
dependencies {
    implementation 'fr.geonature:maps:<version>'
}
```

**Kotlin DSL (`build.gradle.kts`)**

```kotlin
dependencies {
    implementation("fr.geonature:maps:<version>")
}
```

> The `maps` module transitively depends on `fr.geonature:mountpoint`. It will be resolved
> automatically. If you need the `mountpoint` utilities independently, you can also declare:
> ```groovy
> implementation 'fr.geonature:mountpoint:<version>'
> ```

### 4. Hilt setup

The library uses [Hilt](https://dagger.dev/hilt/) for dependency injection. Make sure your application
class is annotated with `@HiltAndroidApp` and that the Hilt plugin is applied to your module:

**`build.gradle` (module)**

```groovy
plugins {
    id 'com.google.dagger.hilt.android' version '2.55' apply false
    id 'com.google.devtools.ksp'
}

dependencies {
    implementation 'com.google.dagger:hilt-android:2.55'
    ksp 'com.google.dagger:hilt-android-compiler:2.55'
}
```

---

## Configure GitHub Packages Access

To publish or consume packages from GitHub Packages, we need to authenticate using a Personal Access
Token (PAT).
1. **Generate a Personal Access Token (PAT):**
   * Go to GitHub Settings > Developer settings > Personal access tokens > Tokens (classic).
   * Generate a new token with the `write:packages` (for publishing) and `read:packages` (for downloading)
     scopes.
2. **Configure Gradle:**
   * Add your GitHub username and the generated token to your global `local.properties` file (located
     at `~/local.properties`):
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