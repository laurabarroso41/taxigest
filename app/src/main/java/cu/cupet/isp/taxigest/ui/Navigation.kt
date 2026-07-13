package cu.cupet.isp.taxigest.ui

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable data object Home : Screen
    @Serializable data object Taxis : Screen
    @Serializable data object Clients : Screen
    @Serializable data object Trips : Screen
    @Serializable data object Reports : Screen
    @Serializable data object ImportContacts : Screen
    @Serializable data class DailyTrips(val date: Long) : Screen
    @Serializable data class ReportDetail(val type: String) : Screen
}
