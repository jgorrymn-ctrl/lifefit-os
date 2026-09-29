package com.lifefit.os

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var repository: HealthConnectRepository

    private val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    private var healthDataState by mutableStateOf<LifeFitHealthData?>(null)
    private var healthMessageState by mutableStateOf("Loading Health Connect…")

    private val permissionLauncher =
        registerForActivityResult(
            PermissionController.createRequestPermissionResultContract()
        ) {
            loadHealthData()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthConnectAvailable =
            HealthConnectClient.getSdkStatus(this) ==
                    HealthConnectClient.SDK_AVAILABLE

        if (healthConnectAvailable) {
            healthConnectClient = HealthConnectClient.getOrCreate(this)
            repository = HealthConnectRepository(healthConnectClient)
        } else {
            healthMessageState = "Health Connect is not available."
        }

        setContent {
            LifeFitTheme {
                LifeFitTodayScreen(
                    data = healthDataState,
                    healthMessage = healthMessageState,
                    onConnect = {
                        if (healthConnectAvailable) {
                            permissionLauncher.launch(permissions)
                        }
                    },
                    onRefresh = {
                        if (healthConnectAvailable) {
                            loadHealthData()
                        }
                    }
                )
            }
        }

        if (healthConnectAvailable) {
            loadHealthData()
        }
    }

    private fun loadHealthData() = lifecycleScope.launch {
        try {
            val granted =
                healthConnectClient.permissionController.getGrantedPermissions()

            if (!granted.containsAll(permissions)) {
                healthMessageState = "Health Connect permission needed"
                return@launch
            }

            healthMessageState = "Health Connect connected"
            healthDataState = repository.readHealthData()

        } catch (e: Exception) {
            healthMessageState =
                "Couldn't read Health Connect: ${e.message ?: "Unknown error"}"
        }
    }
}

@Composable
fun LifeFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = LifeFitBlue,
            secondary = LifeFitNavy,
            background = LifeFitBackground,
            surface = Color.White
        ),
        content = content
    )
}

@Composable
fun LifeFitTodayScreen(
    data: LifeFitHealthData?,
    healthMessage: String,
    onConnect: () -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        containerColor = LifeFitBackground,
        bottomBar = {
            LifeFitBottomBar()
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                containerColor = LifeFitBlue,
                contentColor = Color.White,
                text = {
                    Text(
                        "LifeFit Coach",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            LifeFitHeader()

            ReadinessCard()

            TrainingCard()

            ActivityCard(data)

            RecoveryCard(data)

            BodyCard(data)

            NutritionCard()

            HealthConnectCard(
                healthMessage = healthMessage,
                onConnect = onConnect,
                onRefresh = onRefresh
            )

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun LifeFitHeader() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "LifeFit",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = LifeFitNavy
                )

                Text(
                    text = "TODAY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LifeFitBlue
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Text(
                    text = "Jason  ▾",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 10.dp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = LifeFitNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Strong • Lean • Athletic • Healthy",
            fontSize = 15.sp,
            color = LifeFitMuted
        )
    }
}

@Composable
fun ReadinessCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = LifeFitNavy
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "READY",
                color = LifeFitGreen,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Train normally today",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = "LifeFit is beginning to learn your personal recovery baseline.",
                color = Color(0xFFC8D5E8),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TrainingCard() {
    LifeFitCard(
        title = "TODAY'S TRAINING"
    ) {
        Text(
            text = "Day 1 — Press / Chest",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LifeFitNavy
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Next in your 4 Day Routine",
            color = LifeFitMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "START WORKOUT",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ActivityCard(data: LifeFitHealthData?) {
    LifeFitCard(
        title = "ACTIVITY"
    ) {
        Text(
            text = formatNumber(data?.steps),
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = LifeFitNavy
        )

        Text(
            text = "steps recorded • last 7 days",
            color = LifeFitMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${data?.exerciseSessions ?: "—"} exercise sessions recorded",
            color = LifeFitBlue,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RecoveryCard(data: LifeFitHealthData?) {
    LifeFitCard(
        title = "RECOVERY"
    ) {

        MetricRow(
            label = "Sleep • 7 days",
            value = data?.sleepHours?.let {
                "%.1f h".format(it)
            } ?: "—"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = LifeFitDivider
        )

        MetricRow(
            label = "Resting HR",
            value = data?.restingHeartRate?.let {
                "$it bpm"
            } ?: "—"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = LifeFitDivider
        )

        MetricRow(
            label = "HRV • RMSSD",
            value = data?.hrvMs?.let {
                "%.0f ms".format(it)
            } ?: "—"
        )
    }
}

@Composable
fun BodyCard(data: LifeFitHealthData?) {
    LifeFitCard(
        title = "WEIGHT & BODY"
    ) {

        MetricRow(
            label = "Latest weight",
            value = data?.weightLb?.let {
                "%.1f lb".format(it)
            } ?: "—"
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            color = LifeFitDivider
        )

        MetricRow(
            label = "Body fat",
            value = data?.bodyFatPercent?.let {
                "%.1f%%".format(it)
            } ?: "—"
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Latest measurement available within 30 days",
            color = LifeFitMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
fun NutritionCard() {
    LifeFitCard(
        title = "NUTRITION"
    ) {
        Text(
            text = "Learning Mode",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = LifeFitNavy
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "LifeFit will learn your normal intake, recurring foods and habits before recommending calorie targets.",
            color = LifeFitMuted,
            fontSize = 14.sp
        )
    }
}

@Composable
fun HealthConnectCard(
    healthMessage: String,
    onConnect: () -> Unit,
    onRefresh: () -> Unit
) {
    LifeFitCard(
        title = "DATA CONNECTION"
    ) {

        Text(
            text = healthMessage,
            color = LifeFitMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onConnect,
                modifier = Modifier.weight(1f)
            ) {
                Text("Connect")
            }

            Button(
                onClick = onRefresh,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh")
            }
        }
    }
}

@Composable
fun LifeFitCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = title,
                color = LifeFitBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = LifeFitMuted,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = LifeFitNavy,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LifeFitBottomBar() {
    NavigationBar(
        containerColor = Color.White
    ) {

        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Text("●") },
            label = { Text("Today") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Text("○") },
            label = { Text("Training") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Text("○") },
            label = { Text("Nutrition") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Text("○") },
            label = { Text("Progress") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Text("○") },
            label = { Text("More") }
        )
    }
}

private fun formatNumber(value: Long?): String {
    if (value == null) return "—"

    return "%,d".format(value)
}

private val LifeFitBlue = Color(0xFF1769E0)
private val LifeFitNavy = Color(0xFF10284B)
private val LifeFitBackground = Color(0xFFF3F6FA)
private val LifeFitMuted = Color(0xFF65758B)
private val LifeFitDivider = Color(0xFFE5EAF0)
private val LifeFitGreen = Color(0xFF45D483)