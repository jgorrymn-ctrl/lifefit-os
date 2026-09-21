package com.lifefit.os

import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    private lateinit var hc: HealthConnectClient
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
    private val permissionLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { readHealth() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(40,60,40,40) }
        val title = TextView(this).apply { text="LifeFit OS 1.2"; textSize=28f }
        val sub = TextView(this).apply { text="Health Connect proof-of-concept\nJason • Strong • Lean • Athletic • Healthy"; textSize=16f; setPadding(0,12,0,28) }
        val connect = Button(this).apply { text="Connect Health Connect" }
        val refresh = Button(this).apply { text="Refresh my data" }
        output = TextView(this).apply { text="No Health Connect data read yet."; textSize=17f; setPadding(0,30,0,0) }
        root.addView(title); root.addView(sub); root.addView(connect); root.addView(refresh); root.addView(output)
        setContentView(root)

        if (HealthConnectClient.getSdkStatus(this) == HealthConnectClient.SDK_AVAILABLE) {
            hc = HealthConnectClient.getOrCreate(this)
            connect.setOnClickListener { permissionLauncher.launch(permissions) }
            refresh.setOnClickListener { readHealth() }
        } else {
            output.text = "Health Connect is not available on this device."
            connect.isEnabled=false; refresh.isEnabled=false
        }
    }

    private fun readHealth() = lifecycleScope.launch {
        val granted = hc.permissionController.getGrantedPermissions()
        if (!granted.containsAll(permissions)) { output.text="LifeFit needs Health Connect read permission. Tap Connect Health Connect."; return@launch }
        val end=Instant.now(); val start=end.minus(30, ChronoUnit.DAYS); val range=TimeRangeFilter.between(start,end)
        val steps=hc.readRecords(ReadRecordsRequest(StepsRecord::class, range)).records.sumOf { it.count }
        val weights=hc.readRecords(ReadRecordsRequest(WeightRecord::class, range)).records
        val fat=hc.readRecords(ReadRecordsRequest(BodyFatRecord::class, range)).records
        val rhr=hc.readRecords(ReadRecordsRequest(RestingHeartRateRecord::class, range)).records
        val hrv=hc.readRecords(ReadRecordsRequest(HeartRateVariabilityRmssdRecord::class, range)).records
        val sleep=hc.readRecords(ReadRecordsRequest(SleepSessionRecord::class, range)).records
        val exercise=hc.readRecords(ReadRecordsRequest(ExerciseSessionRecord::class, range)).records
        val latestWeight=weights.maxByOrNull{it.time}?.weight?.inPounds
        val latestFat=fat.maxByOrNull{it.time}?.percentage?.value
        val latestRhr=rhr.maxByOrNull{it.time}?.beatsPerMinute
        val latestHrv=hrv.maxByOrNull{it.time}?.heartRateVariabilityMillis
        val sleepHours=sleep.sumOf { java.time.Duration.between(it.startTime,it.endTime).toMinutes() }/60.0
        output.text = buildString {
            appendLine("REAL HEALTH CONNECT DATA — LAST 7 DAYS")
            appendLine("Steps: ${steps}")
            appendLine("Weight: ${latestWeight?.let{"%.1f lb".format(it)} ?: "—"}")
            appendLine("Body fat: ${latestFat?.let{"%.1f%%".format(it)} ?: "—"}")
            appendLine("Resting HR: ${latestRhr?.let{"$it bpm"} ?: "—"}")
            appendLine("HRV (RMSSD): ${latestHrv?.let{"%.0f ms".format(it)} ?: "—"}")
            appendLine("Sleep recorded: ${"%.1f h".format(sleepHours)}")
            appendLine("Exercise sessions: ${exercise.size}")
            appendLine("\nMilestone: Health Connect → LifeFit is working.")
        }
    }
}
