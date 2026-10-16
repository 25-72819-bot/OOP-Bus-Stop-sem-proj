package com.example.myfirstapp

class BusStop(
    val stopId: String,
    var stopName: String,
    var location: String,
    var latitude: Double,
    var longitude: Double
) {
    fun displayInfo(){
        println("Stop ID: $stopId")
        println("Stop Name: $stopName")
        println("Location: $location")
        println("Latitude: $latitude")
        println("Longitude: $longitude")
    }
}