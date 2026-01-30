package com.merkost.honq.data.local

import com.merkost.honq.data.local.db.QuestionDao
import com.merkost.honq.data.local.entity.QuestionEntity
import kotlinx.serialization.json.Json

class SampleDataSeeder(
    private val questionDao: QuestionDao,
    private val json: Json
) {
    suspend fun seedIfEmpty() {
        val count = questionDao.getQuestionCount()
        if (count == 0) {
            questionDao.insertAll(sampleQuestions)
        }
    }

    private val sampleQuestions = listOf(
        QuestionEntity(
            id = "1",
            code = "RR001",
            text = "When approaching a roundabout, you must:",
            options = """["Speed up to enter quickly","Give way to vehicles already in the roundabout","Always stop before entering","Sound your horn to warn other drivers"]""",
            correctIndex = 1,
            explanation = "You must give way to all vehicles already in the roundabout before entering.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "2",
            code = "SL001",
            text = "What is the maximum speed limit in a school zone during school hours?",
            options = """["50 km/h","40 km/h","60 km/h","30 km/h"]""",
            correctIndex = 1,
            explanation = "The speed limit in school zones is 40 km/h during school zone hours.",
            categoryId = "Speed Limits"
        ),
        QuestionEntity(
            id = "3",
            code = "RR002",
            text = "When can you use a mobile phone while driving?",
            options = """["When stopped at traffic lights","Never while the vehicle is moving or stationary but not parked","When driving under 40 km/h","When using hands-free only"]""",
            correctIndex = 1,
            explanation = "You cannot use a hand-held mobile phone while driving, even when stopped at traffic lights. The vehicle must be parked.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "4",
            code = "TS001",
            text = "What does a yellow traffic light mean?",
            options = """["Speed up to get through","Stop if it is safe to do so","Proceed with caution","Give way to pedestrians"]""",
            correctIndex = 1,
            explanation = "A yellow light means stop if it is safe to do so. Only proceed if stopping would be dangerous.",
            categoryId = "Traffic Signals"
        ),
        QuestionEntity(
            id = "5",
            code = "AD001",
            text = "What is the blood alcohol limit for learner and P1 drivers?",
            options = """["0.02","0.05","Zero (0.00)","0.01"]""",
            correctIndex = 2,
            explanation = "Learner and P1 provisional drivers must have a zero blood alcohol concentration.",
            categoryId = "Alcohol & Drugs"
        ),
        QuestionEntity(
            id = "6",
            code = "RR003",
            text = "When must you indicate before changing lanes?",
            options = """["Only in heavy traffic","At least 5 seconds before changing","Long enough to warn other road users","Only if other vehicles are nearby"]""",
            correctIndex = 2,
            explanation = "You must signal long enough to give sufficient warning to other road users before changing lanes.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "7",
            code = "RR004",
            text = "What should you do when an emergency vehicle approaches with flashing lights and sirens?",
            options = """["Speed up to get out of the way","Stop immediately wherever you are","Move left and stop if safe","Continue driving normally"]""",
            correctIndex = 2,
            explanation = "You must move to the left side of the road and stop if safe to allow emergency vehicles to pass.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "8",
            code = "SD001",
            text = "What is the minimum following distance in good conditions?",
            options = """["1 second","2 seconds","3 seconds","4 seconds"]""",
            correctIndex = 2,
            explanation = "You should maintain at least a 3-second gap from the vehicle in front in good conditions.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "9",
            code = "PK001",
            text = "When parking on a hill facing uphill with a kerb, you should:",
            options = """["Turn wheels towards the kerb","Turn wheels away from the kerb","Keep wheels straight","It doesn't matter"]""",
            correctIndex = 1,
            explanation = "When facing uphill with a kerb, turn your wheels away from the kerb so the car rolls into the kerb if brakes fail.",
            categoryId = "Parking"
        ),
        QuestionEntity(
            id = "10",
            code = "LC001",
            text = "A driver's license can be suspended if you accumulate how many demerit points?",
            options = """["10 points","13 points","15 points","It depends on your license type"]""",
            correctIndex = 3,
            explanation = "The demerit point threshold varies by license type: unrestricted is 13, P2 is 7, P1 is 4, and learners is 4 points.",
            categoryId = "Licensing"
        ),
        QuestionEntity(
            id = "11",
            code = "SD002",
            text = "What must you do before opening your car door?",
            options = """["Sound your horn","Check mirrors and blind spots for traffic and cyclists","Nothing special is required","Flash your lights"]""",
            correctIndex = 1,
            explanation = "Always check mirrors and look over your shoulder before opening doors to avoid hitting cyclists or pedestrians.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "12",
            code = "OT001",
            text = "When can you overtake another vehicle on the left?",
            options = """["Never","When the other vehicle is turning right","When driving on a multi-lane road and traffic is flowing","Both B and C"]""",
            correctIndex = 3,
            explanation = "You can overtake on the left when the vehicle is turning right, or on multi-lane roads in flowing traffic.",
            categoryId = "Overtaking"
        ),
        QuestionEntity(
            id = "13",
            code = "RM001",
            text = "What does a solid white line in the centre of the road mean?",
            options = """["You can cross it to overtake","You cannot cross it at any time","You can cross it to turn into a driveway","You can cross it in emergencies only"]""",
            correctIndex = 2,
            explanation = "A single solid white line means you cannot cross it to overtake, but you can cross to enter or leave a road.",
            categoryId = "Road Markings"
        ),
        QuestionEntity(
            id = "14",
            code = "PD001",
            text = "At a pedestrian crossing without traffic lights, you must:",
            options = """["Sound horn to warn pedestrians","Give way to pedestrians on or approaching the crossing","Pedestrians must wait for you","Speed up to clear the crossing quickly"]""",
            correctIndex = 1,
            explanation = "You must give way to any pedestrian on or approaching a pedestrian crossing.",
            categoryId = "Pedestrians"
        ),
        QuestionEntity(
            id = "15",
            code = "RF001",
            text = "What is the purpose of a safety ramp on a steep descent?",
            options = """["For trucks to park","For emergency stopping if brakes fail","For rest stops","For overtaking"]""",
            correctIndex = 1,
            explanation = "Safety ramps are designed for vehicles to use if their brakes fail on a steep descent.",
            categoryId = "Road Features"
        ),
        QuestionEntity(
            id = "16",
            code = "SD003",
            text = "How should you position your hands on the steering wheel?",
            options = """["12 and 6 o'clock","9 and 3 o'clock or 10 and 2 o'clock","One hand at 12 o'clock","Any comfortable position"]""",
            correctIndex = 1,
            explanation = "The recommended hand position is at 9 and 3 o'clock or 10 and 2 o'clock for optimal control.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "17",
            code = "HC001",
            text = "When driving in fog, you should:",
            options = """["Use high beam headlights","Use low beam headlights","Use parking lights only","Flash your lights continuously"]""",
            correctIndex = 1,
            explanation = "Use low beam headlights in fog. High beams reflect off fog and reduce visibility.",
            categoryId = "Hazardous Conditions"
        ),
        QuestionEntity(
            id = "18",
            code = "RM002",
            text = "What is the meaning of a double yellow line on the road?",
            options = """["No parking at any time","No stopping at any time","No overtaking","Give way"]""",
            correctIndex = 0,
            explanation = "Double yellow lines indicate no parking at any time.",
            categoryId = "Road Markings"
        ),
        QuestionEntity(
            id = "19",
            code = "ND001",
            text = "When must you dip your headlights?",
            options = """["Only in the city","When following or approaching another vehicle","Only in rain","Never on highways"]""",
            correctIndex = 1,
            explanation = "You must dip your headlights when approaching or following another vehicle to avoid dazzling the driver.",
            categoryId = "Night Driving"
        ),
        QuestionEntity(
            id = "20",
            code = "ES001",
            text = "What should you do if your vehicle starts to skid?",
            options = """["Brake hard immediately","Accelerate to regain control","Ease off the accelerator and steer in the direction you want to go","Turn the steering wheel in the opposite direction"]""",
            correctIndex = 2,
            explanation = "Ease off the accelerator and steer gently in the direction you want the front of the car to go.",
            categoryId = "Emergency Situations"
        ),
        QuestionEntity(
            id = "21",
            code = "RM003",
            text = "On a road with lane arrows, you must:",
            options = """["Ignore them if turning","Follow the direction of the arrows","Use any lane you prefer","They are only suggestions"]""",
            correctIndex = 1,
            explanation = "Lane arrows are mandatory and you must travel in the direction shown by the arrows.",
            categoryId = "Road Markings"
        ),
        QuestionEntity(
            id = "22",
            code = "RM004",
            text = "What does a broken white line in the centre of the road mean?",
            options = """["No overtaking allowed","You may cross it to overtake if safe","The road is one way","Lane ends ahead"]""",
            correctIndex = 1,
            explanation = "A broken white line means you may cross it to overtake if it is safe to do so.",
            categoryId = "Road Markings"
        ),
        QuestionEntity(
            id = "23",
            code = "AC001",
            text = "If you hit a parked car and cannot find the owner, you must:",
            options = """["Drive away, it's not your fault","Leave a note with your details","Wait indefinitely","Report it to police only if damage exceeds ${'$'}500"]""",
            correctIndex = 1,
            explanation = "You must leave your name and address on the vehicle or report to police as soon as possible.",
            categoryId = "Accidents"
        ),
        QuestionEntity(
            id = "24",
            code = "HC002",
            text = "What is the safest way to cross a flooded road?",
            options = """["Drive through quickly","Don't enter floodwater if you're unsure of the depth","Follow the car in front","Drive through slowly in first gear"]""",
            correctIndex = 1,
            explanation = "Never drive through floodwater if you cannot see the road surface or are unsure of the depth.",
            categoryId = "Hazardous Conditions"
        ),
        QuestionEntity(
            id = "25",
            code = "TW001",
            text = "When towing a trailer, your speed limit is:",
            options = """["Same as the posted limit","10 km/h below posted limit","100 km/h maximum unless lower limit posted","80 km/h maximum"]""",
            correctIndex = 2,
            explanation = "When towing, the maximum speed is 100 km/h unless a lower limit is posted.",
            categoryId = "Towing"
        ),
        QuestionEntity(
            id = "26",
            code = "RR005",
            text = "A U-turn is not permitted:",
            options = """["At traffic lights unless a sign allows it","On any one-way street","Where there is a 'No U-turn' sign","All of the above"]""",
            correctIndex = 3,
            explanation = "U-turns are prohibited at traffic lights (unless signed), on one-way streets, and where 'No U-turn' signs are posted.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "27",
            code = "RS001",
            text = "What does an orange diamond-shaped sign indicate?",
            options = """["Permanent road hazard","Temporary road works or hazard","Tourist attraction","Speed limit change"]""",
            correctIndex = 1,
            explanation = "Orange diamond signs indicate temporary conditions like road works or hazards.",
            categoryId = "Road Signs"
        ),
        QuestionEntity(
            id = "28",
            code = "PK002",
            text = "How far must you park from a fire hydrant?",
            options = """["1 metre","2 metres","3 metres","No specific distance"]""",
            correctIndex = 0,
            explanation = "You must not park within 1 metre of a fire hydrant or fire plug indicator.",
            categoryId = "Parking"
        ),
        QuestionEntity(
            id = "29",
            code = "SF001",
            text = "What is the penalty for not wearing a seatbelt?",
            options = """["Warning only","Fine and demerit points","Fine only","Licence suspension"]""",
            correctIndex = 1,
            explanation = "Not wearing a seatbelt results in a fine and demerit points for the driver and/or passenger.",
            categoryId = "Safety"
        ),
        QuestionEntity(
            id = "30",
            code = "RR006",
            text = "When can you drive in a bus lane?",
            options = """["Never","When turning left within 100m of your turn","During off-peak hours only","When no buses are present"]""",
            correctIndex = 1,
            explanation = "You may drive in a bus lane for up to 100 metres when turning left or entering/leaving a property.",
            categoryId = "Road Rules"
        ),
        QuestionEntity(
            id = "31",
            code = "SD004",
            text = "What should you check before reversing?",
            options = """["Mirrors only","Mirrors and look behind","Horn before moving","Nothing if you have sensors"]""",
            correctIndex = 1,
            explanation = "Always check mirrors AND look behind your vehicle before reversing to check for obstacles and pedestrians.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "32",
            code = "GW001",
            text = "At a T-intersection without signs, who gives way?",
            options = """["Vehicle on the continuing road","Vehicle on the terminating road","Whichever arrives first","Vehicle turning left"]""",
            correctIndex = 1,
            explanation = "At a T-intersection, vehicles on the terminating road must give way to all vehicles on the continuing road.",
            categoryId = "Give Way Rules"
        ),
        QuestionEntity(
            id = "33",
            code = "VR001",
            text = "How often must you have your vehicle inspected if it's over 5 years old?",
            options = """["Every 6 months","Annually","Every 2 years","Only when selling"]""",
            correctIndex = 1,
            explanation = "Vehicles over 5 years old require annual safety inspections (pink slip) for registration renewal.",
            categoryId = "Vehicle Requirements"
        ),
        QuestionEntity(
            id = "34",
            code = "SD005",
            text = "What is the safe cornering technique?",
            options = """["Accelerate through the corner","Brake in the corner","Slow before the corner, accelerate gently through","Maintain constant speed"]""",
            correctIndex = 2,
            explanation = "The safe technique is to slow down before entering the corner and accelerate gently through it.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "35",
            code = "SD006",
            text = "When stopping behind another vehicle, you should be able to see:",
            options = """["Their number plate","The bottom of their rear tyres","Their rear window","At least 3 metres gap"]""",
            correctIndex = 1,
            explanation = "You should stop where you can see the bottom of the rear tyres of the vehicle in front, ensuring adequate space.",
            categoryId = "Safe Driving"
        ),
        QuestionEntity(
            id = "36",
            code = "CY001",
            text = "A bicycle rider can legally:",
            options = """["Ride on any footpath","Ride through red lights if no traffic","Ride two abreast on roads","Ride without a helmet"]""",
            correctIndex = 2,
            explanation = "Bicycle riders can legally ride two abreast, but no more than 1.5 metres apart.",
            categoryId = "Cyclists"
        ),
        QuestionEntity(
            id = "37",
            code = "PK003",
            text = "When parking on a one-way street, you may park:",
            options = """["On the left side only","On either side","On the right side only","Parallel to traffic flow only"]""",
            correctIndex = 1,
            explanation = "On a one-way street, you may park on either side of the road.",
            categoryId = "Parking"
        ),
        QuestionEntity(
            id = "38",
            code = "FT001",
            text = "Fatigue is a factor in approximately what percentage of fatal crashes?",
            options = """["5%","10%","20%","30%"]""",
            correctIndex = 2,
            explanation = "Driver fatigue is a factor in approximately 20% of fatal road crashes.",
            categoryId = "Fatigue"
        ),
        QuestionEntity(
            id = "39",
            code = "MW001",
            text = "What should you do if you miss your exit on a motorway?",
            options = """["Reverse back to the exit","Continue to the next exit","Stop on the shoulder","Do a U-turn"]""",
            correctIndex = 1,
            explanation = "If you miss your exit, continue to the next exit. Never reverse, stop on the shoulder, or make U-turns on motorways.",
            categoryId = "Motorway Driving"
        ),
        QuestionEntity(
            id = "40",
            code = "HC003",
            text = "When driving in rain, you should:",
            options = """["Follow closer to see brake lights better","Increase your following distance","Maintain normal distance","Drive faster to reduce time in rain"]""",
            correctIndex = 1,
            explanation = "Increase your following distance in rain as stopping distances are longer on wet roads.",
            categoryId = "Hazardous Conditions"
        ),
        QuestionEntity(
            id = "41",
            code = "TS002",
            text = "A stop sign requires you to:",
            options = """["Slow down and give way","Come to a complete stop","Stop only if traffic is approaching","Give way to the right"]""",
            correctIndex = 1,
            explanation = "At a stop sign, you must come to a complete stop before the stop line or intersection.",
            categoryId = "Traffic Signs"
        ),
        QuestionEntity(
            id = "42",
            code = "PK004",
            text = "What is the minimum distance for parking from an intersection without traffic lights?",
            options = """["5 metres","10 metres","15 metres","20 metres"]""",
            correctIndex = 1,
            explanation = "You must not park within 10 metres of an intersection without traffic lights.",
            categoryId = "Parking"
        ),
        QuestionEntity(
            id = "43",
            code = "MW002",
            text = "If your car breaks down on a motorway, you should:",
            options = """["Stay in the car with hazard lights on","Move to the left shoulder and exit the car on the left side","Wait in the middle lane for help","Try to push the car to an exit"]""",
            correctIndex = 1,
            explanation = "Pull onto the left shoulder, turn on hazard lights, and exit from the left side away from traffic.",
            categoryId = "Motorway Driving"
        ),
        QuestionEntity(
            id = "44",
            code = "SF002",
            text = "When must children use an approved child restraint?",
            options = """["Until age 4","Until age 7","Until age 7 or 145cm tall","Until age 12"]""",
            correctIndex = 1,
            explanation = "Children under 7 must be secured in an approved child restraint suitable for their size.",
            categoryId = "Safety"
        ),
        QuestionEntity(
            id = "45",
            code = "TS003",
            text = "What does a flashing yellow light at a pedestrian crossing mean?",
            options = """["Stop immediately","Proceed with caution, give way to pedestrians","Speed up to clear the crossing","The lights are malfunctioning"]""",
            correctIndex = 1,
            explanation = "A flashing yellow light means proceed with caution and give way to any pedestrians.",
            categoryId = "Traffic Signals"
        )
    )
}
