package com.example.myfirstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFirstAppTheme {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var result by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(text = "Hello $name!")

        Button(
            onClick = {
                testAllClasses()
                result = "All classes tested! Check Logcat."
            }
        ) {
            Text("Test All Classes")
        }

        Text(text = result)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyFirstAppTheme {
        Greeting("Android")
    }
}


fun testAllClasses(){
    val user = user(
        "U001",
        "Juan Dela Cruz",
        "juan@example.com",
        "pass123",
        "Passenger"
    )

    val bus = Bus("B001", "BUS_101", 40, "Available")

    val stop = BusStop(
        "S001",
        "Central Terminal",
        "Manila",
        14.5995,
        120.9842
    )

    val route = BusRoute(
        "R001",
        "Manila Route",
        "Central Manila",
        "City Center"
    )

    route.addBusStop(stop)

    val schedule = BusSchedule(
        "SCH001",
        bus,
        route,
        "08:00 AM",
        "09:00 AM"
    )

    user.displayInfo()
    bus.displayInfo()
    stop.displayInfo()
    route.displayInfo()
    schedule.displaySchedule()
}