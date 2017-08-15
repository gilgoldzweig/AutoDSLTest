package goldzweigapps.com.test

import annotations.AutoDSL
import java.text.DateFormat
import java.util.*

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
@AutoDSL()
data class Quick(
        var title2: String = "event create by Q-IT Enterprise Android App",

        var inviteeList: List<String> = emptyList(),

        var anonymousInviteeList: List<String> = emptyList(),

        var startDate: String = "",

        var timeFrameStart: String = "08:00+03:00",

        var eventDuration: String = "PT0H30M",

        var endDate: String = "",

        var timeFrameEnd: String = "19:30+03:00",

        var optionsNum: Int = 25,


        var initiator: String = "",


        var timeEventList: List<Any> = emptyList(),


        var irrelevantTimes: List<DateFormat> = emptyList(),


        var evaluationCommands: List<String> = listOf("SPECIAL_LOCATIONS"),

        var listOfUnwantedTimes: List<Any> = emptyList(),

        var overNight: Boolean = true,

        var conferenceRooms: Map<String, Calendar> = emptyMap(),

        var requestedEventType: String = "WORK",

        var ignoreSettings: List<String> = emptyList(),

        var onClick: String.(other: String) -> Unit = {})  {

//    constructor(quick: Quick.() -> Unit) : this() {
//        this.apply(quick)
//    }
}