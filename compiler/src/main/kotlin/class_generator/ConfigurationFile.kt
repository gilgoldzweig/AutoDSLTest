package class_generator

import modules.Field

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
data class ConfigurationFile(var generatedClassName: String = "",
                             var extendingClass: Field,
                             var finalElementsForDeclaration: Iterable<FunctionConfigurationFile> = emptyList(),
                             var functions: Iterable<FunctionConfigurationFile> = emptyList())