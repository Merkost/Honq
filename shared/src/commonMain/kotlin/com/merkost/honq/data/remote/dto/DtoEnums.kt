package com.merkost.honq.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class StateIdDto {
    @SerialName("nsw") NSW,
    @SerialName("vic") VIC,
    @SerialName("qld") QLD,
    @SerialName("wa") WA,
    @SerialName("sa") SA,
    @SerialName("tas") TAS,
    @SerialName("act") ACT,
    @SerialName("nt") NT
}

@Serializable
enum class LicenseTypeIdDto {
    @SerialName("car") CAR,
    @SerialName("rider") RIDER
}

@Serializable
enum class LicenseStageIdDto {
    @SerialName("learner") LEARNER,
    @SerialName("p1") P1,
    @SerialName("p2") P2,
    @SerialName("full") FULL
}

@Serializable
enum class AssessmentTypeIdDto {
    @SerialName("knowledge_test") KNOWLEDGE_TEST,
    @SerialName("hazard_perception") HAZARD_PERCEPTION
}

@Serializable
enum class QuestionCategoryDto {
    @SerialName("alcohol_and_drugs") ALCOHOL_AND_DRUGS,
    @SerialName("bicycle_safety") BICYCLE_SAFETY,
    @SerialName("fatigue_and_defensive_driving") FATIGUE_AND_DEFENSIVE_DRIVING,
    @SerialName("general_knowledge") GENERAL_KNOWLEDGE,
    @SerialName("icac") ICAC,
    @SerialName("intersections") INTERSECTIONS,
    @SerialName("negligent_driving") NEGLIGENT_DRIVING,
    @SerialName("pedestrians") PEDESTRIANS,
    @SerialName("rider_safety") RIDER_SAFETY,
    @SerialName("road_users_hazards") ROAD_USERS_HAZARDS,
    @SerialName("seat_belts_restraints") SEAT_BELTS_RESTRAINTS,
    @SerialName("speed_limits") SPEED_LIMITS,
    @SerialName("traffic_lights_lanes") TRAFFIC_LIGHTS_LANES,
    @SerialName("traffic_signs") TRAFFIC_SIGNS
}


@Serializable
enum class ResourceTypeDto {
    @SerialName("practice_test") PRACTICE_TEST,
    @SerialName("pdf") PDF,
    @SerialName("handbook") HANDBOOK,
    @SerialName("other") OTHER
}
