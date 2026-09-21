package com.lifefit.os

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var hc: HealthConnectClient
    private lateinit var repository: HealthConnectRepository
    private lateinit var output: TextView

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    private val permissionLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) {
            readHealth()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 40)
        }

        val title = TextView(this).apply {
            text = "LifeFit OS 1.2"
            textSize = 28f
        }

        val sub = TextView(this).apply {
            text =
                "Health Connect proof-of-concept\nJason • Strong • Lean • Athletic • Healthy"
            textSize = 16f
            setPadding(0, 12, 0, 28)
        }

        val connect = Button(this).apply {
            text = "Connect Health Connect"
        }

        val refresh = Button(this).apply {
            text = "Refresh my data"
        }

        output = TextView(this).apply {
            text = "No Health Connect data read yet."
            textSize = 17f
            setPadding(0, 30, 0, 0)
        }

        root.addView(title)
        root.addView(sub)
        root.addView(connect)
        root.addView(refresh)
        root.addView(output)

        setContentView(root)

        if (
            HealthConnectClient.getSdkStatus(this) ==
            HealthConnectClient.SDK_AVAILABLE
        ) {
            hc = HealthConnectClient.getOrCreate(this)

            repository = HealthConnectRepository(hc)

            connect.setOnClickListener {
                permissionLauncher.launch(permissions)
            }

            refresh.setOnClickListener {
                readHealth()
            }
        } else {
            output.text = "Health Connect is not available on this device."
            connect.isEnabled = false
            refresh.isEnabled = false
        }
    }

    private fun readHealth() = lifecycleScope.launch {

        val granted =
            hc.permissionController.getGrantedPermissions()

        if (!granted.containsAll(permissions)) {
            output.text =
                "LifeFit needs Health Connect read permission. Tap Connect Health Connect."
            return@launch
        }

        try {
            val data = repository.readHealthData()

            output.text = buildString {
                appendLine("REAL HEALTH CONNECT DATA")
                appendLine("Activity & recovery: last 7 days")
                appendLine("Body measurements: latest within 30 days")
                appendLine()

                appendLine("Steps: ${data.steps}")

                appendLine(
                    "Weight: ${
                        data.weightLb?.let {
                            "%.1f lb".format(it)
                        } ?: "—"
                    }"
                )

                appendLine(
                    "Body fat: ${
                        data.bodyFatPercent?.let {
                            "%.1f%%".format(it)
                        } ?: "—"
                    }"
                )

                appendLine(
                    "Resting HR: ${
                        data.restingHeartRate?.let {
                            "$it bpm"
                        } ?: "—"
                    }"
                )

                appendLine(
                    "HRV (RMSSD): ${
                        data.hrvMs?.let {
                            "%.0f ms".format(it)
                        } ?: "—"
                    }"
                )

                appendLine(
                    "Sleep recorded: ${
                        "%.1f h".format(data.sleepHours)
                    }"
                )

                appendLine(
                    "Exercise sessions: ${data.exerciseSessions}"
                )

                appendLine()
                appendLine(
                    "Milestone: Health Connect → LifeFit data layer is working."
                )
            }

        } catch (e: Exception) {
            output.text =
                "LifeFit couldn't read Health Connect data:\n${e.message}"
        }
    }
}