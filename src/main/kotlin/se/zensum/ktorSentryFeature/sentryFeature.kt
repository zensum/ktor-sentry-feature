package se.zensum.ktorSentry

import io.sentry.Sentry
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.application.ApplicationCallPipeline
import org.jetbrains.ktor.application.ApplicationFeature
import org.jetbrains.ktor.pipeline.PipelineContext
import org.jetbrains.ktor.util.AttributeKey

class SentryFeature(configuration: Configuration) {

    val sentryClient = Sentry.init(configuration.dsn).apply {environment = configuration.appEnv }

    class Configuration {
        var dsn: String? = null
        var appEnv: String = "n/a"
    }

    suspend fun intercept(context: PipelineContext<Unit>) {
        try{
            context.proceed()
        }catch (e: Exception){
            sentryClient.sendException(e)
            throw e
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SentryFeature> {
        override val key = AttributeKey<SentryFeature>("SentryFeature")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SentryFeature {
            val result = SentryFeature(Configuration().apply(configure))
            pipeline.intercept(ApplicationCallPipeline.Call) {
                result.intercept(this)
            }
            return result
        }
    }
}