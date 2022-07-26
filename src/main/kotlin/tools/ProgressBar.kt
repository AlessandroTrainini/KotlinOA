package tools

import kotlin.math.floor

class ProgressBar(private val maxValue: Int) {
    private var currentValue: Int = 0
    private val barSize = 10
    private var oldPercentage: Int = -1
    private var currentIncrement: Int = 1


    fun updateAndPrintProgressBar() {
        updateCurrentValue()
        printProgressBar()
    }

    private fun updateCurrentValue() {
        currentValue += currentIncrement
        if (currentValue > maxValue) currentValue = maxValue
    }

    fun printProgressBar() {
        val percentage = getPercentage()
        if (oldPercentage != percentage) {
            val currentProgress = getProgess()
            val missingProgress = barSize - currentProgress
            print("\r" + "|" + " - ".repeat(currentProgress) + "   ".repeat(missingProgress) + "|" + " $percentage%")
            oldPercentage = percentage
            if (missingProgress <= 0) println()
        }
    }

    fun getPercentage(): Int {
        return floor(currentValue.toDouble() / maxValue * 100).toInt()
    }

    private fun getProgess(): Int {
        return floor(currentValue.toDouble() / maxValue * barSize).toInt()
    }

    fun updateAndCheck(): Boolean {
        updateCurrentValue()
        return currentValue < maxValue
    }

    fun increaseIncrement() {
        currentIncrement += 1
    }

    fun decreaseIncrement() {
        if (currentIncrement > 1) {
            currentIncrement /= 2
        }
    }

    fun setCurrentValue(newValue: Int) {
        currentValue = newValue
    }

    fun resetIncrement() {
        currentIncrement = 1
    }

    fun getCurrentValue(): Int {
        return currentValue
    }

}