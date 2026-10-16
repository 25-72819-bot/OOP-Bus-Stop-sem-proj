package com.example.myfirstapp

class BusRoute(
    val routeId: String,
    var routeName: String,
    var startPoint: String,
    var destination: String
) {
    private val busStop = mutableListOf<BusStop>()

    fun addBusStop(stop: BusStop){
        busStop.add(stop)
    }

    fun displayInfo(){
        println("Route ID: $routeId")
        println("Route Name: $routeName")
        println("Start: $startPoint")
        println("Destination: $destination")

        println("Bus Stop:")
        busStop.forEach {
            println("- ${it.stopName}")
        }
    }
}