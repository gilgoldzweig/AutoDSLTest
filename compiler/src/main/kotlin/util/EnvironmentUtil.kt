package util

import com.squareup.javapoet.JavaFile
import com.squareup.kotlinpoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
object EnvironmentUtil {
    private lateinit var processingEnvironment: ProcessingEnvironment
    private var initialize = false


    fun init(environment: ProcessingEnvironment) {
        processingEnvironment = environment
        initialize = true
    }

    fun logError(message: String) {
        if (!initialize) return
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    fun logWarning(message: String) {
        if (!initialize) return
        processingEnvironment.messager.printMessage(Diagnostic.Kind.WARNING, message)
    }
    fun savePath() : String {
        if (!initialize) return ""
        val generatedPath = processingEnvironment.options["kapt.kotlin.generated"]
        return generatedPath
                ?.replace("(.*)tmp(/kapt/debug/)kotlinGenerated".toRegex(), "$1generated/source$2")!!
                .replace("kaptKotlin", "kapt")
    }

    fun isSerializable(typeMirror: TypeMirror): Boolean {
        if (!initialize) return false
        val serializable = processingEnvironment.elementUtils
                .getTypeElement("java.io.Serializable").asType()
        return processingEnvironment.typeUtils.isAssignable(typeMirror, serializable)
    }

    fun isParcelable(typeMirror: TypeMirror): Boolean {
        if (!initialize) return false
        val parcelable = processingEnvironment.elementUtils
                .getTypeElement("android.os.Parcelable").asType()
        return processingEnvironment.typeUtils.isAssignable(typeMirror, parcelable)
    }
}// Empty private constructor