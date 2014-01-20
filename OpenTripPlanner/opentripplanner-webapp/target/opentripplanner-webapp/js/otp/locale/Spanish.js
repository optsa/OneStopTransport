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
//Ã¡:\xE1, Ã©:\xE9, Ã­:\xED Ã³:\xF3, Ãº:\xFA, Ã�:\xC1, Ã‰:\xC9, Ã�:\xCD, Ã“:\xD3, Ãš:\xDA, Ã±:\xF1, Ã‘:\xD1
otp.locale.Spanish = {

    config : 
    {
        metricsSystem : "international",
        rightClickMsg : "Haz click con el bot\xF3n derecho sobre el mapa para elegir los puntos de origen y destino.",
        attribution   : {
            title   : "License Attribution",
            content : "Disclaimer goes here"  // TODO localize
        }
    },

    contextMenu : 
    {
        fromHere         : "Salir desde aqu\xED",
        toHere           : "Llegar hasta aqu\xED",
        intermediateHere : "Add intermediate point",

        centerHere       : "Centrar mapa aqu\xED",
        zoomInHere       : "Acercar",
        zoomOutHere      : "Alejar",
        previous         : "\xDAltimo encuadre",
        next             : "Siguiente encuadre"
    },

    // TODO Localize Me
    bikeTriangle : 
    {
        safeName : "Bike friendly",
        safeSym  : "B",

        hillName : "Flat",
        hillSym  : "F",

        timeName : "Quick",
        timeSym  : "Q"
    },

    service : 
    {
        weekdays:  "d\xEDas de la semana",
        saturday:  "S\xE1bado",
        sunday:    "Domingo",
        schedule:  "Horario"
    },

    indicators : 
    {
        ok         : "OK",
        date       : "Fecha",
        loading    : "Cargando",
        searching  : "Buscando...",
        qEmptyText : "Direcci\xF3n, intersecci\xF3n,  punto de inter\xE9s o Identificador de Parada..."
    },

    buttons: 
    {
        reverse       : "Cambiar",
        reverseTip    : "<b>Cambiar origen-destino</b><br/>Planifica el viaje de vuelta intercambiando origen y destino, y ajustando la hora de salida.",
        reverseMiniTip: "Cambiar origen-destino",

        edit          : "Editar",
        editTip       : "<b>Editar el viaje</b><br/>Vuelve a la pantalla principal para cambiar aspectos del viaje.",

        clear         : "Borrar",
        clearTip      : "<b>Clear</b><br/>Borra el mapa y todas las herramientas activas.",

        fullScreen    : "Pantalla completa",
        fullScreenTip : "<b>Pantalla completa</b><br/>Mostrar - ocultar paneles",

        print         : "Imprimir",
        printTip      : "<b>Imprimir</b><br/>Imprimir este plan de viaje junto con las paradas.",

        link          : "Link",
        linkTip      : "<b>Link</b><br/>Muestra distintas url para este viaje.",

        feedback      : "Feedback",
        feedbackTip   : "<b>Feedback</b><br/>Send your thoughts or experiences with the map",

        submit       : "Enviar",
        clearButton  : "Borrar",
        ok           : "OK",
        cancel       : "Cancelar",
        yes          : "S\xED",
        no           : "No",
        showDetails  : "Mostrar detalles...",
        hideDetails  : "Ocultar detalles..."
    },

    // note: keep these lower case (and uppercase via template / code if needed)
    directions : 
    {
        southeast:      "sureste",
        southwest:      "sudoeste",
        northeast:      "nordeste",
        northwest:      "noroeste",
        north:          "norte",
        west:           "oeste",
        south:          "sur",
        east:           "este",
        bound:          "l\xEDmite",
        left:           "gira a la izquierda",
        right:          "gira a la derecha",
        slightly_left:  "gira ligeramente a la izquierda",
        slightly_right: "gira ligeramente a la derecha",
        hard_left:      "gira completamente a la izquierda",
        hard_right:     "gira completamente a la derecha",
        'continue':     "sigue recto por", 
        to_continue:    "para continuar en", 
        becomes:        "se hace", 
        at:             "a",
        on:             "en",
        to:             "hasta",
        via:            "via",
        circle_counterclockwise: "take roundabout counterclockwise",
        circle_clockwise:        "take roundabout clockwise"
    },

    // see otp.planner.Templates for use
    instructions : 
    {
        walk         : "Andar",
        walk_toward  : "Anda hacia el",
        walk_verb    : "Andando",
        bike         : "Bicicleta",
        bike_toward  : "Pedalea hacia el",
        bike_verb    : "En bicicleta",
        drive        : "Coche",
        drive_toward : "Avanza hacia el",
        drive_verb   : "Coche",
        move         : "Avanza",
        move_toward  : "Avanza hacia el",

        transfer     : "transbordo",
        transfers    : "transbordos",

        continue_as  : "Continues as",
        stay_aboard  : "stay on board",

        depart       : "Salida desde",
        arrive       : "Llegada a",

        start_at     : "Origen:",
        end_at       : "Destino:"
    },

    // see otp.planner.Templates for use
    labels : 
    {
        agency_msg   : "Service run by", // TODO
        agency_msg_tt: "Open agency website in separate window...", // TODO
        about        : "Alrededor de ",
        stop_id      : "Stop ID",
        trip_details : "Detalles del viaje",
        fare         : "Tarifa",
        fare_symbol  : "\u20ac",

        // TODO  -- used in the Trip Details summary to describe different fares 
        regular_fare : "",
        student_fare : "",
        senior_fare  : "",

        travel       : "Hora de salida",
        valid        : "Hora actual",
        trip_length  : "Tiempo",
        with_a_walk  : "with a walk of",
        alert_for_rt : "Alert for route"
    },

    // see otp.planner.Templates for use -- one output are the itinerary leg headers
    modes : 
    {
        WALK:           "A PIE",
        BICYCLE:        "BICICLETA",
        CAR:            "COCHE",
        TRAM:           "TRANV\xCDA",
        SUBWAY:         "METRO",
        RAIL:           "TREN",
        BUS:            "AUTOB\xDAS",
        FERRY:          "BOTE",
        CABLE_CAR:      "PUENTE COLGANTE",
        GONDOLA:        "GONDOLA",
        FUNICULAR:      "FUNICULAR"
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

        minute         : "minuto",
        minutes        : "minutos",
        minute_abbrev  : "min",
        minutes_abbrev : "mins",
        second_abbrev  : "sec",
        seconds_abbrev : "secs",
        format         : "D, j M H:i",
        date_format    : "d-m-Y",
        time_format    : "H:i",
        months         : ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
    },

    systemmap :
    {
        labels :
        {
            panelTitle : "System Map"
        }
    },

    tripPlanner :
    {
        // see otp/planner/*.js for where these values are used
        labels : 
        {
            panelTitle    : "Planificador multimodal",
            tabTitle      : "Planificar un viaje",
            inputTitle    : "Detalles del viaje",
            optTitle      : "Preferencias (opcional)",
            submitMsg     : "Planificando su viaje...",
            optionalTitle : "",
            date          : "Fecha",
            time          : "Hora",
            when          : "Tiempo",
            from          : "Desde",
            fromHere      : "Desde aqu\xED",
            to            : "Hasta",
            toHere        : "Hasta aqu\xED",
            intermediate  : "Intermediate Place",          // TODO
            minimize      : "Mostrar el",
            maxWalkDistance: "M\xE1xima distancia hasta la parada",
            walkSpeed     : "velocidad de caminar",
            maxBikeDistance: "M\xE1xima distancia hasta la bicicletas",               // TODO
            walkSpeed     : "velocidad de bicicleta",
            arriveDepart  : "Llegada/Salida a",
            mode          : "Modo de viaje",
            wheelchair    : "Viaje con accesibilidad",
            go            : "Empezar",
            planTrip      : "Planificar el viaje",
            newTrip       : "Nuevo viaje"
        },

        // see otp/config.js for where these values are used
        link : 
        {
            text           : "Link a este viaje (OTP)",
            trip_separator : "Este viaje en otros planificadores intermodales",
            bike_separator : "En otros planificadores de bicicletas",
            walk_separator : "En otros planificadores pedestres",
//TODO
            google_transit : "Google Transit",
            google_bikes   : "Google Bike Directions",
            google_walk    : "Google Walking Directions",
            google_domain  : "http://www.google.es"
        },

        // see otp.planner.Forms for use
        geocoder:
        {
//TODO
            working      : "Looking up address ....",
            error        : "Did not receive any results",
            msg_title    : "Donde es review trip plan",
            msg_content  : "Donde es correct errors before planning your trip",
            select_result_title : "Please select a result",
            address_header : "Address"
        },

        error:
        {
            title        : 'Error del planificador',
            deadMsg      : "El planificador no responde. Por favor, int\xE9ntelo m\xE1s tarde",
            geoFromMsg   : "Por favor, elija la direcci\xF3n de salida del viaje: ",
            geoToMsg     : "Por favor, elija la direcci\xF3n de llegada del viaje: "
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
            409: "Too close",
            340: "Geocode from ambiguous",
            350: "Geocode to ambiguous",
            360: "Geocode from and to ambiguous"
        },

        options: 
        [
          ['TRANSFERS', 'M\xEDnimo n\xFAmero de transbordos'],
          ['QUICK',     'Viaje m\xE1s corto'],
          ['SAFE',      'Viaje m\xE1s seguro'],
          ['TRIANGLE',  'Custom trip...'] // TODO localize
        ],
    
        arriveDepart: 
        [
          ['false', 'Salida'], 
          ['true',  'Llegada']
        ],

        walkSpeed :
        [
            ['0.278',  '1 km/h'],
            ['0.556',  '2 km/h'],
            ['0.833',  '3 km/h'],
            ['1.111',  '4 km/h'],
            ['1.389',  '5 km/h'],
            ['1.667',  '6 km/h'],
            ['1.944',  '7 km/h'],
            ['2.222',  '8 km/h'],
            ['2.500',  '9 km/h'],
            ['2.778',  '10 km/h']
        ],

        maxWalkDistance : 
        [
            ['500',   '500 metros'],
            ['1000',   '1 km'],
            ['5000',   '5 km'],
            ['10000',  '10 km'],
            ['20000',  '20 km']
        ],
    
        mode : 
        [
            ['TRANSIT,WALK', 'Transporte p\xFAblico'],
            ['BUSISH,TRAINISH,WALK', 'Bus y tren'],
            ['BUSISH,WALK', 'S\xF3lo bus'],
            ['TRAINISH,WALK', 'S\xF3lo tren'],
            ['WALK', 'S\xF3lo a pie'],
            ['BICYCLE', 'Bicicleta'],
            ['TRANSIT,BICYCLE', 'Transporte p\xFAblico y bicicleta'],
            ['CAR', 'Coche']
        ],

        wheelchair :
        [
            ['false', 'No se requiere un viaje con accesibilidad'],
            ['true', 'S\xED se requiere un viaje con accesibilidad']
        ]
    },

    CLASS_NAME : "otp.locale.Spanish"
};
