package com.tafnit.transit.data.mock

import com.tafnit.transit.domain.model.AlertSeverity
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LineArrival
import com.tafnit.transit.domain.model.LineStopEntry
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.ServiceAlert
import com.tafnit.transit.domain.model.TransitLine
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TransitStop
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object IsraelTransitDataset {

    // Default Central Israel location (Rishon LeZion / Tel Aviv metro corridor)
    val DEFAULT_CENTRAL_ISRAEL = LatLng(32.0672, 34.7778) // Tel Aviv / Dizengoff-Azrieli area

    fun haversineMeters(a: LatLng, b: LatLng): Int {
        val r = 6371000.0
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLon = Math.toRadians(b.longitude - a.longitude)
        val lat1 = Math.toRadians(a.latitude)
        val lat2 = Math.toRadians(b.latitude)
        val h = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(h), sqrt(1 - h))
        return (r * c).roundToInt()
    }

    val defaultFavorites: List<FavoritePlace> = listOf(
        FavoritePlace(
            id = "fav_home",
            label = "Home",
            iconKey = "home",
            address = "Herzl St 48, Rishon LeZion",
            location = LatLng(31.9642, 34.8044),
            isPinned = true
        ),
        FavoritePlace(
            id = "fav_work",
            label = "Work",
            iconKey = "work",
            address = "Azrieli Center, Tel Aviv",
            location = LatLng(32.0741, 34.7922),
            isPinned = true
        ),
        FavoritePlace(
            id = "fav_weizmann",
            label = "Weizmann Park",
            iconKey = "star",
            address = "Weizmann St, Nes Ziona",
            location = LatLng(31.9293, 34.7987),
            isPinned = false
        )
    )

    val stops: List<TransitStop> = listOf(
        TransitStop(
            id = "stop_20145",
            code = "20145",
            nameEn = "Azrieli Mall / Menachem Begin",
            nameHe = "קניון עזריאלי / דרך מנחם בגין",
            cityEn = "Tel Aviv-Yafo",
            cityHe = "תל אביב-יפו",
            location = LatLng(32.0744, 34.7916),
            lines = listOf("274", "164", "63", "R1", "51"),
            upcomingArrivals = listOf(
                LineArrival("line_274", "274", TransitOperator.EGGED, TransitMode.BUS, "Rehovot Central", "רחובות ת. מרכזית", 3, true, "18:27", "v_274_1"),
                LineArrival("line_63", "63", TransitOperator.DAN, TransitMode.BUS, "Aluf Sade Terminal", "מסוף אלוף שדה", 5, true, "18:29", "v_63_1"),
                LineArrival("line_r1", "R1", TransitOperator.DANKAL, TransitMode.LIGHT_RAIL, "Bat Yam HaKomemiyut", "בת ים הקוממיות", 6, true, "18:30", "v_r1_1", "Platform 1"),
                LineArrival("line_164", "164", TransitOperator.EGGED, TransitMode.BUS, "Rishon LeZion", "ראשון לציון", 11, false, "18:35")
            )
        ),
        TransitStop(
            id = "stop_25501",
            code = "25501",
            nameEn = "Tel Aviv HaShalom Railway",
            nameHe = "תחנת רכבת תל אביב השלום",
            cityEn = "Tel Aviv-Yafo",
            cityHe = "תל אביב-יפו",
            location = LatLng(32.0734, 34.7933),
            lines = listOf("IC", "SUB", "274", "56"),
            upcomingArrivals = listOf(
                LineArrival("line_rail_ta_rlz", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Rishon LeZion HaRishonim", "ראשון לציון הראשונים", 4, true, "18:28", "v_rail_1", "Platform 2"),
                LineArrival("line_rail_haifa", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Haifa Hof HaCarmel", "חיפה חוף הכרמל", 9, true, "18:33", "v_rail_2", "Platform 1"),
                LineArrival("line_rail_jer", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Jerusalem Yitzhak Navon", "ירושלים יצחק נבון", 14, true, "18:38", "v_rail_3", "Platform 3")
            )
        ),
        TransitStop(
            id = "stop_21440",
            code = "21440",
            nameEn = "Dizengoff Center / King George",
            nameHe = "דיזנגוף סנטר / המלך ג׳ורג׳",
            cityEn = "Tel Aviv-Yafo",
            cityHe = "תל אביב-יפו",
            location = LatLng(32.0753, 34.7751),
            lines = listOf("5", "63", "24", "129"),
            upcomingArrivals = listOf(
                LineArrival("line_5", "5", TransitOperator.DAN, TransitMode.BUS, "Tel Aviv Savidor", "רכבת סבידור מרכז", 2, true, "18:26", "v_5_1"),
                LineArrival("line_63", "63", TransitOperator.DAN, TransitMode.BUS, "Carmelit Terminal", "מסוף כרמלית", 7, true, "18:31", "v_63_2"),
                LineArrival("line_24", "24", TransitOperator.METROPOLINE, TransitMode.BUS, "Ramat HaSharon", "רמת השרון", 12, false, "18:36")
            )
        ),
        TransitStop(
            id = "stop_21012",
            code = "21012",
            nameEn = "Habima Square / Rothschild",
            nameHe = "כיכר הבימה / רוטשילד",
            cityEn = "Tel Aviv-Yafo",
            cityHe = "תל אביב-יפו",
            location = LatLng(32.0725, 34.7791),
            lines = listOf("5", "18", "142"),
            upcomingArrivals = listOf(
                LineArrival("line_5", "5", TransitOperator.DAN, TransitMode.BUS, "Tel Aviv Central", "ת. מרכזית תל אביב", 4, true, "18:28", "v_5_2"),
                LineArrival("line_18", "18", TransitOperator.DAN, TransitMode.BUS, "Bat Yam", "בת ים", 8, true, "18:32", "v_18_1")
            )
        ),
        TransitStop(
            id = "stop_36102",
            code = "36102",
            nameEn = "Herzl / Rothschild",
            nameHe = "הרצל / רוטשילד",
            cityEn = "Rishon LeZion",
            cityHe = "ראשון לציון",
            location = LatLng(31.9646, 34.8041),
            lines = listOf("274", "164", "83", "11"),
            upcomingArrivals = listOf(
                LineArrival("line_274", "274", TransitOperator.EGGED, TransitMode.BUS, "Tel Aviv University", "אוניברסיטת תל אביב", 3, true, "18:27", "v_274_2"),
                LineArrival("line_164", "164", TransitOperator.EGGED, TransitMode.BUS, "Petah Tikva", "פתח תקווה", 7, true, "18:31", "v_164_1"),
                LineArrival("line_83", "83", TransitOperator.EGGED, TransitMode.BUS, "Tel Aviv Carmelit", "כרמלית תל אביב", 13, false, "18:37")
            )
        ),
        TransitStop(
            id = "stop_36218",
            code = "36218",
            nameEn = "Rishon LeZion HaRishonim",
            nameHe = "תחנת רכבת הראשונים",
            cityEn = "Rishon LeZion",
            cityHe = "ראשון לציון",
            location = LatLng(31.9491, 34.8035),
            lines = listOf("IC", "274", "19"),
            upcomingArrivals = listOf(
                LineArrival("line_rail_ta_rlz", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Tel Aviv Savidor", "תל אביב סבידור", 6, true, "18:30", "v_rail_4", "Platform 1"),
                LineArrival("line_274", "274", TransitOperator.EGGED, TransitMode.BUS, "Rehovot", "רחובות", 9, true, "18:33")
            )
        ),
        TransitStop(
            id = "stop_37005",
            code = "37005",
            nameEn = "Weizmann / HaBanim",
            nameHe = "ויצמן / הבנים",
            cityEn = "Nes Ziona",
            cityHe = "נס ציונה",
            location = LatLng(31.9296, 34.7989),
            lines = listOf("274", "164", "201"),
            upcomingArrivals = listOf(
                LineArrival("line_274", "274", TransitOperator.EGGED, TransitMode.BUS, "Tel Aviv Azrieli", "תל אביב עזריאלי", 5, true, "18:29"),
                LineArrival("line_201", "201", TransitOperator.EGGED, TransitMode.BUS, "Rehovot Science Park", "פארק המדע רחובות", 10, false, "18:34")
            )
        ),
        TransitStop(
            id = "stop_23014",
            code = "23014",
            nameEn = "Or Yehuda Terminal / HaHagana",
            nameHe = "מסוף אור יהודה / ההגנה",
            cityEn = "Or Yehuda",
            cityHe = "אור יהודה",
            location = LatLng(32.0294, 34.8565),
            lines = listOf("36", "59", "475"),
            upcomingArrivals = listOf(
                LineArrival("line_36", "36", TransitOperator.KAVIM, TransitMode.BUS, "Tel Aviv Central", "ת. מרכזית תל אביב", 4, true, "18:28"),
                LineArrival("line_59", "59", TransitOperator.KAVIM, TransitMode.BUS, "Sheba Tel HaShomer", "שיבא תל השומר", 8, true, "18:32")
            )
        ),
        TransitStop(
            id = "stop_10112",
            code = "10112",
            nameEn = "Jerusalem Yitzhak Navon / Central",
            nameHe = "ירושלים יצחק נבון / מרכזית",
            cityEn = "Jerusalem",
            cityHe = "ירושלים",
            location = LatLng(31.7889, 35.2031),
            lines = listOf("IC", "L1", "480"),
            upcomingArrivals = listOf(
                LineArrival("line_kfir_1", "L1", TransitOperator.KFIR, TransitMode.LIGHT_RAIL, "Mount Herzl", "הר הרצל", 2, true, "18:26", "v_kfir_1"),
                LineArrival("line_rail_jer", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Tel Aviv HaHagana", "תל אביב ההגנה", 12, true, "18:36")
            )
        ),
        TransitStop(
            id = "stop_41008",
            code = "41008",
            nameEn = "Haifa Merkaz HaShmona",
            nameHe = "חיפה מרכז השמונה",
            cityEn = "Haifa",
            cityHe = "חיפה",
            location = LatLng(32.8206, 34.9967),
            lines = listOf("IC", "1", "115"),
            upcomingArrivals = listOf(
                LineArrival("line_rail_haifa", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, "Tel Aviv HaShalom", "תל אביב השלום", 7, true, "18:31"),
                LineArrival("line_115", "115", TransitOperator.EGGED, TransitMode.BUS, "Technion", "הטכניון", 11, false, "18:35")
            )
        )
    )

    val lines: List<TransitLine> by lazy {
        val azrieli = stops.first { it.code == "20145" }
        val habima = stops.first { it.code == "21012" }
        val dizengoff = stops.first { it.code == "21440" }
        val rlzHerzl = stops.first { it.code == "36102" }
        val rlzRail = stops.first { it.code == "36218" }
        val nesZiona = stops.first { it.code == "37005" }
        val haShalom = stops.first { it.code == "25501" }
        val orYehuda = stops.first { it.code == "23014" }
        val jerusalem = stops.first { it.code == "10112" }
        val haifa = stops.first { it.code == "41008" }

        listOf(
            TransitLine(
                id = "line_274",
                number = "274",
                operator = TransitOperator.EGGED,
                mode = TransitMode.BUS,
                originEn = "Tel Aviv University",
                originHe = "אוניברסיטת תל אביב",
                destinationEn = "Rehovot Central",
                destinationHe = "רחובות ת. מרכזית",
                outboundStops = listOf(
                    LineStopEntry(dizengoff, 1, 0, true),
                    LineStopEntry(habima, 2, 4, true),
                    LineStopEntry(azrieli, 3, 9, true, isBusApproaching = true),
                    LineStopEntry(rlzHerzl, 4, 24, true),
                    LineStopEntry(rlzRail, 5, 29, false),
                    LineStopEntry(nesZiona, 6, 35, false)
                ),
                returnStops = listOf(
                    LineStopEntry(nesZiona, 1, 0, true),
                    LineStopEntry(rlzRail, 2, 6, true),
                    LineStopEntry(rlzHerzl, 3, 11, true, isBusApproaching = true),
                    LineStopEntry(azrieli, 4, 26, true),
                    LineStopEntry(habima, 5, 31, false),
                    LineStopEntry(dizengoff, 6, 35, false)
                ),
                outboundPolyline = listOf(dizengoff.location, habima.location, azrieli.location, rlzHerzl.location, rlzRail.location, nesZiona.location),
                returnPolyline = listOf(nesZiona.location, rlzRail.location, rlzHerzl.location, azrieli.location, habima.location, dizengoff.location),
                scheduleTimesOutbound = listOf("06:00", "06:15", "06:30", "07:00", "07:30", "08:00", "09:00", "10:00", "12:00", "14:00", "16:00", "17:00", "18:00", "18:27", "18:45", "19:15", "20:00", "21:00", "22:00"),
                scheduleTimesReturn = listOf("05:45", "06:15", "06:45", "07:15", "08:00", "09:00", "11:00", "13:00", "15:00", "17:00", "18:29", "19:00", "20:00", "21:30"),
                hasActiveAlert = true
            ),
            TransitLine(
                id = "line_63",
                number = "63",
                operator = TransitOperator.DAN,
                mode = TransitMode.BUS,
                originEn = "Carmelit Terminal",
                originHe = "מסוף כרמלית",
                destinationEn = "Aluf Sade Terminal",
                destinationHe = "מסוף אלוף שדה",
                outboundStops = listOf(
                    LineStopEntry(dizengoff, 1, 2, true, isBusApproaching = true),
                    LineStopEntry(habima, 2, 5, true),
                    LineStopEntry(azrieli, 3, 10, true)
                ),
                returnStops = listOf(
                    LineStopEntry(azrieli, 1, 3, true),
                    LineStopEntry(habima, 2, 7, true),
                    LineStopEntry(dizengoff, 3, 12, true)
                ),
                outboundPolyline = listOf(dizengoff.location, habima.location, azrieli.location),
                returnPolyline = listOf(azrieli.location, habima.location, dizengoff.location),
                scheduleTimesOutbound = listOf("05:30", "06:00", "06:20", "06:40", "07:00", "07:20", "08:00", "10:00", "12:00", "14:00", "16:00", "18:29", "18:50", "19:10", "20:00", "21:30", "23:00"),
                scheduleTimesReturn = listOf("05:45", "06:15", "06:45", "07:15", "08:00", "10:00", "12:00", "14:00", "16:00", "18:31", "19:00", "20:30", "22:00"),
                hasActiveAlert = false
            ),
            TransitLine(
                id = "line_r1",
                number = "R1",
                operator = TransitOperator.DANKAL,
                mode = TransitMode.LIGHT_RAIL,
                originEn = "Petah Tikva Central",
                originHe = "פתח תקווה תחנה מרכזית",
                destinationEn = "Bat Yam HaKomemiyut",
                destinationHe = "בת ים הקוממיות",
                outboundStops = listOf(
                    LineStopEntry(azrieli, 1, 6, true, isBusApproaching = true),
                    LineStopEntry(habima, 2, 11, true)
                ),
                returnStops = listOf(
                    LineStopEntry(habima, 1, 4, true),
                    LineStopEntry(azrieli, 2, 9, true)
                ),
                outboundPolyline = listOf(azrieli.location, habima.location),
                returnPolyline = listOf(habima.location, azrieli.location),
                scheduleTimesOutbound = listOf("05:30", "05:36", "05:42", "06:00", "07:00", "08:00", "12:00", "16:00", "18:30", "18:36", "18:42", "20:00", "22:00", "23:30"),
                scheduleTimesReturn = listOf("05:35", "05:41", "06:00", "08:00", "12:00", "16:00", "18:28", "18:34", "20:00", "22:30"),
                hasActiveAlert = false
            ),
            TransitLine(
                id = "line_rail_ta_rlz",
                number = "IC",
                operator = TransitOperator.ISRAEL_RAILWAYS,
                mode = TransitMode.RAIL,
                originEn = "Tel Aviv HaShalom",
                originHe = "תל אביב השלום",
                destinationEn = "Rishon LeZion HaRishonim",
                destinationHe = "ראשון לציון הראשונים",
                outboundStops = listOf(
                    LineStopEntry(haShalom, 1, 4, true, isBusApproaching = true),
                    LineStopEntry(rlzRail, 2, 22, true)
                ),
                returnStops = listOf(
                    LineStopEntry(rlzRail, 1, 6, true),
                    LineStopEntry(haShalom, 2, 24, true)
                ),
                outboundPolyline = listOf(haShalom.location, rlzRail.location),
                returnPolyline = listOf(rlzRail.location, haShalom.location),
                scheduleTimesOutbound = listOf("06:12", "07:12", "08:12", "09:12", "12:12", "15:12", "17:12", "18:28", "19:28", "20:28", "21:28"),
                scheduleTimesReturn = listOf("06:30", "07:30", "08:30", "12:30", "15:30", "18:30", "19:30", "20:30"),
                hasActiveAlert = true
            ),
            TransitLine(
                id = "line_164",
                number = "164",
                operator = TransitOperator.EGGED,
                mode = TransitMode.BUS,
                originEn = "Petah Tikva",
                originHe = "פתח תקווה",
                destinationEn = "Rehovot",
                destinationHe = "רחובות",
                outboundStops = listOf(
                    LineStopEntry(azrieli, 1, 11, false),
                    LineStopEntry(rlzHerzl, 2, 27, false),
                    LineStopEntry(nesZiona, 3, 36, false)
                ),
                returnStops = listOf(
                    LineStopEntry(nesZiona, 1, 5, true),
                    LineStopEntry(rlzHerzl, 2, 14, true),
                    LineStopEntry(azrieli, 3, 30, false)
                ),
                outboundPolyline = listOf(azrieli.location, rlzHerzl.location, nesZiona.location),
                returnPolyline = listOf(nesZiona.location, rlzHerzl.location, azrieli.location),
                scheduleTimesOutbound = listOf("06:00", "06:30", "07:00", "08:00", "10:00", "14:00", "18:35", "19:05", "20:30"),
                scheduleTimesReturn = listOf("06:15", "07:15", "09:15", "13:15", "17:15", "18:31", "19:30"),
                hasActiveAlert = false
            ),
            TransitLine(
                id = "line_36",
                number = "36",
                operator = TransitOperator.KAVIM,
                mode = TransitMode.BUS,
                originEn = "Or Yehuda Terminal",
                originHe = "מסוף אור יהודה",
                destinationEn = "Tel Aviv Central",
                destinationHe = "ת. מרכזית תל אביב",
                outboundStops = listOf(
                    LineStopEntry(orYehuda, 1, 4, true, isBusApproaching = true),
                    LineStopEntry(azrieli, 2, 22, true)
                ),
                returnStops = listOf(
                    LineStopEntry(azrieli, 1, 8, true),
                    LineStopEntry(orYehuda, 2, 26, true)
                ),
                outboundPolyline = listOf(orYehuda.location, azrieli.location),
                returnPolyline = listOf(azrieli.location, orYehuda.location),
                scheduleTimesOutbound = listOf("06:00", "06:20", "07:00", "08:00", "12:00", "16:00", "18:28", "19:00", "21:00"),
                scheduleTimesReturn = listOf("06:30", "07:30", "12:30", "16:30", "18:32", "19:30", "21:30"),
                hasActiveAlert = false
            ),
            TransitLine(
                id = "line_kfir_1",
                number = "L1",
                operator = TransitOperator.KFIR,
                mode = TransitMode.LIGHT_RAIL,
                originEn = "Neve Yaakov",
                originHe = "נווה יעקב",
                destinationEn = "Mount Herzl",
                destinationHe = "הר הרצל",
                outboundStops = listOf(LineStopEntry(jerusalem, 1, 2, true, isBusApproaching = true)),
                returnStops = listOf(LineStopEntry(jerusalem, 1, 4, true)),
                outboundPolyline = listOf(jerusalem.location),
                returnPolyline = listOf(jerusalem.location),
                scheduleTimesOutbound = listOf("05:30", "06:00", "08:00", "12:00", "18:26", "18:32", "20:00", "23:00"),
                scheduleTimesReturn = listOf("05:40", "06:10", "08:10", "12:10", "18:28", "20:10"),
                hasActiveAlert = false
            ),
            TransitLine(
                id = "line_rail_haifa",
                number = "IC",
                operator = TransitOperator.ISRAEL_RAILWAYS,
                mode = TransitMode.RAIL,
                originEn = "Haifa Merkaz HaShmona",
                originHe = "חיפה מרכז השמונה",
                destinationEn = "Tel Aviv HaShalom",
                destinationHe = "תל אביב השלום",
                outboundStops = listOf(
                    LineStopEntry(haifa, 1, 7, true),
                    LineStopEntry(haShalom, 2, 62, true)
                ),
                returnStops = listOf(
                    LineStopEntry(haShalom, 1, 9, true),
                    LineStopEntry(haifa, 2, 64, true)
                ),
                outboundPolyline = listOf(haifa.location, haShalom.location),
                returnPolyline = listOf(haShalom.location, haifa.location),
                scheduleTimesOutbound = listOf("06:00", "07:00", "08:00", "12:00", "18:31", "19:31", "21:31"),
                scheduleTimesReturn = listOf("06:33", "08:33", "12:33", "18:33", "19:33", "21:33"),
                hasActiveAlert = false
            )
        )
    }

    val liveVehicles: List<LiveVehicle> = listOf(
        LiveVehicle("v_274_1", "line_274", "274", TransitOperator.EGGED, TransitMode.BUS, LatLng(32.0761, 34.7892), 165f, 34, "20145"),
        LiveVehicle("v_63_1", "line_63", "63", TransitOperator.DAN, TransitMode.BUS, LatLng(32.0748, 34.7820), 90f, 28, "20145"),
        LiveVehicle("v_r1_1", "line_r1", "R1", TransitOperator.DANKAL, TransitMode.LIGHT_RAIL, LatLng(32.0752, 34.7924), 210f, 45, "20145"),
        LiveVehicle("v_rail_1", "line_rail_ta_rlz", "IC", TransitOperator.ISRAEL_RAILWAYS, TransitMode.RAIL, LatLng(32.0785, 34.7950), 195f, 92, "25501"),
        LiveVehicle("v_5_1", "line_5", "5", TransitOperator.DAN, TransitMode.BUS, LatLng(32.0739, 34.7768), 35f, 22, "21440"),
        LiveVehicle("v_274_2", "line_274", "274", TransitOperator.EGGED, TransitMode.BUS, LatLng(31.9610, 34.8039), 15f, 38, "36102"),
        LiveVehicle("v_36_1", "line_36", "36", TransitOperator.KAVIM, TransitMode.BUS, LatLng(32.0320, 34.8512), 285f, 40, "23014")
    )

    val landmarks: List<PlaceResult> = listOf(
        PlaceResult(
            id = "place_azrieli",
            nameEn = "Azrieli Center",
            nameHe = "מרכז עזריאלי",
            subtitleEn = "Derech Menachem Begin 132, Tel Aviv",
            subtitleHe = "דרך מנחם בגין 132, תל אביב",
            location = LatLng(32.0741, 34.7922),
            category = PlaceCategory.MALL
        ),
        PlaceResult(
            id = "place_dizengoff",
            nameEn = "Dizengoff Center",
            nameHe = "דיזנגוף סנטר",
            subtitleEn = "Dizengoff St 50, Tel Aviv",
            subtitleHe = "דיזנגוף 50, תל אביב",
            location = LatLng(32.0755, 34.7749),
            category = PlaceCategory.MALL
        ),
        PlaceResult(
            id = "place_hayarkon",
            nameEn = "HaYarkon Park",
            nameHe = "פארק הירקון",
            subtitleEn = "Rokach Blvd, Tel Aviv",
            subtitleHe = "שדרות רוקח, תל אביב",
            location = LatLng(32.1003, 34.8066),
            category = PlaceCategory.PARK
        ),
        PlaceResult(
            id = "place_sarona",
            nameEn = "Sarona Market",
            nameHe = "שרונה מרקט",
            subtitleEn = "Kalman Magen 3, Tel Aviv",
            subtitleHe = "קלמן מגן 3, תל אביב",
            location = LatLng(32.0717, 34.7870),
            category = PlaceCategory.LANDMARK
        ),
        PlaceResult(
            id = "place_rlz_center",
            nameEn = "Rishon LeZion Rothschild Mall",
            nameHe = "קניון רוטשילד ראשון לציון",
            subtitleEn = "Rothschild St 45, Rishon LeZion",
            subtitleHe = "רוטשילד 45, ראשון לציון",
            location = LatLng(31.9642, 34.8012),
            category = PlaceCategory.MALL
        ),
        PlaceResult(
            id = "place_weizmann_nz",
            nameEn = "Weizmann Institute / Science Park",
            nameHe = "מכון ויצמן / פארק המדע",
            subtitleEn = "Rehovot / Nes Ziona",
            subtitleHe = "רחובות / נס ציונה",
            location = LatLng(31.9082, 34.8091),
            category = PlaceCategory.LANDMARK
        )
    )

    val serviceAlerts: List<ServiceAlert> = listOf(
        ServiceAlert(
            id = "alert_ayalon_rail",
            titleEn = "Israel Railways: Minor delays on Ayalon Corridor",
            titleHe = "רכבת ישראל: עיכובים קלים בציר איילון",
            descriptionEn = "Due to infrastructure signaling maintenance near Tel Aviv HaShalom, trains toward Rishon LeZion and Ashkelon may experience 5–8 minute delays.",
            descriptionHe = "עקב עבודות איתות ותשתית באזור תחנת ת״א השלום, ייתכנו עיכובים של 5–8 דקות ברכבות לכיוון ראשון לציון ואשקלון.",
            severity = AlertSeverity.WARNING,
            affectedLineNumbers = listOf("IC"),
            affectedStopCodes = listOf("25501", "36218"),
            operator = TransitOperator.ISRAEL_RAILWAYS,
            updatedAt = "18:10"
        ),
        ServiceAlert(
            id = "alert_274_detour",
            titleEn = "Line 274: Temporary detour on Ibn Gabirol",
            titleHe = "קו 274: הסטת מסלול זמנית באבן גבירול",
            descriptionEn = "Light Rail construction works on Ibn Gabirol St. Line 274 operates via Menachem Begin toward Azrieli Mall.",
            descriptionHe = "עקב עבודות הרכבת הקלה באבן גבירול, קו 274 פועל דרך דרך מנחם בגין לכיוון עזריאלי.",
            severity = AlertSeverity.INFO,
            affectedLineNumbers = listOf("274"),
            affectedStopCodes = listOf("20145", "36102"),
            operator = TransitOperator.EGGED,
            updatedAt = "17:45"
        ),
        ServiceAlert(
            id = "alert_rlz_roadwork",
            titleEn = "Herzl St Rishon LeZion: Evening lane closure",
            titleHe = "רחוב הרצל ראשון לציון: חסימת נתיב בערב",
            descriptionEn = "Municipal roadworks between 21:00 and 05:00. Expect slight delays on lines 164 and 83.",
            descriptionHe = "עבודות תשתית עירוניות בין השעות 21:00 ל-05:00. ייתכנו עיכובים קלים בקווים 164 ו-83.",
            severity = AlertSeverity.INFO,
            affectedLineNumbers = listOf("164", "83"),
            affectedStopCodes = listOf("36102"),
            operator = TransitOperator.EGGED,
            updatedAt = "16:30"
        )
    )
}
