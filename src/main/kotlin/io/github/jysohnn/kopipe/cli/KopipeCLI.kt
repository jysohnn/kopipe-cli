package io.github.jysohnn.kopipe.cli

import io.github.jysohnn.kopipe.cli.util.LoadingSpinner
import io.github.jysohnn.kopipe.context.Context
import io.github.jysohnn.kopipe.context.Message
import io.github.jysohnn.kopipe.context.Role
import io.github.jysohnn.kopipe.knowledge.OpenAIEmbeddingVectorStore
import io.github.jysohnn.kopipe.pipe.languagemodel.ContextAwareLanguageModel
import io.github.jysohnn.kopipe.pipe.languagemodel.OpenAILanguageModel
import io.github.jysohnn.kopipe.tool.Tool
import io.github.jysohnn.kopipe.tool.ToolSelector
import io.github.jysohnn.kopipe.tool.shell.ShellToolBox
import java.io.File

fun main() {
    val context = Context()
    val knowledgeContext = Context()
    val toolContext = Context()

    val contextAwareLanguageModel = ContextAwareLanguageModel(
        languageModel = OpenAILanguageModel(),
        context = context,
        knowledgeContext = knowledgeContext,
        toolContext = toolContext
    )

    print("[INFO]\nGenerating knowledge based on the current directory...\n")
    val knowledgeStore = OpenAIEmbeddingVectorStore()
    knowledgeStore.store(
        knowledge = createKnowledgeOfCurrentDirectory()
    )
    println("=> Completed.")

    val toolSelector = ToolSelector(
        languageModel = OpenAILanguageModel(),
        tools = ShellToolBox.getAll()
    )

    val loadingSpinner = LoadingSpinner("Thinking")

    while (true) {
        val userInput = readUserInput()
        if (userInput == "/quit") break
        loadingSpinner.start()

        val knowledge = knowledgeStore.retrieve(
            query = userInput,
            minSimilarity = 0.8
        )
        val knowledgeFileName = knowledge?.let {
            val regex = Regex("""File Name:\s*(.+)""")
            val match = regex.find(it)

            match?.groupValues?.get(1)
        }

        knowledge?.let {
            val message = Message(Role.KNOWLEDGE, it)
            if (!knowledgeContext.contains(message)) {
                knowledgeContext.append(message)
            }
        }

        val (tool, toolInput) = toolSelector.select(
            input = userInput,
            context = context,
            knowledgeContext = knowledgeContext,
            toolContext = toolContext
        )

        tool?.let {
            loadingSpinner.stop()
            val result = invokeTool(tool, toolInput)
            loadingSpinner.start()

            toolContext.append(Message(Role.TOOL, result))
        }

        loadingSpinner.stop()

        println("$BLUE_TEXT_COLOR[${Role.ASSISTANT}]")
        contextAwareLanguageModel.executeByStreaming(input = userInput) { chunk ->
            print(chunk)
        }
        println("$RESET_TEXT_COLOR${if (knowledgeFileName != null) "\nSource: $knowledgeFileName" else ""}")
    }
}

private fun invokeTool(tool: Tool, input: String): String {
    val answer = if (tool.isUserConsentRequired) {
        printToolInput(toolName = tool.name, toolInput = input)
        readUserInput()
    } else {
        "yes"
    }

    var output = "User refuses to run tool."
    if (answer == "yes" || answer == "y") {
        if (tool.isUserConsentRequired) println()

        output = tool.invoke(input = input)
        printToolOutput(toolOutput = output)
    }

    return """
            |- Tool Name: ${tool.name}
            |- Tool Input: $input
            |- Tool Output: $output
           """.trimMargin()
}

private fun createKnowledgeOfCurrentDirectory(): List<String> {
    val knowledge = mutableListOf<String>()
    val maxFileSizeBytes = 1024 * 1024
    val availableFileExtension = listOf(
        ".txt", ".kt", ".kts", ".java", ".js", ".py", ".c", ".cpp", ".rb", ".ts",
        ".tsx", ".jsx", ".html", ".css", ".xml", ".json", ".yaml", ".yml", ".properties"
    )

    File(".").walk().forEach {
        if (!it.isFile) return@forEach
        if (it.length() > maxFileSizeBytes) return@forEach
        if (!availableFileExtension.any { extension -> it.name.endsWith(extension) }) return@forEach

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

private const val RESET_TEXT_COLOR = "\u001B[0m"
private const val GREEN_TEXT_COLOR = "\u001B[32m"
private const val BLUE_TEXT_COLOR = "\u001B[34m"
private const val YELLOW_TEXT_COLOR = "\u001B[33m"

private fun readUserInput(): String {
    print(
        """|$GREEN_TEXT_COLOR
           |[${Role.USER}]
           |$RESET_TEXT_COLOR""".trimMargin()
    )
    print(GREEN_TEXT_COLOR)
    val input = readln()
    print(RESET_TEXT_COLOR)

    return input
}

private fun printToolInput(toolName: String, toolInput: String) {
    print(
        """|$YELLOW_TEXT_COLOR[${Role.TOOL}]
           |Do you want to use the $toolName tool with the following request? (yes/no)
           |$toolInput
           |$RESET_TEXT_COLOR""".trimMargin()
    )
}

private fun printToolOutput(toolOutput: String) {
    print(
        """|$YELLOW_TEXT_COLOR[${Role.TOOL}]
           |$toolOutput
           |$RESET_TEXT_COLOR""".trimMargin()
    )
}