package Utils

import android.graphics.Color

open class SegmentColors
{
    companion object
    {
        private const val NUM_CLASSES = 23

        private val colorMap = mapOf(
                "Unlabeled" to Color.TRANSPARENT,
                "Building" to Color.parseColor("#bdc3c7"), // silver
                "Fence" to Color.parseColor("#f39c12"), // orange
                "Other" to Color.parseColor("#7f8c8d"), // gray
                "Pedestrian" to Color.parseColor("#16a085"), // green
                "Pole" to Color.parseColor("#d35400"), // pumpkin
                "RoadLine" to Color.parseColor("#f1c40f"), // yellow
                "Road" to Color.parseColor("#e74c3c"), // road
                "SideWalk" to Color.parseColor("#2ecc71"), // emerald
                "Vegetation" to Color.parseColor("#27ae60"), // green sea
                "Vehicles" to Color.parseColor("#3498db"), // peter river
                "Wall" to Color.parseColor("#8e44ad"), // wisteria
                "TrafficSign" to Color.parseColor("#f1c40f"), // yellow
                "Sky" to Color.parseColor("#3498db"), // peter river
                "Ground" to Color.parseColor("#34495e"), // wet asphalt
                "Bridge" to Color.parseColor("#16a085"), // green
                "RailTrack" to Color.parseColor("#34495e"), // wet asphalt
                "GuardRail" to Color.parseColor("#16a085"), // green
                "TrafficLight" to Color.parseColor("#f1c40f"), // yellow
                "Static" to Color.parseColor("#7f8c8d"), // gray
                "Dynamic" to Color.parseColor("#3498db"), // peter river
                "Water" to Color.parseColor("#2980b9"), // belize hole
                "Terrain" to Color.parseColor("#7f8c8d") // gray
        )
    }

    private val labels = arrayOf(
            "Unlabeled", "Building", "Fence", "Other", "Pedestrian", "Pole", "RoadLine", "Road", "SideWalk", "Vegetation",
            "Vehicles", "Wall", "TrafficSign", "Sky", "Ground", "Bridge", "RailTrack", "GuardRail", "TrafficLight", "Static", "Dynamic",
            "Water", "Terrain"
    )

    fun getLabels(): Array<String> {
        return labels
    }

    fun getColorForLabel(label: String): Int {
        return colorMap[label] ?: Color.WHITE
    }

    fun getColors(): IntArray {
        val colors = IntArray(NUM_CLASSES)
        for (i in 0 until NUM_CLASSES) {
            colors[i] = getColorForLabel(labels[i])
        }
        return colors
    }

    fun getSemanticLabel(label: Int): Pair<String, Int> {
        val index = label and 0xFFFF
        val classId = index shr 8
        val instanceId = index and 0xFF

        val semanticLabel = when (classId) {
            0 -> "Unlabeled"
            1 -> "Building"
            2 -> "Fence"
            3 -> "Other"
            4 -> "Pedestrian"
            5 -> "Pole"
            6 -> "RoadLine"
            7 -> "Road"
            8 -> "SideWalk"
            9 -> "Vegetation"
            10 -> "Vehicles"
            11 -> "Wall"
            12 -> "TrafficSign"
            13 -> "Sky"
            14 -> "Ground"
            15 -> "Bridge"
            16 -> "RailTrack"
            17 -> "GuardRail"
            18 -> "TrafficLight"
            19 -> "Static"
            20 -> "Dynamic"
            21 -> "Water"
            22 -> "Terrain"
            else -> "Unlabeled"
        }

        val color = colorMap[semanticLabel] ?: Color.TRANSPARENT
        return Pair(semanticLabel, color)
    }
}