package class_generator
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec as JavaTypeSpec
import com.squareup.javapoet.TypeVariableName as JavaTypeVariableName
import com.squareup.javapoet.ParameterSpec as JavaParameterSpec
import com.squareup.kotlinpoet.*
import consts.packageName
import extentions.changeCharCase
import extentions.toTitle
import javax.lang.model.element.Modifier
import kotlin.reflect.KClass

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
class ClassGenerator {
    private lateinit var typeSpecBuilder: TypeSpec.Builder
    //    private lateinit var anonymousTypeSpecBuilder: TypeSpec.Builder
    private lateinit var javaTypeSpecBuilder: JavaTypeSpec
    private lateinit var kotlinFileBuilder: KotlinFile.Builder
    private lateinit var javaFileBuilder: JavaFile.Builder
    private lateinit var classConfigurationFile: ConfigurationFile
    private var classFinalVeriables = StringBuilder()
    var classGenericTypes = ""


    fun startClassCreation(classConfigurationFile: ConfigurationFile, supportJava: Boolean = false): ClassGenerator {
        classGenericTypes = ""
        for (finalElement in classConfigurationFile.finalElementsForDeclaration) {
            val first = finalElement.field.fieldName
            classFinalVeriables.append("$first = $first")

            if (finalElement != classConfigurationFile.finalElementsForDeclaration.last()) {
                classFinalVeriables.append(", ")
            }
        }
        classGenericTypes = if(classConfigurationFile.extendingClass.genericTypes.any())
            classConfigurationFile.extendingClass.genericTypes.toString()
                    .replace('[', '<')
                    .replace(']', '>')
        else ""
        typeSpecBuilder = TypeSpec.classBuilder(classConfigurationFile.generatedClassName)
                .addTypeVariables(classConfigurationFile
                        .extendingClass.genericTypes
                        .map { TypeVariableName.invoke(it) })
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameters(classConfigurationFile.finalElementsForDeclaration.map {
                            (field) ->
                            ParameterSpec
                                    .builder(field.fieldName,
                                            (field.fieldClass.canonicalName + if(field.genericTypes.any())
                                                field.genericTypes.toString()
                                                        .replace('[', '<')
                                                        .replace(']', '>')
                                            else "").asTypeName())
                                    .build()
                        }
                                .asIterable())
                        .build())
                .addProperty(PropertySpec.builder(classConfigurationFile.extendingClass.fieldName,
                        TypeVariableName.invoke(classConfigurationFile.extendingClass.fieldClass.canonicalName + classGenericTypes))
                        .initializer("${classConfigurationFile.extendingClass.fieldClass.canonicalName}$classGenericTypes($classFinalVeriables)")
                        .build())
//        anonymousTypeSpecBuilder = TypeSpec.anonymousClassBuilder("this.apply(_init)\nreturn ${classConfigurationFile.extendingClassName}\n")
        kotlinFileBuilder = KotlinFile.builder(packageName, classConfigurationFile.generatedClassName)

        this.classConfigurationFile = classConfigurationFile
        classConfigurationFile.functions.forEach { addFunction(it) }
//        kotlinFileBuilder.addStaticImport(Class.forName(classNameForParameterAsObject))
        return this
    }

    private fun createJavaBuilder(): ClassGenerator {
//        javaTypeSpecBuilder = JavaTypeSpec.classBuilder(presentableClassName)
//                .addMethod()

        return this
    }


    fun addFunction(function: FunctionConfigurationFile): ClassGenerator {
        val simpleName = "${function.field.fieldClass.simpleName()}${if (function.field.nullable) "?" else ""}"
        val generics = if(function.field.genericTypes.any())

            function.field.genericTypes.toString()
                    .replace('[', '<')
                    .replace(']', '>')
        else ""
        val classWithGenerics = "$simpleName$generics"
//            if (primitives.contains(function.variableClass.packageName())) {
        typeSpecBuilder
                .addProperty(createProperty(function.field.fieldName,classWithGenerics,
                        if (function.field.nullable) null else
                        "${classConfigurationFile.extendingClass.fieldName}.${function.field.fieldName}"))
                .addFun(FunSpec.builder(function.field.fieldName)
                        .addParameter(createParameterAsHigherFunction(simpleName, generics))
                        .addCode("""${classConfigurationFile.extendingClass.fieldName}.${function.field.fieldName}.apply(_init)
                                    |return ${classConfigurationFile.extendingClass.fieldName}.${function.field.fieldName}
                                    |
                                """.trimMargin())
                        .returns(TypeVariableName.invoke(classWithGenerics))
                        .build())
//            } else {
//                typeSpecBuilder
//                        .addFun(FunSpec.builder(function.variablesName)
//                                .addParameter(createParameterAsHigherFunction(function.variableClass))
//                                .addCode("""${classConfigurationFile.extendingClassName}.${function.variablesName}.apply(_init)
//                                    |return ${classConfigurationFile.extendingClassName}.${function.variablesName}
//                                    |
//                                """.trimMargin())
//                                .returns(function.variableClass)
//                                .build())
//                        .addFun(FunSpec.builder(function.variablesName)
//                                .addParameter(createParameter(function.variablesName, function.variableClass))
//                                .addCode("${classConfigurationFile.extendingClassName}.${function.variablesName} = ${function.variablesName}\n").build())
//
//            }
        return this
    }

    fun createClass(): KotlinFile {
        val funName = if (!classConfigurationFile
                .generatedClassName
                .contains(classConfigurationFile
                        .extendingClass
                        .fieldName, true)) classConfigurationFile
                .generatedClassName.changeCharCase(0, false) else classConfigurationFile
                .extendingClass
                .fieldName
        val file = kotlinFileBuilder
                .addType(typeSpecBuilder.build())
                .addFun(FunSpec.builder(funName)
                        .addTypeVariables(classConfigurationFile
                                .extendingClass.genericTypes
                                .map { TypeVariableName.invoke(it) })
                        .addParameters(classConfigurationFile.finalElementsForDeclaration.map {
                            (field) ->
                            ParameterSpec
                                    .builder(field.fieldName, (field.fieldClass.canonicalName + if(field.genericTypes.any())
                                        field.genericTypes.toString()
                                                .replace('[', '<')
                                                .replace(']', '>')
                                    else "").asTypeName())
                                    .build()
                        }
                                .asIterable())
                        .addParameter(createParameterAsHigherFunction(classConfigurationFile.generatedClassName, classGenericTypes))
                        .addCode("""
                                    |return ${classConfigurationFile
                                .generatedClassName}$classGenericTypes($classFinalVeriables).apply(_init).${classConfigurationFile.extendingClass.fieldName}
                                    |
                                """.trimMargin())
                        .returns(TypeVariableName.invoke(classConfigurationFile.extendingClass.fieldClass.canonicalName + classGenericTypes))
                        .build())
                .build()
        classFinalVeriables = StringBuilder()
        return file
    }

    //region util functions
    internal fun KClass<*>.asTypeName() =
            ClassName.bestGuess(qualifiedName ?: toString())
    internal fun String.asTypeName() = if (!contains("<")) ClassName.bestGuess(this) else
        TypeVariableName.invoke(this)

    private fun createParameterAsHigherFunction(variableClass: KClass<*>) =
            ParameterSpec.builder("_init",
                    LambdaTypeName.get(variableClass.asTypeName(),
                            emptyList(), UNIT)).build()

    private fun createParameterAsHigherFunction(variableClass: String) =
            ParameterSpec.builder("_init",
                    LambdaTypeName.get(ClassName.bestGuess(variableClass),
                            emptyList(), UNIT)).build()
    private fun createParameterAsHigherFunction(variableClassName: ClassName) =
            ParameterSpec.builder("_init",
                    LambdaTypeName.get(variableClassName,
                            emptyList(), UNIT))
                    .build()
//    private fun createLambda(name: String = "_init",
//                             receiverClass: String,
//                             genericTypes: String,
//                             parameters: ): ParameterSpec {
//        ParameterSpec.builder(name,
//                LambdaTypeName.get())
//    }
    private fun createParameterAsHigherFunction(variableClassName: String, generics: String) =
            ParameterSpec.builder("_init",
                    LambdaTypeName.get(TypeVariableName.invoke(variableClassName + generics),
                            emptyList(), UNIT))
                    .build()

//    private fun createMainHigherOrderFunction(variableClass: String) =
//            ParameterSpec.builder("_init",
//                    LambdaTypeName.get(ClassName.bestGuess(variableClass),
//                            emptyList(), ClassName.bestGuess(classQualifiedName))).build()

    private fun createParameter(variableName: String, variableClass: String) =
            ParameterSpec.builder(variableName, variableClass.asTypeName()).build()

    private fun createParameter(variableName: String, variableClass: KClass<*>) =
            ParameterSpec.builder(variableName, variableClass.asTypeName()).build()

    private fun createParameter(variableName: String, variableClass: ClassName) =
            ParameterSpec.builder(variableName, variableClass).build()
    private fun createParameter(variableName: String, variableClass: TypeName) =
            ParameterSpec.builder(variableName, variableClass).build()
    private fun createProperty(variableName: String, variableClass: String, variableDefaultValue: String? = null):
            PropertySpec {
        val isPropertyNull = variableDefaultValue == null

        val propertyClass = if (isPropertyNull) variableClass.asTypeName().asNullable() else variableClass.asTypeName().asNonNullable()
        return PropertySpec.builder(variableName, propertyClass)
                .initializer(variableDefaultValue ?: "null")
                .mutable(true)
                .setter(FunSpec.setterBuilder()
                        .addParameter(createParameter(variableName, propertyClass))
                        .addCode("""${classConfigurationFile.extendingClass.fieldName}.$variableName = $variableName
                                    |
                                """.trimMargin())
                        .build())
                .build()
    }


    private fun createProperty(variableName: String, variableClass: KClass<*>) =
            PropertySpec.builder(variableName, variableClass.asTypeName()).build()

    private fun createProperty(variableName: String, variableClass: ClassName) =
            PropertySpec.builder(variableName, variableClass).build()



    internal fun Class<*>.asTypeName() =
            JavaTypeVariableName.get(canonicalName ?: toString())

    internal fun String.asJavaTypeName() = JavaTypeVariableName.get(this)

    private fun createMethodAsBuilderFunction(variableName: String, variableClass: Class<*>) =
            MethodSpec.methodBuilder("with${variableName.toTitle()}")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)


    private fun createParameterAsBuilderFunction(variableName: String, variableClass: Class<*>) =
            JavaParameterSpec.builder(variableClass.asTypeName(), "with${variableName.toTitle()}")
                    .addModifiers(Modifier.PUBLIC)
                    .build()

    private fun createParameterAsBuilderFunction(variableName: String, variableClass: String) =
            JavaParameterSpec.builder(variableClass.asJavaTypeName(), "with${variableName.toTitle()}")
                    .addModifiers(Modifier.PUBLIC)
                    .build()

//    private fun createMainBuilderFunction() =
//            JavaParameterSpec.builder(classQualifiedName.asJavaTypeName(),
//                    "builder", Modifier.PUBLIC, Modifier.STATIC)
//                    .build()

    private fun createJavaParameter(variableName: String, variableClass: String) =
            JavaParameterSpec.builder(JavaTypeVariableName.get(variableClass), variableName)
                    .build()

    private fun createJavaParameter(variableName: String, variableClass: Class<*>) =
            JavaParameterSpec.builder(variableClass.asTypeName(), variableName)
                    .build()

//    private fun createProperty(variableName: String, variableClass: String, variableDefaultValue: String?):
//            PropertySpec {
//        val isPropertyNull = variableDefaultValue == null
//        val propertyClass = if (isPropertyNull) "$variableClass?" else variableClass
//        return PropertySpec.builder(variableName, propertyClass.asTypeName())
//                .initializer(variableDefaultValue ?: "null")
//                .mutable(true)
//                .setter(FunSpec.setterBuilder()
//                        .addParameter(createParameter(variableName, variableClass))
//                        .addCode("""
//                                |$presentableClassParameterName.$variableName = $variableName
//                                |""".trimMargin())
//                        .build())
//                .build()
//    }
//
//
//    private fun createProperty(variableName: String, variableClass: KClass<*>) =
//            PropertySpec.builder(variableName, variableClass.asTypeName()).build()
    //endregion util functions
}