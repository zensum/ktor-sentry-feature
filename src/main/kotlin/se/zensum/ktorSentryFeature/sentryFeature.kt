package se.zensum.ktorSentry

import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.pipeline.PipelineContext
import io.ktor.util.AttributeKey
import io.sentry.Sentry
import io.sentry.SentryClient

private typealias CustomizeFn = SentryClient.() -> Unit

private inline fun <T> sentryWrap(fn: () -> T) = try {
    fn()
} catch (ex: Exception) {
    Sentry.capture(ex)
    throw ex
} finally {
    Sentry.clearContext()
}

class SentryFeature private constructor() {

    class Configuration {
        var dsn: String? = null
        var appEnv: String? = null
        var serverName: String? = null
        var customizeFn: CustomizeFn? = null
        fun customize(fn: CustomizeFn) { customizeFn = fn }

        internal fun initClient() {
            // If the user has specified a DSN
            if (dsn != null) {
                Sentry.init(dsn)
            }

            Sentry.getStoredClient().apply {
                serverName?.let {
                    val user: String? = System.getProperty("user.name")
                    this.serverName =  user?.let { "$it@" } + hostname()
                }
                appEnv?.let {
                    this.environment = appEnv
                }
            }

            customizeFn?.invoke(Sentry.getStoredClient())
        }
    }

    suspend fun intercept(context: PipelineContext<Unit, ApplicationCall>) {
        sentryWrap { context.proceed() }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SentryFeature> {
        override val key = AttributeKey<SentryFeature>("SentryFeature")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SentryFeature {
            val cfg = Configuration().apply(configure)
            cfg.initClient()
            check(cfg.dsn != null) { "Sentry DSN must be set for Sentry to work" }
            val result = SentryFeature()

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.intercept(this)
            }
            return result
        }
    }
}