package me.tongfei.progressbar

import java.io.PrintStream

internal class InternalConsoleProgressBarConsumer(
    internal val out: PrintStream = defaultOut
) : ConsoleProgressBarConsumer(out) {
    private companion object {
        private val defaultOut: PrintStream = System.err
    }
}
