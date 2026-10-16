package com.example.myfirstapp

class Bus(
    val busId: String,
    val busNumber: String,
    var capacity: Int,
    var status: String
) {
    fun updateStatus(newStatus: String){
        status = newStatus
    }

    fun displayInfo() {
        println("Bus ID: $busId")
        println("Bus Number: $busNumber")
        println("Capacity: $capacity")
        println("Status: $status")
    }
}