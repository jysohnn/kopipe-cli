package io.github.jysohnn.kopipe.cli

import io.github.jysohnn.kopipe.context.Message
import io.github.jysohnn.kopipe.context.Role
import io.github.jysohnn.kopipe.pipe.languagemodel.ContextAwareLanguageModel

fun ContextAwareLanguageModel.appendUserContext(text: String) =
    this.context.append(Message(Role.USER, text))

fun ContextAwareLanguageModel.appendAssistantContext(text: String) =
    this.context.append(Message(Role.ASSISTANT, text))

fun ContextAwareLanguageModel.appendKnowledgeContext(text: String) =
    this.context.append(Message(Role.KNOWLEDGE, text))

fun ContextAwareLanguageModel.appendToolContext(text: String) =
    this.context.append(Message(Role.TOOL, text))