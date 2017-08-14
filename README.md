# ktor-sentry-feature

# Example of installation and configuration
```
val sentryDsn = "Some sentry DSN"
val appEnv = "development"

embeddedServer(Netty, 8080) {
    install(SentryFeature){
        dsn = sentryDsn
        appEnv = appEnv
    }
}.start(wait = true)
```