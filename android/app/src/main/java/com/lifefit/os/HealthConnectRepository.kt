package com.lifefit.os

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

data class LifeFitHealthData(
    val steps: Long,
    val weightLb: Double?,
    val bodyFatPercent: Double?,
    val restingHeartRate: Long?,
    val hrvMs: Double?,
    val sleepHours: Double,
    val exerciseSessions: Int
)

class HealthConnectRepository(
    private val healthConnectClient: HealthConnectClient
) {

    suspend fun readHealthData(): LifeFitHealthData {

        val end = Instant.now()

        // Recent recovery/activity window
        val recentStart = end.minus(7, ChronoUnit.DAYS)
        val recentRange = TimeRangeFilter.between(recentStart, end)

        // Longer window for measurements that may not be taken every week
        val bodyStart = end.minus(30, ChronoUnit.DAYS)
        val bodyRange = TimeRangeFilter.between(bodyStart, end)

        val steps = healthConnectClient.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                recentRange
            )
        ).records.sumOf { it.count }

        val weights = healthConnectClient.readRecords(
            ReadRecordsRequest(
                WeightRecord::class,
                bodyRange
            )
        ).records

        val bodyFat = healthConnectClient.readRecords(
            ReadRecordsRequest(
                BodyFatRecord::class,
                bodyRange
            )
        ).records

        val restingHeartRate = healthConnectClient.readRecords(
            ReadRecordsRequest(
                RestingHeartRateRecord::class,
                recentRange
            )
        ).records

        val hrv = healthConnectClient.readRecords(
            ReadRecordsRequest(
                HeartRateVariabilityRmssdRecord::class,
                recentRange
            )
        ).records

        val sleep = healthConnectClient.readRecords(
            ReadRecordsRequest(
                SleepSessionRecord::class,
                recentRange
            )
        ).records

        val exercise = healthConnectClient.readRecords(
            ReadRecordsRequest(
                ExerciseSessionRecord::class,
                recentRange
            )
        ).records

        val latestWeight =
            weights.maxByOrNull { it.time }?.weight?.inPounds

        val latestBodyFat =
            bodyFat.maxByOrNull { it.time }?.percentage?.value

        val latestRestingHeartRate =
            restingHeartRate.maxByOrNull { it.time }?.beatsPerMinute

        val latestHrv =
            hrv.maxByOrNull { it.time }?.heartRateVariabilityMillis

        val totalSleepHours =
            sleep.sumOf {
                java.time.Duration
                    .between(it.startTime, it.endTime)
                    .toMinutes()
            } / 60.0

        return LifeFitHealthData(
            steps = steps,
            weightLb = latestWeight,
            bodyFatPercent = latestBodyFat,
            restingHeartRate = latestRestingHeartRate,
            hrvMs = latestHrv,
            sleepHours = totalSleepHours,
            exerciseSessions = exercise.size
        )
    }
}