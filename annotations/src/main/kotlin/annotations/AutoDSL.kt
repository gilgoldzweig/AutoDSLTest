package annotations

/**
 * Created by gilgoldzweig on 04/07/2017.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@DslMarker
annotation class AutoDSL(val generatedClassName: String = "")
