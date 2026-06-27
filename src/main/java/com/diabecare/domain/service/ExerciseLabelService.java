package com.diabecare.domain.service;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseType;

public class ExerciseLabelService {

    public String resolveTypeLabel(ExerciseType type) {
        return switch (type) {
            case WALKING          -> "Caminata";
            case RUNNING          -> "Trote / Carrera";
            case JOGGING          -> "Carrera ligera (jogging)";
            case CYCLING          -> "Ciclismo";
            case STATIONARY_BIKE  -> "Bicicleta estática";
            case SWIMMING         -> "Natación";
            case WATER_AEROBICS   -> "Aeróbicos acuáticos";
            case WEIGHT_TRAINING  -> "Pesas";
            case CALISTHENICS     -> "Calistenia";
            case CROSSFIT         -> "CrossFit";
            case YOGA             -> "Yoga";
            case PILATES          -> "Pilates";
            case STRETCHING       -> "Estiramiento";
            case TAI_CHI          -> "Tai Chi";
            case FOOTBALL         -> "Fútbol";
            case BASKETBALL       -> "Baloncesto";
            case VOLLEYBALL       -> "Voleibol";
            case TENNIS           -> "Tenis";
            case PADEL            -> "Pádel";
            case BASEBALL         -> "Béisbol";
            case GOLF             -> "Golf";
            case DANCING          -> "Baile";
            case ZUMBA            -> "Zumba";
            case AEROBICS         -> "Aeróbicos";
            case HIKING           -> "Senderismo";
            case CLIMBING         -> "Escalada";
            case ELLIPTICAL       -> "Elíptica";
            case ROWING           -> "Remo";
            case JUMPING_ROPE     -> "Saltar la cuerda";
            case STAIR_CLIMBING   -> "Subir escaleras";
            case MARTIAL_ARTS     -> "Artes marciales";
            case BOXING           -> "Boxeo";
            case SKATING          -> "Patinaje";
            case SKIING           -> "Esquí";
            case SURFING          -> "Surf";
            case HOUSEHOLD_CHORES -> "Tareas del hogar";
            case GARDENING        -> "Jardinería";
            case PHYSICAL_THERAPY -> "Fisioterapia";
            case OTHER            -> "Otro";
        };
    }

    public String resolveIntensityLabel(ExerciseIntensity intensity) {
        return switch (intensity) {
            case LOW      -> "Baja";
            case MODERATE -> "Moderada";
            case HIGH     -> "Alta";
        };
    }
}