package se.zensum.ktorSentry

import io.sentry.Sentry
import io.sentry.SentryClient
import org.jetbrains.ktor.application.ApplicationCallPipeline
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.pipeline.PipelineContext
import org.jetbrains.ktor.util.AttributeKey

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
        var customizeFn: CustomizeFn? = null
        fun customize(fn: CustomizeFn) { customizeFn = fn }

        internal fun initClient() {
            // If the user has specified a DSN
            if (dsn != null) {
                Sentry.init(dsn)
            }
            if (appEnv != null) {
                Sentry.getStoredClient().environment = appEnv
            }
            customizeFn?.invoke(Sentry.getStoredClient())
        }
    }

    suspend fun intercept(context: PipelineContext<Unit>) {
        sentryWrap { context.proceed() }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SentryFeature> {
        override val key = AttributeKey<SentryFeature>("SentryFeature")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SentryFeature {
            val cfg = Configuration().apply(configure)
            cfg.initClient()
            val result = SentryFeature()

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.intercept(this)
            }
            return result
        }
    }
}