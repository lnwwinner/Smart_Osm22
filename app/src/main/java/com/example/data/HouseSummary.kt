package com.example.data

data class HouseSummary(
    val householdId: Long,
    val houseNo: String,
    val villageNo: String = "",
    val totalMembers: Int,
    val males: Int,
    val females: Int,
    val owners: Int,
    val residents: Int,
    val deceased: Int = 0,
    val elderly: Int = 0,
    val earlyChild: Int = 0,
    val schoolAge: Int = 0,
    /** Total children covered by the household summary (early childhood + school age). */
    val children: Int
        get() = earlyChild + schoolAge,
    val teenager: Int = 0,
    val workingAge: Int = 0,
    val latitude: Double?,
    val longitude: Double?,
    val dataStatus: DataStatus = DataStatus.NEEDS_REVIEW,
    val headName: String? = null
)

