package io.github.jysohnn.kopipe.cli

import io.github.jysohnn.kopipe.knowledge.OpenAIEmbeddingVectorStore
import io.github.jysohnn.kopipe.pipe.languagemodel.ContextAwareLanguageModel
import io.github.jysohnn.kopipe.pipe.languagemodel.OpenAILanguageModel
import io.github.jysohnn.kopipe.tool.ToolSelector
import io.github.jysohnn.kopipe.tool.shell.ShellToolBox
import java.io.File

fun main() {
    val cli = CommandLineInterface

    val contextAwareLanguageModel = ContextAwareLanguageModel(
        languageModel = OpenAILanguageModel()
    )
    val knowledgeStore = OpenAIEmbeddingVectorStore(
        isPossibleToRepeatRetrieving = false
    )
    knowledgeStore.addAll(
        knowledge = createKnowledgeOfCurrentDirectory()
    )

    val toolSelector = ToolSelector(
        languageModel = OpenAILanguageModel(),
        tools = ShellToolBox.getAll()
    )

    while (true) {
        val userInput = cli.readUserInput()
        if (userInput == "/quit") break

        val knowledge = knowledgeStore.retrieve(
            query = userInput,
            minSimilarity = 0.8
        )
        knowledge?.let {
            contextAwareLanguageModel.appendKnowledgeContext(it)
        }

        val (tool, toolInput) = toolSelector.select(
            context = contextAwareLanguageModel.context,
            input = userInput
        )
        val toolResult = tool?.let {
            cli.printToolInput(toolName = tool.name, toolInput = toolInput)
            val answer = cli.readUserInput()

            var toolOutput = "User refuses to run tool."
            if (answer == "예") {
                toolOutput = tool.invoke(input = toolInput)

                cli.printToolOutput(toolOutput = toolOutput)
            }

            """|- Tool Name: ${it.name}
               |- Tool Input: $toolInput
               |- Tool Output: $toolOutput
            """.trimMargin()
        }

        contextAwareLanguageModel.appendUserContext(userInput)
        toolResult?.let { contextAwareLanguageModel.appendToolContext(it) }

        val assistantOutput = contextAwareLanguageModel.execute(
            input = userInput
        )
        cli.printAssistantOutput(assistantOutput = assistantOutput)

        contextAwareLanguageModel.appendAssistantContext(assistantOutput)
    }
}

private fun createKnowledgeOfCurrentDirectory(): List<String> {
    val knowledge = mutableListOf<String>()

    File(".").walk().forEach {
        if (!it.isFile) return@forEach
        if (it.length() > 1024 * 1024) return@forEach
        if (!listOf(
                ".txt", ".kt", ".js", ".java", ".kts", ".json", ".yaml", ".yml", ".properties"
            ).any { extension -> it.name.endsWith(extension) }
        ) return@forEach

        knowledge.add(
            """
                |- File Name: ${it.name}
                |- File Content:
                |${it.readText()}
            """.trimMargin()
        )
    }

    return knowledge
}