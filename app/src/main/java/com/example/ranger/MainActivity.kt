package com.example.ranger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import com.example.ranger.ui.theme.RangerTheme
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import android.content.Context
import android.util.Log
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RangerTheme {
                RangeSelector()
            }
        }
    }
}

// Function to read data from the Excel file
fun readXlsxFile(context: Context, fileName: String): List<List<String>> {
    return try {
        val inputStream: InputStream = context.assets.open(fileName)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)
        val data = mutableListOf<List<String>>()

        for (row in sheet) {
            val rowData = mutableListOf<String>()
            for (cell in row) {
                rowData.add(cell.toString().trim()) // Convert all data to string and trim
            }
            if (rowData.isNotEmpty()) { // Avoid adding empty rows
                data.add(rowData)
            }
        }

        workbook.close()
        inputStream.close()

        Log.d("ExcelData", "Data loaded successfully: $data")
        data
    } catch (e: Exception) {
        Log.e("ExcelData", "Error reading Excel file: ${e.message}")
        emptyList() // Return an empty list if there's an error
    }
}

// Function to find a row based on the range input
fun findRowByRange(data: List<List<String>>, range: String): List<String>? {
    Log.d("SearchInput", "Searching for: $range")
    val trimmedRange = range.trim() // Ensure input is clean
    val result = data.find { it.getOrNull(0)?.trim() == trimmedRange } // Match against the first column "ت"
    Log.d("SearchResult", "Search result for $range: $result")
    return result
}

@Composable
fun RangeSelector() {
    val context = LocalContext.current
    val data = remember { readXlsxFile(context, "HE860.xlsx") }
    val ranges = data.mapNotNull { it.getOrNull(0) }
    var selectedRange by remember { mutableStateOf<String?>(null) }
    var result by remember { mutableStateOf<List<String>?>(null) }
    var expanded by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(
                        text = selectedRange ?: "اضغط هنا لاختيار المدى",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    ranges.forEach { range ->
                        DropdownMenuItem(
                            text = { Text(range, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                selectedRange = range
                                expanded = false
                                result = findRowByRange(data, range)
                                data(context, range)
                            }
                        )
                    }
                }

                // Results Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(12.dp)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val labels = listOf(
                            "حشوة تامة", "حيدان", "حشوة أولى", "حيدان",
                            "حشوة ثانية", "حيدان", "حشوة ثالثة", "حيدان"
                        )

                        for (i in labels.indices step 2) {
                            val label1 = labels[i]
                            val value1 = result?.getOrNull(i + 1)?.trim() ?: ""
                            val label2 = labels.getOrNull(i + 1) ?: ""
                            val value2 = result?.getOrNull(i + 2)?.trim() ?: ""

                            Column {
                                Text(
                                    text = "$label1: $value1",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "$label2: $value2",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Divider(
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                    thickness = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun data(context: Context, range: String) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        location?.let {
            val latitude = it.latitude
            val longitude = it.longitude

            val database = Firebase.database
            val ref = database.getReference("data")

            val data = mapOf(
                "range" to range,
                "location" to mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            )

            ref.push().setValue(data)
        }
    } else {
        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }
}

// Preview for development
@Preview(showBackground = true)
@Composable
fun RangeSelectorPreview() {
    RangerTheme {
        RangeSelector()
    }
}
