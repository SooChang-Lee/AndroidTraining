package com.soochang.domain.model.direction

import com.google.gson.annotations.SerializedName

data class Direction(
    @SerializedName("trans_id") val transId: String = "",
    @SerializedName("routes") val routes: List<Route> = emptyList()
) {
    data class Route(
        @SerializedName("result_code") val resultCode: Int,
        @SerializedName("result_msg") val resultMsg: String,
        @SerializedName("summary") val summary: Summary,
        @SerializedName("sections") val sections: List<Section>
    ) {
        data class Summary(
            @SerializedName("origin") val origin: Origin,
            @SerializedName("destination") val destination: Destination,
            @SerializedName("waypoints") val waypoints: List<Any>,
            @SerializedName("priority") val priority: String,
            @SerializedName("bound") val bound: Bound,
            @SerializedName("fare") val fare: Fare,
            @SerializedName("distance") val distance: Int,
            @SerializedName("duration") val duration: Int
        ) {
            data class Origin(
                @SerializedName("name") val name: String,
                @SerializedName("x") val x: Double,
                @SerializedName("y") val y: Double
            )

            data class Destination(
                @SerializedName("name") val name: String,
                @SerializedName("x") val x: Double,
                @SerializedName("y") val y: Double
            )

            data class Bound(
                @SerializedName("min_x") val minX: Double,
                @SerializedName("min_y") val minY: Double,
                @SerializedName("max_x") val maxX: Double,
                @SerializedName("max_y") val maxY: Double
            )

            data class Fare(
                @SerializedName("taxi") val taxi: Int,
                @SerializedName("toll") val toll: Int
            )
        }

        data class Section(
            @SerializedName("distance") val distance: Int,
            @SerializedName("duration") val duration: Int,
            @SerializedName("bound") val bound: Bound,
            @SerializedName("roads") val roads: List<Road>,
            @SerializedName("guides") val guides: List<Guide>
        ) {
            data class Bound(
                @SerializedName("min_x") val minX: Double,
                @SerializedName("min_y") val minY: Double,
                @SerializedName("max_x") val maxX: Double,
                @SerializedName("max_y") val maxY: Double
            )

            data class Road(
                @SerializedName("name") val name: String,
                @SerializedName("distance") val distance: Int,
                @SerializedName("duration") val duration: Int,
                @SerializedName("traffic_speed") val trafficSpeed: Double,
                @SerializedName("traffic_state") val trafficState: Int,
                @SerializedName("vertexes") val vertexes: List<Double>
            )

            data class Guide(
                @SerializedName("name") val name: String,
                @SerializedName("x") val x: Double,
                @SerializedName("y") val y: Double,
                @SerializedName("distance") val distance: Int,
                @SerializedName("duration") val duration: Int,
                @SerializedName("type") val type: Int,
                @SerializedName("guidance") val guidance: String,
                @SerializedName("road_index") val roadIndex: Int
            )
        }
    }
}