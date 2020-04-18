package dev.reimer.progressbar.ktx

import me.tongfei.progressbar.*
import java.io.InputStream
import java.io.PrintStream
import java.text.DecimalFormat
import java.util.*
import java.util.stream.BaseStream
import java.util.stream.Stream
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.microseconds

@DslMarker
annotation class ProgressBarDsl

@ProgressBarDsl
class ProgressBarBuilderScope {

    private companion object {
        private val defaultDecimalFormat = DecimalFormat("#.0")
        private val emptyDecimalFormat = DecimalFormat()
    }

    var task: String = ""
    var initialMax: Long = -1
    var updateIntervalMillis: Int = 1000
    @ExperimentalTime
    var updateInterval: Duration
        get() = updateIntervalMillis.microseconds
        set(value) {
            updateIntervalMillis = value.inMilliseconds.toInt()
        }
    var style: ProgressBarStyle = ProgressBarStyle.COLORFUL_UNICODE_BLOCK
    var consumer: ProgressBarConsumer = InternalConsoleProgressBarConsumer()
    var out: PrintStream
        get() {
            return consumer.let { consumer ->
                check(consumer is InternalConsoleProgressBarConsumer) {
                    "Cannot get print stream from custom consumer."
                }
                consumer.out
            }
        }
        set(value) {
            consumer = InternalConsoleProgressBarConsumer(value)
        }
    var unitName: String = ""
    var unitSize: Long = 1
    var showSpeed: Boolean
        get() = speedFormat !== emptyDecimalFormat
        set(value) {
            speedFormat = when {
                value && speedFormat === emptyDecimalFormat -> defaultDecimalFormat
                else -> emptyDecimalFormat
            }
        }
    var speedFormat: DecimalFormat = emptyDecimalFormat

    fun unit(unitName: String, unitSize: Long) {
        this.unitName = unitName
        this.unitSize = unitSize
    }

    fun showSpeed(speedFormat: DecimalFormat?) {
        showSpeed = true
        this.speedFormat = speedFormat!!
    }

    internal fun builder(): ProgressBarBuilder {
        val builder = ProgressBarBuilder()
            .setTaskName(task)
            .setInitialMax(initialMax)
            .setUpdateIntervalMillis(updateIntervalMillis)
            .setStyle(style)
            .setConsumer(consumer)
            .setUnit(unitName, unitSize)
        if (showSpeed) {
            builder.showSpeed(speedFormat)
        }
        return builder
    }

    private fun buildWithConsumer(consumer: ProgressBarConsumer? = null): ProgressBar {
        val builder = ProgressBarBuilder()
            .setTaskName(task)
            .setInitialMax(initialMax)
            .setUpdateIntervalMillis(updateIntervalMillis)
            .setStyle(style)
            .setInitialMax(initialMax)
        if (consumer != null) {
            builder.setConsumer(consumer)
        }
        return builder.build()
    }

    private fun buildWithStream(printStream: PrintStream): ProgressBar {
        return ProgressBar(
            task,
            initialMax,
            updateIntervalMillis,
            printStream,
            style,
            unitName,
            unitSize,
            showSpeed,
            speedFormat
        )
    }
}

private fun progressBarBuilder(configure: ProgressBarBuilderScope.() -> Unit) =
    ProgressBarBuilderScope().apply(configure).builder()

fun progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): ProgressBar = progressBarBuilder(configure).build()

@JvmName("progressBarImmutable")
fun <T> Iterator<T>.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): Iterator<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))

fun <T> MutableIterator<T>.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): MutableIterator<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))

@JvmName("progressBarImmutable")
fun <T> Iterable<T>.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): Iterable<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))

fun <T> MutableIterable<T>.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): MutableIterable<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))

fun InputStream.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): InputStream =
    ProgressBar.wrap(this, progressBarBuilder(configure))

fun <T> Spliterator<T>.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): Spliterator<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))

fun <T, S : BaseStream<T, S>> S.progressBar(configure: ProgressBarBuilderScope.() -> Unit = {}): Stream<T> =
    ProgressBar.wrap(this, progressBarBuilder(configure))


