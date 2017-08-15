package processors

import annotations.AutoDSL
import annotations.Column
import class_generator.ClassGenerator
import class_generator.ConfigurationFile
import class_generator.FunctionConfigurationFile
import com.squareup.kotlinpoet.*
import extentions.changeCharCase
import extentions.get
import extentions.isNullOrEmpty
import extentions.toTitle
import modules.Field
import org.jetbrains.annotations.Nullable
import util.EnvironmentUtil
import java.io.File
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.tools.Diagnostic
import kotlin.collections.ArrayList

/**
 * Created by gilgoldzweig on 04/07/2017.
 */
class AutoDSLProcessor : AbstractProcessor() {


    private var firstProcess = true
    private var round = -1
    private var HALT = false
    private var classGenerator: ClassGenerator = ClassGenerator()
    private val finalElementsInClass: ArrayList<Pair<String, String>> = ArrayList()
    private val funs: ArrayList<Triple<String, String, ArrayList<Pair<String, String>>>> = ArrayList()


    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        round++
        if (round == 0) EnvironmentUtil.init(processingEnv)

        if (!processClass(roundEnv)) return HALT
        if (!processColumns(roundEnv)) return false
        if (roundEnv.processingOver()) {
            try {
                HALT = true
            } catch (ex: IOException) {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, ex.toString())
            }

        }

        return false
    }

    private fun generateClass(classElement: TypeElement) {

        var extendingClass = classElement.qualifiedName.toString()
        val extendingClassName = classElement.simpleName.toString().changeCharCase(0, false)
        val generatedClassName: String
        EnvironmentUtil.logWarning("$extendingClass, ${classElement.typeParameters}")
        val annotation = classElement.getAnnotation(AutoDSL::class.java)
        if (annotation.generatedClassName.isEmpty()) {
            generatedClassName = "${extendingClassName.toTitle()}DSL"
        } else {
            generatedClassName = annotation.generatedClassName
        }
        val configurationFile = ConfigurationFile(generatedClassName,
                Field(extendingClassName, ClassName.bestGuess(extendingClass),
                        classElement.typeParameters.map { it.asType().toString()
                        }))


        val classConfFile =
                obtainClassFields(configurationFile, classElement.enclosedElements)

        val finalElementsInClass = ArrayList<Pair<String, String>>()
        classGenerator
                .startClassCreation(classConfFile, false)

//        classGenerator.createClass().writeTo(File(EnvironmentUtil.savePath()).toPath())
//        EnvironmentUtil.logWarning(classConfFile.toString())
//        val elementsInClass = getFinalEnclosingElements(classElement.enclosedElements)
//        classElement.asType().javaClass.declaredConstructors[0].parameters
//
//        for (element in elementsInClass) {
//            if (element == elementsInClass.first()) {

//            } else {
//                classGenerator.addFunction(element.first, element.second, *element.third.toTypedArray())
//            }
//        }
//        classGenerator.createClass().writeTo(System.out)
        classGenerator.createClass().writeTo(File(EnvironmentUtil.savePath()).toPath())
//        kotlinFile.writeTo(File(EnvironmentUtil.savePath()).toPath())
    }

    private fun processClass(roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(AutoDSL::class.java)
        if (elements.isNullOrEmpty()) return true
        for (element in elements) {
            if (element.kind != ElementKind.CLASS) {
                EnvironmentUtil.logError("AutoDSL can only be use for classes!")
                return false
            }
            if (!generateDSLClasses(element as TypeElement)) return false
        }
        return true
    }

    private fun obtainClassFields(configurationFile: ConfigurationFile, elements: List<Element>): ConfigurationFile {
        val functionsConfigurationFiles = ArrayList<FunctionConfigurationFile>()
        val finalFields = ArrayList<FunctionConfigurationFile>()
        elements.filter { it.kind.isField }
                .forEach {
                    val elementType = it.asType()
                    var genericTypes = (it as? TypeElement)?.typeParameters
                            ?.map { it.toString() } ?: emptyList()
                    val elementClassName = elementType.toString()
                    val elementModifiers = it.modifiers
                    val fieldAnnotation: Column? = it.getAnnotation(Column::class.java)
                    val isFinal = elementModifiers.contains(Modifier.FINAL)

                    val inNullable = it.getAnnotation(Nullable::class.java) != null
                    EnvironmentUtil.logWarning(it.annotationMirrors.toString() + "isNullable: $inNullable")
//            if (fieldAnnotation?.ignore ?: false) break
                    val simpleFieldName = it.simpleName.toString()

                    val fieldClassString = when {
                        elementClassName.contains("<") -> {
                            val startIndex = elementClassName['<']

                            val endIndex = elementClassName.lastIndexOf('>')
                            if (genericTypes.isEmpty()) {
                                val elementsAsString = elementClassName.substring(startIndex + 1, endIndex)
                                        .replace("? extends ", "")
                                        .replace("? super ", "")
                                        .replace("kotlin.jvm.functions.", "")
                                        .replace("java.lang.", "")
                                        .replace("Object", "Any" ,true)
                                        .replace("Void", "Unit" ,true)

                                genericTypes = elementsAsString.split(",")
//                                        .map {
//                                    if (it.contains("? extends ")) {
//                                        it.removePrefix("? extends ")
//                                    } else {
//                                        it
//                                    }
//                                }
                            }
                            if (elementClassName.contains("java.util")) {
                                elementClassName.replace("java.util", "kotlin.collections").substringBefore('<')
                            } else if (elementClassName.contains("kotlin.jvm.functions")) {
                                elementClassName.replace("kotlin.jvm.functions.", "").substringBefore('<')
                            } else {
                                elementClassName.substringBefore('<')
                            }

                        }
//                            elementClassName.removePrefix(elementClassName.substring(elementClassName['<'], elementClassName.lastIndexOf('>')))
                        elementClassName.contains("java.lang") ->
                            elementClassName.removePrefix("java.lang.").toTitle()
                        elementClassName.contains(".") ->
                            elementClassName
                        else ->
                            elementClassName.toTitle()
                    }
                    EnvironmentUtil.logWarning("$fieldClassString, $elementClassName, $elementType")
                    if (isFinal) {
                        finalFields.add(FunctionConfigurationFile(Field(
                                simpleFieldName,
                                ClassName.bestGuess(fieldClassString), genericTypes, inNullable)))
                    } else {
                        if ((fieldAnnotation != null && !fieldAnnotation.hasDefaultValues)) {
                            finalFields.add(FunctionConfigurationFile(Field(
                                    simpleFieldName,
                                    ClassName.bestGuess(fieldClassString), genericTypes, inNullable)))
                        }
                        functionsConfigurationFiles
                                .add(FunctionConfigurationFile(Field(
                                        simpleFieldName,
                                        ClassName.bestGuess(fieldClassString), genericTypes, inNullable)))
                    }
                }
        configurationFile.functions = functionsConfigurationFiles
        configurationFile.finalElementsForDeclaration = finalFields
        return configurationFile
    }

    //    private fun getFinalEnclosingElements(elements: List<Element>): Pair<List<Pair<String, String>> {
//        val finalElements = ArrayList<Pair<Pair<String, String>, List<Pair<String, String>>>>()
//        val innerFinalElements = ArrayList<Pair<String, String>>()
//        EnvironmentUtil.logWarning(elements.toString())
//        elements
//                .filter { it.kind.isField }
//                .forEach {
//                    val simpleName = it.simpleName.toString()
//                    val generatedClassName = it.asType().toString()
//                    val variableClassName = when {
//                        generatedClassName.contains("java.lang") -> generatedClassName.removePrefix("java.lang.").toTitle()
//                        it.asType().toString().contains(".") -> generatedClassName
//                        else -> it.asType().toString().toTitle()
//                    }
//                    EnvironmentUtil.logWarning("""
//                        SimpleName: $simpleName
//                        QualifiedName: $variableClassName
//                        Modifiers: ${it.modifiers}
//                        Kind: ${it.kind}
//                    """.trimIndent())
//
//                    if (it.modifiers.contains(Modifier.FINAL)) {
//                        if (primitives.contains(generatedClassName)) {
//                            innerFinalElements.add(simpleName to variableClassName.toTitle())
//                        } else {
//                            val newElements = getFinalEnclosingElements(it.enclosedElements)
//                            for (element in newElements) {
//                                innerFinalElements.addAll(element.third)
//                            }
//                        }
//                        EnvironmentUtil.logWarning("simpleName: $simpleName, generatedClassName: ${variableClassName.toTitle()}")
//                    }
//                        finalElements.add(Triple(simpleName,
//                                variableClassName, innerFinalElements))
//                        EnvironmentUtil.logWarning(innerFinalElements.toString())
//                }
//        EnvironmentUtil.logWarning(finalElements.toString())
//        return finalElements
//    }
    private fun generateDSLClasses(element: TypeElement): Boolean {
        generateClass(element)
        return true
    }

    private fun processColumns(roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(Column::class.java)
        if (elements.isNullOrEmpty()) return true
        for (element in elements) {
//            if (element.kind != ElementKind.CLASS) {
//                EnvironmentUtil.logError("Column can only be used for classes!")
//                return false
//            }
            if (element is TypeElement) {
                if (!generateDSLClasses(element)) return false
            }

        }
        return true
    }

    //region overrides
    override fun getSupportedAnnotationTypes() =
            setOf(AutoDSL::class.java.canonicalName, Column::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
    //endregion overrides
}