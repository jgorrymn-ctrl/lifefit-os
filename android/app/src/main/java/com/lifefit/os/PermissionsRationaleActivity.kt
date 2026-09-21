package com.lifefit.os

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class PermissionsRationaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this).apply {
            text = """
                LifeFit uses Health Connect to read your health and fitness data.

                This includes activity, exercise, sleep, weight, body composition, resting heart rate, and HRV.

                LifeFit uses this information to provide personalized training, recovery, nutrition, and progress guidance.
            """.trimIndent()

            textSize = 18f
            setPadding(40, 60, 40, 40)
        }

        setContentView(text)
    }
}