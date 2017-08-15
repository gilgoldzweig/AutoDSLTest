package annotations

/**
 * Created by gilgoldzweig on 04/07/2017.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
annotation class Column(
        //ignore: will not be added to the generated class
        val ignore: Boolean = false,
        //hasDefaultValues: notifying the processor that this field has a default value
        val hasDefaultValues: Boolean = true)