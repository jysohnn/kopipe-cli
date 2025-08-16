package io.github.jysohnn.kopipe.cli

import io.github.jysohnn.kopipe.context.Role

object CommandLineInterface {
    private const val RESET_TEXT_COLOR = "\u001B[0m"
    private const val GREEN_TEXT_COLOR = "\u001B[32m"
    private const val BLUE_TEXT_COLOR = "\u001B[34m"
    private const val YELLOW_TEXT_COLOR = "\u001B[33m"

    fun readUserInput(): String {
        print(
            """
            |$GREEN_TEXT_COLOR
            |[${Role.USER}]
            |$RESET_TEXT_COLOR""".trimMargin()
        )
        return readln()
    }

    fun printToolInput(toolName: String, toolInput: String) {
        print(
            """
            |${YELLOW_TEXT_COLOR}
            |[${Role.TOOL}]
            |${toolName} 도구를 아래 요청값으로 사용하시겠습니까? (예/아니오)
            |$toolInput
            |$RESET_TEXT_COLOR""".trimMargin()
        )
    }

    fun printToolOutput(toolOutput: String) {
        print(
            """
            |${YELLOW_TEXT_COLOR}
            |[${Role.TOOL}]
            |$toolOutput
            |$RESET_TEXT_COLOR""".trimMargin()
        )
    }

    fun printAssistantOutput(assistantOutput: String) {
        print(
            """
            |${BLUE_TEXT_COLOR}
            |[${Role.ASSISTANT}]
            |$assistantOutput
            |$RESET_TEXT_COLOR""".trimMargin()
        )
    }
}