package tools

import kotlin.math.floor

class ProgressBar(private val maxValue: Int) {
    private var currentValue: Int = 0
    private val barSize = 10
    private var oldPercentage: Int = 0


    fun updateProgressBar() {
        currentValue += 1
        val percentage = floor(currentValue.toDouble() / maxValue * 100).toInt()
        if (oldPercentage != percentage) {
            val currentProgress = floor(currentValue.toDouble() / maxValue * 10).toInt()
            val missingProgress = barSize - currentProgress
            print("\r" + "|" + " - ".repeat(currentProgress) + "   ".repeat(missingProgress) + "|" + " $percentage%")
            oldPercentage = percentage
            if (missingProgress == 0) println()
        }
    }

}