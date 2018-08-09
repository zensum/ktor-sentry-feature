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

            val client = Sentry.getStoredClient()

            client.serverName = this.serverName
            if(client.serverName == null) {
                val user: String? = System.getProperty("user.name")
                val hostname: String? = user?.let { "$it@" } + hostname()
                client.serverName = hostname
            }

            appEnv?.let { env ->
                client.environment = env
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
            val result = SentryFeature()

            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.intercept(this)
            }
            return result
        }
    }
}