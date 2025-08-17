# Kopipe CLI

**Kopipe CLI** is an intelligent command-line interface tool built on the [kopipe](https://github.com/jysohnn/kopipe)
library. This CLI is a context-aware AI assistant that accepts natural language input and utilizes Gemini's language
models and vector search to select and execute appropriate tools.

## Key Features

- **Natural Language Processing**: Understands user natural language input and performs appropriate tasks
- **Context Awareness**: Context-aware responses based on conversation history and project files
- **Automatic Tool Selection**: AI automatically selects and suggests shell tools that match user requests
- **Knowledge-Based Search**: Vectorizes files in the current directory for relevant information retrieval

## Installation and Build

```bash
# Clone the project
git clone https://github.com/jysohnn/kopipe-cli.git
cd kopipe-cli

# Option 1. Build and Run
./gradlew run --console=plain

# Option 2. Build Jar and Run
./gradlew clean shadowJar
java -jar build/libs/kopipe-cli-${VERSION}.jar
```

## Environment Setup

To use OpenAI API, set the environment variable:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

To use Gemini API, set the environment variable:

```bash
export GEMINI_API_KEY="your-api-key-here"
```

## Usage

### Basic Execution

```bash
./gradlew run --console=plain
```

When the program starts, the following prompt will appear:

```
[USER]
```

### Basic Commands

- Enter requests in natural language
- `/quit`: Exit the program

### Usage Examples

#### Example 1: File System Operations

```
[USER]
Find all .kts files in the current directory

[TOOL]
Do you want to use the shell_list tool with the following request? (yes/no)
{}

[USER]
yes

[TOOL]
The files in the current directory are as follows:
build
build.gradle.kts
gradle
gradle.properties
gradlew
gradlew.bat
README.md
settings.gradle.kts
src


[ASSISTANT]
Based on the list of files in the current directory, the following .kts (Kotlin Script) files are present:

- build.gradle.kts
- settings.gradle.kts
```

#### Example 2: Code Analysis

```
[USER]
Explain the main functionality of the Main.kt file

[ASSISTANT]
The Main.kt file implements a command-line interface (CLI) for interacting with a knowledge-augmented language model, integrated with tool selection and execution capabilities. Here’s an overview of its main functionality:

1. **Initialization**:
   - It sets up the main components:
     - A **ContextAwareLanguageModel** (uses OpenAILanguageModel) to handle conversations contextually.
     - A **knowledge store** (OpenAIEmbeddingVectorStore) is loaded with knowledge extracted from text/code/config files in the current directory.
     - A **ToolSelector** is created to choose appropriate tools (from ShellToolBox) based on user input and context.

2. **Interaction Loop**:
   - The CLI enters a loop where it repeatedly does the following:
     1. Reads user input.
     2. If the user types `/quit`, the application ends.
     3. Searches for relevant knowledge in the current directory to assist answering the user's query.
         - If found, adds this knowledge to the model’s context.
     4. Uses the tool selector to determine whether a tool should be invoked (based on context and input).
        - If a tool is suggested:
          - Shows the tool and its intended input to the user.
          - Asks the user for permission to run it.
          - Runs the tool if the user approves, and outputs the result; otherwise notes the refusal.
          - Updates the context with the tool call and result.
     5. Runs the context-aware language model to generate an assistant response, considering recent user input, tool actions, and retrieved knowledge.
     6. Prints the assistant’s output.
     7. Updates conversation context with the latest assistant response.

3. **Knowledge Initialization**:
   - The function `createKnowledgeOfCurrentDirectory()` scans all files in the current directory (up to ~1MB in size, with specific file extensions), and adds their contents as knowledge snippets for retrieval during conversations.

**In summary:**  
Main.kt enables a conversational assistant enhanced with local project knowledge and shell tool capabilities. It builds up a dynamic context from user conversation, project files, and tool execution to provide helpful, knowledge-rich, and actionable answers in a CLI environment
```

#### Example 3: Code Editing

````
[USER]
Improve HelloWorld.kt with code that outputs more interesting phrases, not HelloWorld.

[TOOL]
Do you want to use the shell_write tool with the following request? (yes/no)
{"file_name":"HelloWorld.kt","file_content":"fun main() {\n    println(\"Welcome to Kotlin Programming!\")\n    println(\"Today is a great day to learn new things.\")\n    println(\"Let's explore together!\")\n}"}

[USER]
yes

[TOOL]
The contents of HelloWorld.kt have been updated.

[ASSISTANT]
Here is an improved version of HelloWorld.kt that outputs more interesting phrases:

```kotlin
fun main() {
    println("Welcome to Kotlin Programming!")
    println("Today is a great day to learn new things.")
    println("Let's explore together!")
}
```

You can now run this code to see these new messages instead of the traditional "Hello World!" output.
````

## Supported File Formats

The following file formats are included in the knowledge base when scanning the current directory:

- `.txt`, `.kt`, `.js`, `.java`, `.kts`
- `.json`, `.yaml`, `.yml`, `.properties`
- File size limit: 1MB or less

## Requirements

- Kotlin 2.1.20+
- JVM 21+

## License

This project is based on the kopipe library and follows the license of that library.