/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.locale");

/**
  * @class
  */

otp.locale.Turkish = {

    config :
    {
        metricsSystem : "international",
        rightClickMsg : "Yolculuğun başlangıç ​​ve bitiş belirlemek için harita üzerine sağ tıklayın.",
        attribution   : {
            title   : "License Attribution",
            content : "Yasal uyarı buraya eklenecek"
        }
    },

    contextMenu : 
    {
        fromHere         : "Yolculuğu buradan başlat",
        toHere           : "Yolculuğu burada bitir",
        intermediateHere : "Ara nokta ekle",

        centerHere       : "Haritayı buraya ortala",
        zoomInHere       : "Yaklaştır",
        zoomOutHere      : "Uzaklaştır",
        previous         : "Bir önceki pozisyon",
        next             : "Sonraki pozisyon"
    },

  
    bikeTriangle : 
    {
        safeName : "En Güvenli",
        safeSym  : "G",

        hillName : "En Düz",
        hillSym  : "D",

        timeName : "En Hızlı",
        timeSym  : "H"
    },

    service : 
    {
        weekdays:  "Haftaiçi",
        saturday:  "Cumartesi",
        sunday:    "Pazar",
        schedule:  "Zamanla"
    },

    indicators : 
    {
        ok         : "Tamam",
        date       : "Tarih",
        loading    : "Yükleniyor",
        searching  : "Aranıyor...",
        qEmptyText : "Adres, kavşak, dönemeç..."
    },
	// TODO localize
    buttons: 
    {
        reverse       : "Reverse",
        reverseTip    : "<b>Reverse directions</b><br/>Plan a return trip by reversing this trip's start and end points, and adjusting the time forward.",
        reverseMiniTip: "Reverse directions",

        edit          : "Edit",
        editTip       : "<b>Edit trip</b><br/>Return to the main trip planner input form with the details of this trip.",

        clear         : "Clear",
        clearTip      : "<b>Clear</b><br/>Clear the map and all active tools.",

        fullScreen    : "Full Screen",
        fullScreenTip : "<b>Full Screen</b><br/>Show -or- hide tool panels",

        print         : "Print",
        printTip      : "<b>Print</b><br/>Print friendly version of the trip plan (without map).",

        link          : "Link",
        linkTip      : "<b>Link</b><br/>Show link url for this trip plan.",

        feedback      : "Feedback",
        feedbackTip   : "<b>Feedback</b><br/>Send your thoughts or experiences with the map",

        submit       : "Submit",
        clearButton  : "Clear",
        ok           : "OK",
        cancel       : "Cancel",
        yes          : "Yes",
        no           : "No",
        showDetails  : "Show details...",
        hideDetails  : "Hide details..."
    },

    // note: keep these lower case (and uppercase via template / code if needed)
    directions : 
    {
        southeast:      "southeast",
        southwest:      "southwest",
        northeast:      "northeast",
        northwest:      "northwest",
        north:          "north",
        west:           "west",
        south:          "south",
        east:           "east",
        bound:          "bound",
        left:           "left",
        right:          "right",
        slightly_left:  "slight left",
        slightly_right: "slight right",
        hard_left:      "hard left",
        hard_right:     "hard right",
        'continue':     "continue on",
        to_continue:    "to continue on",
        becomes:        "becomes",
        at:             "at",
        on:             "on",
        to:             "to",
        via:            "via",
        circle_counterclockwise: "take roundabout counterclockwise",
        circle_clockwise:        "take roundabout clockwise",
        // rather than just being a direction, this should be
        // full-fledged to take just the exit name at the end
        elevator: "take elevator to"
    },

    // see otp.planner.Templates for use
    instructions :
    {
        walk         : "Walk",
        walk_toward  : "Walk",
        walk_verb    : "Walk",
        bike         : "Bike",
        bike_toward  : "Bike",
        bike_verb    : "Bike",
        drive        : "Drive",
        drive_toward : "Drive",
        drive_verb   : "Drive",
        move         : "Proceed",
        move_toward  : "Proceed",

        transfer     : "transfer",
        transfers    : "transfers",

        continue_as  : "Continues as",
        stay_aboard  : "stay on board",

        depart       : "Depart",
        arrive       : "Arrive",

        start_at     : "Start at",
        end_at       : "End at"
    },

    // see otp.planner.Templates for use
    labels : 
    {
        agency_msg   : "Service run by", // TODO
        agency_msg_tt: "Open agency website in separate window...", // TODO
        about        : "Hakkında",
        stop_id      : "Stop ID",
        trip_details : "Yolculuk detayları",
        fare         : "Ücret",
        fare_symbol  : "$",

        // TODO  -- used in the Trip Details summary to describe different fares 
        regular_fare   : "",
        student_fare : "",
        senior_fare  : "",

        travel       : "Seyehat",
        valid        : "Geçerli",
        trip_length  : "Zaman",
        with_a_walk  : "yürüyüş ile",
        alert_for_rt : "Güzergah için uyar"
    },

    // see otp.planner.Templates for use -- one output are the itinerary leg headers
    modes :
    {
        WALK:           "Yürüyüş",
        BICYCLE:        "Bisiklet",
        CAR:            "Araba",
        TRAM:           "Tramway",
        SUBWAY:         "Metro",
        RAIL:           "Tren",
        BUS:            "Otobüs",
        FERRY:          "Feribot",
        CABLE_CAR:      "Teleferik",
        GONDOLA:        "Gondol",
        FUNICULAR:      "Funiküler"
    },

    ordinal_exit:
    {
        1:  "to first exit",
        2:  "to second exit",
        3:  "to third exit",
        4:  "to fourth exit",
        5:  "to fifth exit",
        6:  "to sixth exit",
        7:  "to seventh exit",
        8:  "to eighth exit",
        9:  "to ninth exit",
        10: "to tenth exit"
    },

    time:
    {
        // TODO
        hour_abbrev    : "hour",
        hours_abbrev   : "hours",
        hour           : "hour",
        hours          : "hours",

        minute         : "dakika",
        minutes        : "dakika",
        minute_abbrev  : "dak",
        minutes_abbrev : "dak",
        second_abbrev  : "san",
        seconds_abbrev : "san",
        format         : "F jS, Y @ g:ia",
        date_format    : "d-m-Y",
        time_format    : "H:i",
        months         : ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara']
    },

    systemmap :
    {
        labels :
        {
            panelTitle : "Sistem Haritası"
        }
    },

    tripPlanner :
    {
        // see otp/planner/*.js for where these values are used
        labels : 
        {
            panelTitle    : "Trip Planner",
            tabTitle      : "Bir Yolculuk Planla",
            inputTitle    : "Yolculuk detayları",
            optTitle      : "Yolculuk ayarları (opsiyonel)",
            submitMsg     : "Yolculuğunuz planlanıyor...",
            optionalTitle : "",
            date          : "Tarih",
            time          : "Saat",
            when          : "When",
            from          : "From",
            fromHere      : "From here",
            to            : "To",
            toHere        : "To here",
            intermediate  : "Intermediate Place",
            minimize      : "Show me the",
            maxWalkDistance: "Maximum walk",
            walkSpeed     : "Walk speed",
            maxBikeDistance: "Maximum bike",
            bikeSpeed     : "Bike speed",
            arriveDepart  : "Arrive by/Depart at",
            mode          : "Travel by",
            wheelchair    : "Wheelchair accessible trip", 
            go            : "Go",
            planTrip      : "Plan Your Trip",
            newTrip       : "New trip"
        },

        // see otp/config.js for where these values are used
        link : 
        {
            text           : "Link to this trip (OTP)",
            trip_separator : "This trip on other transit planners",
            bike_separator : "On other bike trip planners",
            walk_separator : "On other walking direction planners",
            google_transit : "Google Transit",
            google_bikes   : "Google Bike Directions",
            google_walk    : "Google Walking Directions",
            google_domain  : "http://www.google.com"
        },

        // see otp.planner.Forms for use
        geocoder:
        {
            working      : "Looking up address ....",
            error        : "Did not receive any results",
            msg_title    : "Review trip plan",
            msg_content  : "Please correct errors before planning your trip",
            select_result_title : "Please select a result",
            address_header : "Address"
        },

        error:
        {
            title        : 'Trip Planner Hata',
            deadMsg      : "Map Trip Planner is currently not responding. Please wait a few minutes to try again, or try the text trip planner (see link below).",
            geoFromMsg   : "Please select the 'From' location for your trip: ",
            geoToMsg     : "Please select the 'To' location for your trip: "
        },
        
        // default messages from server if a message was not returned
        msgcodes:
        {
            200: "Plan OK",
            500: "Server error",
            400: "Trip out of bounds",
            404: "Path not found",
            406: "No transit times",
            408: "Request timed out",
            413: "Invalid parameter",
            440: "From geocode not found",
            450: "To geocode not found",
            460: "Geocode from and to not found",
            470: "From or to not wheelchair accessible",
            409: "Too close",
            340: "Geocode from ambiguous",
            350: "Geocode to ambiguous",
            360: "Geocode from and to ambiguous"
        },

        options: 
        [
          ['TRANSFERS', 'En az transfer'],
          ['QUICK',     'En hızlı yolculuk'],
          ['SAFE',      'En Güvenli yolculuk'],
          ['TRIANGLE',  'Özelleştirilmiş yolculuk...']
        ],
    
        arriveDepart: 
        [
          ['false', 'Depart'], 
          ['true',  'Arrive']
        ],
    
        maxWalkDistance : 
        [
            ['160',   '1/10 mil'],
            ['420',   '1/4 mil'],
            ['840',   '1/2 mil'],
            ['1260',  '3/4 mil'],
            ['1609',  '1 mil'],
            ['3219',  '2 mil'],
            ['4828',  '3 mil'],
            ['6437',  '4 mil'],
            ['8047',  '5 mil'],
            ['16093',  '10 mil'],
            ['24140',  '15 mil'],
            ['32187',  '20 mil'],
            ['48280',  '30 mil'],
            ['64374',  '40 mil'],
            ['80467',  '50 mil'],
            ['160934',  '100 mil']
        ],

        walkSpeed :
        [
            //it's not that people in Turkey are 3,600x as speedy
            //it's that hour in Turkish is "saat".  I bet that gets
            //confusing!
            ['0.278',  '1 km/s'],
            ['0.556',  '2 km/s'],
            ['0.833',  '3 km/s'],
            ['1.111',  '4 km/s'],
            ['1.389',  '5 km/s'],
            ['1.667',  '6 km/s'],
            ['1.944',  '7 km/s'],
            ['2.222',  '8 km/s'],
            ['2.500',  '9 km/s'],
            ['2.778',  '10 km/s']
        ],
    
        mode : 
        [
            ['TRANSIT,WALK', 'Transit'],
// DO WE REALLY NEED THIS?  ISN'T BUS & TRAIN the same as TRANSIT, WALK
//          ['BUSISH,TRAINISH,WALK', 'Bus & Train'],
            ['BUSISH,WALK', 'Bus only'],
            ['TRAINISH,WALK', 'Train only'],
            ['WALK', 'Walk only'],
            ['BICYCLE', 'Bicycle'],
            ['TRANSIT,BICYCLE', 'Transit & Bicycle']
        ],

        wheelchair :
        [
            ['false', 'Zorunlu Değil'],
            ['true', 'Zorunlu']
        ]
    },

    CLASS_NAME : "otp.locale.Turkish"
};
