# ktor-sentry - Sentry reporting for ktor
[![license](https://img.shields.io/github/license/zensum/ktor-sentry-feature.svg)]() [![](https://jitpack.io/v/zensum/ktor-sentry-feature.svg)](https://jitpack.io/#zensum/ktor-sentry-feature)

Ktor-sentry adds support for reporting unhandled exceptions
in [ktor](https://ktor.io) to [Sentry](https://sentry.io). Just set
the `SENTRY_DSN` environment variable and install the feature in ktor
and you're ready to go.

```kotlin
// Add an import...
import se.zensum.ktorSentry.SentryFeature

embeddedServer(Netty, 8080) {
    // ...and then in your server add the install
    install(SentryFeature)
}.start(wait = true)
```

## Installation
First add jitpack.io to your dependencies

``` gradle
maven { url 'https://jitpack.io' }
```

And then our dependency

``` gradle
dependencies {
            compile 'com.github.zensum:ktor-sentry-feature:-SNAPSHOT'
}
```

## Customization

Currently there are two customizable variables for the feature: The
`SENTRY_DSN` to use which, per default, is fetched from the
environment. The other variable `appEnv` sets the environment
(e.g. prod or dev) the exceptions are reported as occurring within.

```kotlin
import se.zensum.ktorSentry.SentryFeature

embeddedServer(Netty, 8080) {
    install(SentryFeature){
        dsn = yourSentryDSN // Override the default of SENTRY_DSN
        appEnv = yourAppEnv // Set an enviorment such as prod or dev for reported exceptions
    }
}.start(wait = true)
```
