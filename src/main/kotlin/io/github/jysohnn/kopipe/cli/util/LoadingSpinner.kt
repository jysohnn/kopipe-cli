package io.github.jysohnn.kopipe.cli.util

class LoadingSpinner(
    private val message: String
) {
    @Volatile
    private var running = false
    private var thread: Thread? = null
    private val lock = Any()
    private var lastFrameLen = 0
    private val intervalMs: Long = 100L
    private val out = System.out

    fun start() {
        out.print("\n")
        synchronized(lock) {
            if (running) return
            running = true
            thread = Thread {
                val symbols = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
                var idx = 0
                hideCursor()
                while (running) {
                    val frame = "\r$message ${symbols[idx]}"
                    synchronized(lock) {
                        out.print(frame)
                        out.flush()
                        lastFrameLen = frame.length - 1
                    }
                    Thread.sleep(intervalMs)
                    idx = (idx + 1) % symbols.size
                }
                showCursor()
            }.apply { isDaemon = true; start() }
        }
    }

    fun stop() {
        synchronized(lock) {
            if (!running) return
            running = false
        }
        thread?.join()
        synchronized(lock) {
            clearLine()
            out.flush()
        }
    }

    private fun clearLine() {
        out.print("\r")
        out.print(" ".repeat(maxOf(0, lastFrameLen)))
        out.print("\r")
    }

    private fun hideCursor() = out.print("\u001B[?25l")
    private fun showCursor() = out.print("\u001B[?25h")
}
