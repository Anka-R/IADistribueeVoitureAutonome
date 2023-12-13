import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionMaker {

    /**
     * Utilité d'avoir ou de ne pas avoir des dégâts matériels.
     * élément 0 : utilité d'avoir des dégâts matériels.
     * élément 1 : utilité de ne pas avoir des dégâts matériels dans le cas où les bornes escamotables sont levées.
     */
    private static final int[] CAR_DAMAGE_UTILITY = {-3, 2};

    /**
     * Utilité de renverser ou de ne pas renverser un piéton.
     * élément 0 : utilité de renverser un piéton.
     * élément 1 : utilité de ne pas renverser un piéton dans le cas où un piéton traverse.
     */
    private static final int[] PEDESTRIAN_DAMAGE_UTILITY = {-10, 2};

    /**
     * Utilité de s'arrêter mais qu'il n'y ai pas de piéton ou de voiture bloquant le passage.
     */
    private static final int STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY = -4;

    /**
     * Utilité de s'arrêter mais que les bornes escamotables ne soient pas levées.
     */
    private static final int STOP_BUT_NO_BOLLARD_UTILITY = -4;

    /**
     * Utilité de traverser rapidement l'intersection.
     */
    private static final int FAST_CROSSING_UTILITY = 2;

    /**
     * Probabilité que les bornes escamotables soient levées
     */
    private double bollardRaisedProbability = 0;

    /**
     * Probabilité qu'il y ai un piéton sur la route du haut
     */
    private double pedestrianUpRoadProbability = 0;

    /**
     * Probabilité qu'il y ai un piéton sur la route de gauche
     */
    private double pedestrianLeftRoadProbability = 0;

    /**
     * Probabilité qu'il y ai un piéton sur la route du bas
     */
    private double pedestrianDownRoadProbability = 0;

    /**
     * Probabilité qu'il y ai un piéton sur la route de droite
     */
    private double pedestrianRightRoadProbability = 0;

    /**
     * Probabilité qu'il y ai une voiture sur la route du bas
     */
    private double carDownRoadProbability = 0;

    /**
     * Récupère les informations de tous les agents et prend une décision
     */
    public Map<Integer, String> makeDecision(SelfDrivingCar selfDrivingCar, RetractableBollardIntersection bollards, Camera camera) {
        Map<Integer, String> steps = new HashMap<>();

        Object[] carInformation = selfDrivingCar.getInformation();
        int carSpeed = (int) carInformation[0];
        IntersectionRoads carItinerary = (IntersectionRoads) carInformation[1];

        List<EnvironmentSituation> cameraInformation = camera.getInformation();
        cameraInformation = reorganiseElements(cameraInformation);

        Environment.printSituation(cameraInformation, carSpeed, carItinerary, bollards.getInformation());

        int stepCount = 1;
        int situationNumber = 0;
        boolean abnormalSituation = false;
        boolean noObstacleStep1 = false;

        while (situationNumber < cameraInformation.size() && !abnormalSituation) {
            EnvironmentSituation situation = cameraInformation.get(situationNumber);
            boolean bollardsRaised = bollards.getInformation();

            if (situation.equals(EnvironmentSituation.RED_TRAFFIC_LIGHT_UP_DOWN_ROAD)
                    || situation.equals(EnvironmentSituation.PEDESTRIAN_UP_ROAD) || bollardsRaised) {

                // OBSTACLE DANS L'ÉTAPE 1

                boolean carCanStop = carCanReachSpeed(selfDrivingCar.getSpeed(), 0, selfDrivingCar.getDistanceToNextObstacle());
                steps.put(stepCount, firstObstacleDecisionText(carCanStop, situation, bollardsRaised));

                if (bollardsRaised) {
                    bollards.setBollardRaised(false);
                }

                selfDrivingCar.setSpeed(0);
                selfDrivingCar.setDistanceToNextObstacle(Environment.DISTANCE_TO_INTERSECTION_CENTER);

                abnormalSituation = !carCanStop;

                if (!abnormalSituation) {
                    // On estime qu'une conduite raisonnable implique une vitesse de 20 km/h dans une intersection
                    boolean carCanReachSpeed = carCanReachSpeed(selfDrivingCar.getSpeed(), Environment.SPEED_IN_INTERSECTION, Environment.DISTANCE_TO_TRAFFIC_LIGHT/2);

                    if (carCanReachSpeed) {
                        selfDrivingCar.setSpeed(Environment.SPEED_IN_INTERSECTION);
                        selfDrivingCar.setDistanceToNextObstacle(Environment.DISTANCE_TO_TRAFFIC_LIGHT/2);
                    }
                }

            } else if ((situation.equals(EnvironmentSituation.PEDESTRIAN_LEFT_ROAD) && carItinerary.equals(IntersectionRoads.LEFT))
                    || ((situation.equals(EnvironmentSituation.PEDESTRIAN_RIGHT_ROAD)
                    || situation.equals(EnvironmentSituation.CAR_COMES_FROM_DOWN)) && carItinerary.equals(IntersectionRoads.RIGHT))
                    || (situation.equals(EnvironmentSituation.PEDESTRIAN_DOWN_ROAD) && carItinerary.equals(IntersectionRoads.DOWN))) {

                // OBSTACLE DANS L'ÉTAPE 2

                boolean carCanStop = carCanReachSpeed(selfDrivingCar.getSpeed(), 0, selfDrivingCar.getDistanceToNextObstacle());

                if (carCanStop) {
                    steps.put(stepCount, "La voiture s'arrête au centre de l'intersection"
                            + (situation.equals(EnvironmentSituation.CAR_COMES_FROM_DOWN) ?
                            " et laisse passer la voiture arrivant en face" : " et laisse passer le piéton"));

                    selfDrivingCar.setDistanceToNextObstacle(0);
                    selfDrivingCar.setSpeed(0);
                } else {
                    steps.put(stepCount, "La voiture ne peut pas s'arrêter au centre de l'intersection et le dépasse");
                    abnormalSituation = true;
                }

            } else if (!situation.equals(EnvironmentSituation.RED_TRAFFIC_LIGHT_LEFT_RIGHT_ROAD)) {

                // AUCUN OBSTACLE DANS L'ÉTAPE 1 (La voiture avance directement au centre de l'intersection)

                if (!noObstacleStep1) {

                    noObstacleStep1 = true;
                    boolean carCanReachSpeed = carCanReachSpeed(selfDrivingCar.getSpeed(), Environment.SPEED_IN_INTERSECTION, selfDrivingCar.getDistanceToNextObstacle());

                    if (carCanReachSpeed) {
                        steps.put(stepCount, "La voiture ralentit à " + Environment.SPEED_IN_INTERSECTION + " km/h dans l'intersection");
                        selfDrivingCar.setSpeed(Environment.SPEED_IN_INTERSECTION);
                        selfDrivingCar.setDistanceToNextObstacle(Environment.DISTANCE_TO_INTERSECTION_CENTER);
                    } else {
                        steps.put(stepCount, "La voiture ne peut pas ralentir à " + Environment.SPEED_IN_INTERSECTION + " km/h et roule plus rapidement dans l'intersection");
                        abnormalSituation = true;
                    }
                } else {
                    stepCount--;
                }
            } else {
                stepCount--;
            }

            situationNumber++;
            stepCount++;
        }

        if (!abnormalSituation) {
            if (carItinerary.equals(IntersectionRoads.DOWN)) {
                steps.put(stepCount, "La voiture poursuit sur la voie en face et continue sa route");
            } else {
                steps.put(stepCount, "La voiture tourne sur sa voie et continue sa route");
            }
        }

        return steps;
    }

    /**
     * Prend des décisions en fonction des probabilité et utilité des obstacles.
     * @param selfDrivingCar
     * @param bollards
     * @param camera
     * @return Un tableau contenant les éléments suivants :
     *          - élément 0 : utilité totale obtenue.
     *          - élément 1 : décisions prises.
     */
    public Object[] makeDecisionWithUtilities(SelfDrivingCar selfDrivingCar, RetractableBollardIntersection bollards, Camera camera) {
        Map<Integer, String> steps = new HashMap<>();
        int globalUtility;

        Object[] carInformation = selfDrivingCar.getInformation();
        int carSpeed = (int) carInformation[0];
        IntersectionRoads carItinerary = (IntersectionRoads) carInformation[1];

        List<EnvironmentSituation> cameraInfo = camera.getInformation();
        Environment.printSituation(cameraInfo, carSpeed, carItinerary, bollards.getInformation());


        // -----  FIRST OBSTACLE  -----

        double stopUtilityFirstObstacleEsperance = (bollardRaisedProbability * CAR_DAMAGE_UTILITY[1])
                + (pedestrianUpRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[1]) + ((1 - bollardRaisedProbability) * STOP_BUT_NO_BOLLARD_UTILITY)
                + ((1 - pedestrianUpRoadProbability) * STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY);

        double noFirstObstacleProbability = (1 - bollardRaisedProbability) * (1 - pedestrianUpRoadProbability);

        double notStopUtilityFirstObstacleEsperance = (noFirstObstacleProbability * FAST_CROSSING_UTILITY) + (bollardRaisedProbability * CAR_DAMAGE_UTILITY[0])
                + (pedestrianUpRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[0]);

        System.out.println("\nEspérances d'utilités : ");
        System.out.println("- S'arrêter au niveau du premier obstacle : " + stopUtilityFirstObstacleEsperance);
        System.out.println("- Ne pas s'arrêter au niveau du premier obstacle : " + notStopUtilityFirstObstacleEsperance);


        if (stopUtilityFirstObstacleEsperance >= notStopUtilityFirstObstacleEsperance) {
            steps.put(1, "La voiture s'arrête au niveau du premier obstacle éventuel");
        } else {
            steps.put(1, "La voiture ne s'arrête pas au niveau du premier obstacle éventuel");
        }

        Object[] firstObstacleUtilityResult = calculateUtilityFirstObstacle(stopUtilityFirstObstacleEsperance >= notStopUtilityFirstObstacleEsperance,
                bollards.getInformation(), cameraInfo);

        globalUtility = (int) firstObstacleUtilityResult[0];
        String firstObstacleDamageType = (String) firstObstacleUtilityResult[1];

        if (firstObstacleDamageType == null) {
            // -----  SECOND OBSTACLE  -----

            double stopUtilitySecondObstacleEsperance = 0;
            double notStopUtilitySecondObstacleEsperance = 0;

            switch (carItinerary) {
                case LEFT -> {
                    stopUtilitySecondObstacleEsperance = (pedestrianLeftRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[1])
                            + ((1 - pedestrianLeftRoadProbability) * STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY);

                    notStopUtilitySecondObstacleEsperance = ((1 - pedestrianLeftRoadProbability) * FAST_CROSSING_UTILITY)
                            + (pedestrianLeftRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[0]);
                }
                case DOWN -> {
                    stopUtilitySecondObstacleEsperance = (pedestrianDownRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[1])
                            + ((1 - pedestrianDownRoadProbability) * STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY);

                    notStopUtilitySecondObstacleEsperance = ((1 - pedestrianDownRoadProbability) * FAST_CROSSING_UTILITY)
                            + (pedestrianDownRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[0]);
                }
                case RIGHT -> {
                    stopUtilitySecondObstacleEsperance = (pedestrianRightRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[1]
                            + ((1 - pedestrianRightRoadProbability) * STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY))
                            + (carDownRoadProbability * CAR_DAMAGE_UTILITY[1]) + ((1 - carDownRoadProbability) * STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY);

                    notStopUtilitySecondObstacleEsperance = (((1 - pedestrianDownRoadProbability) * (1 - carDownRoadProbability)) * FAST_CROSSING_UTILITY)
                            + (pedestrianDownRoadProbability * PEDESTRIAN_DAMAGE_UTILITY[0]) + (carDownRoadProbability * CAR_DAMAGE_UTILITY[0]);
                }
            }

            System.out.println("- S'arrêter au niveau du deuxième obstacle : " + stopUtilitySecondObstacleEsperance);
            System.out.println("- Ne pas s'arrêter au niveau du deuxième obstacle : " + notStopUtilitySecondObstacleEsperance);

            if (stopUtilitySecondObstacleEsperance >= notStopUtilitySecondObstacleEsperance) {
                steps.put(2, "La voiture s'arrête au niveau du deuxième obstacle éventuel");
            } else {
                steps.put(2, "La voiture ne s'arrête pas au niveau du deuxième obstacle éventuel mais ne provoque aucun dégât");
            }

            Object[] secondObstacleUtilityResult = calculateUtilitySecondObstacle(
                    stopUtilitySecondObstacleEsperance >= notStopUtilitySecondObstacleEsperance, carItinerary, cameraInfo);

            globalUtility += (int) secondObstacleUtilityResult[0];
            String secondObstacleDamageType = (String) secondObstacleUtilityResult[1];

            if (secondObstacleDamageType != null) {

                if (secondObstacleDamageType.equals("PEDESTRIAN_DAMAGE")) {
                    steps.put(2, "La voiture ne s'arrête pas au centre de l'intersection et renverse le piéton");
                } else if (secondObstacleDamageType.equals("CAR_DAMAGE")) {
                    steps.put(2, "La voiture ne s'arrête pas au centre de l'intersection et percute la voiture arrivant en face");
                }

            } else {
                if (carItinerary.equals(IntersectionRoads.DOWN)) {
                    steps.put(3, "La voiture continue sur la voie d'en face et poursuit sa route");
                } else {
                    steps.put(3, "La voiture tourne sur sa voie et poursuit sa route");
                }
            }

        } else {

            if (firstObstacleDamageType.equals("PEDESTRIAN_DAMAGE")) {
                steps.put(1, "La voiture ne s'arrête pas au passage clouté et renverse le piéton");
            } else if (firstObstacleDamageType.equals("CAR_DAMAGE")) {
                steps.put(1, "La voiture ne s'arrête pas devant les bornes escamotables et les touche");
            }

        }

        return new Object[]{globalUtility, steps};

    }

    /**
     * Détermine si la voiture peut atteindre la vitesse souhaitée sur la distance disponible
     * @param oldSpeed
     * @param newSpeed
     * @param distance
     * @return
     */
    private boolean carCanReachSpeed(int oldSpeed, int newSpeed, int distance) {
        int distanceToAdjustSpeed = SelfDrivingCar.distanceToAdjustSpeed(oldSpeed, newSpeed);

        return distanceToAdjustSpeed <= distance;
    }

    /**
     * Retourne le texte de l'étape dans le cas d'un obstacle dès l'arrivée de la voiture.
     * @param carCanStop
     * @param situation
     * @param bollardRaised
     * @return
     */
    private String firstObstacleDecisionText(boolean carCanStop, EnvironmentSituation situation, boolean bollardRaised) {
        String text = "La voiture";

        if (carCanStop) {
            text += " s'arrête";
        } else {
            text += " ne peut pas s'arrêter";
        }

        if (bollardRaised) {

            text += " devant les bornes escamotables";
            if (!carCanStop) {
                text += " et les touche";
            } else {
                text += " et attend qu'elles se baissent";
            }

        } else {

            switch (situation) {
                case RED_TRAFFIC_LIGHT_UP_DOWN_ROAD -> {
                    text += " au feu rouge";
                    if (!carCanStop) {
                        text += " et le dépasse";
                    } else {
                        text += " et attend qu'il passe au vert";
                    }
                }
                case PEDESTRIAN_UP_ROAD -> {
                    text += " devant le passage clouté";
                    if (!carCanStop) {
                        text += " et renverse le piéton";
                    } else {
                        text += " et laisse passer le piéton";
                    }
                }
            }
        }

        return text;
    }

    /**
     * Permet de réorganiser les situations
     * @param cameraInfo
     * @return
     */
    private List<EnvironmentSituation> reorganiseElements(List<EnvironmentSituation> cameraInfo) {
        List<EnvironmentSituation> situations = new ArrayList<>();
        situations.add(cameraInfo.get(0));

        if (cameraInfo.contains(EnvironmentSituation.PEDESTRIAN_UP_ROAD)) {
            situations.add(EnvironmentSituation.PEDESTRIAN_UP_ROAD);
        }

        for (int i = 1 ; i < cameraInfo.size() ; i++) {
            if (!cameraInfo.get(i).equals(EnvironmentSituation.PEDESTRIAN_UP_ROAD)) {
                situations.add(cameraInfo.get(i));
            }
        }

        return situations;
    }

    /**
     * Réalise 100 cycles de situations et calcule les probabilités de chaque obstacle.
     * @param camera
     */
    public void learnAndUpdateProbability(Camera camera) {
        final int ITERATIONS = 100;
        for (int i = 0 ; i < ITERATIONS ; i++) {
            List<EnvironmentSituation> situations = camera.getInformation();
            RetractableBollardIntersection bollardIntersection = new RetractableBollardIntersection();

            if (bollardIntersection.getInformation()) {
                bollardRaisedProbability++;
            }

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_UP_ROAD)) {
                pedestrianUpRoadProbability++;
            }

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_LEFT_ROAD)) {
                pedestrianLeftRoadProbability++;
            }

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_DOWN_ROAD)) {
                pedestrianDownRoadProbability++;
            }

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_RIGHT_ROAD)) {
                pedestrianRightRoadProbability++;
            }

            if (situations.contains(EnvironmentSituation.CAR_COMES_FROM_DOWN)) {
                carDownRoadProbability++;
            }
        }

        bollardRaisedProbability /= ITERATIONS;
        pedestrianUpRoadProbability /= ITERATIONS;
        pedestrianLeftRoadProbability /= ITERATIONS;
        pedestrianDownRoadProbability /= ITERATIONS;
        pedestrianRightRoadProbability /= ITERATIONS;
        carDownRoadProbability /= ITERATIONS;
    }

    /**
     * Permet d'afficher les probabilités des obstacles.
     */
    public void printProbabilities() {
        System.out.println("Probabilités :");
        System.out.println("- Bornes escamotables levées : " + bollardRaisedProbability);
        System.out.println("- Piéton sur la route du haut : " + pedestrianUpRoadProbability);
        System.out.println("- Piéton sur la route de gauche : " + pedestrianLeftRoadProbability);
        System.out.println("- Piéton sur la route du bas : " + pedestrianDownRoadProbability);
        System.out.println("- Piéton sur la route de droite : " + pedestrianRightRoadProbability);
        System.out.println("- Voiture provenant de la route du bas : " + carDownRoadProbability);
    }

    /**
     * Permet de calculer l'utilité obtenu pour le premier obstacle.
     * @param carStopped
     * @param bollardRaised
     * @param situations
     * @return Un tableau contenant les éléments suivants :
     *          - élément 0 : Utilité obtenue.
     *          - élément 1 : Type de dégâts dans le cas où il y en a.
     */
    private Object[] calculateUtilityFirstObstacle(boolean carStopped, boolean bollardRaised, List<EnvironmentSituation> situations) {
        Object[] result = new Object[2];
        result[1] = null;

        int utility;
        if (carStopped) {

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_UP_ROAD)) {
                utility = PEDESTRIAN_DAMAGE_UTILITY[1];
            } else if (bollardRaised) {
                utility = CAR_DAMAGE_UTILITY[1];
            } else {
                utility = STOP_BUT_NO_BOLLARD_UTILITY + STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY;
            }

        } else {

            if (situations.contains(EnvironmentSituation.PEDESTRIAN_UP_ROAD)) {
                utility = PEDESTRIAN_DAMAGE_UTILITY[0];
                result[1] = "PEDESTRIAN_DAMAGE";
            } else if (bollardRaised) {
                utility = CAR_DAMAGE_UTILITY[0];
                result[1] = "CAR_DAMAGE";
            } else {
                utility = FAST_CROSSING_UTILITY;
            }

        }

        result[0] = utility;

        return result;
    }

    /**
     * Permet de calculer l'utilité obtenu pour le deuxième obstacle.
     * @param carStopped
     * @param carItinerary
     * @param situations
     * @return Un tableau contenant les éléments suivants :
     *          - élément 0 : Utilité obtenue.
     *          - élément 1 : Type de dégâts dans le cas où il y en a.
     */
    private Object[] calculateUtilitySecondObstacle(boolean carStopped, IntersectionRoads carItinerary, List<EnvironmentSituation> situations) {
        Object[] result = new Object[2];
        result[1] = null;

        int utility = 0;

        switch (carItinerary) {
            case LEFT -> {
                if (carStopped) {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_LEFT_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[1];
                    } else {
                        utility = STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY;
                    }
                } else {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_LEFT_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[0];
                        result[1] =  "PEDESTRIAN_DAMAGE";
                    } else {
                        utility = FAST_CROSSING_UTILITY;
                    }
                }
            }
            case DOWN -> {
                if (carStopped) {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_DOWN_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[1];
                    } else {
                        utility = STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY;
                    }
                } else {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_DOWN_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[0];
                        result[1] =  "PEDESTRIAN_DAMAGE";
                    } else {
                        utility = FAST_CROSSING_UTILITY;
                    }
                }
            }
            case RIGHT -> {
                if (carStopped) {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_RIGHT_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[1];
                    } else if (situations.contains(EnvironmentSituation.CAR_COMES_FROM_DOWN)) {
                        utility = CAR_DAMAGE_UTILITY[1];
                    } else {
                        utility = STOP_BUT_NO_PEDESTRIAN_OR_CAR_UTILITY * 2; // Ni voiture, ni piéton
                    }
                } else {
                    if (situations.contains(EnvironmentSituation.PEDESTRIAN_RIGHT_ROAD)) {
                        utility = PEDESTRIAN_DAMAGE_UTILITY[0];
                        result[1] =  "PEDESTRIAN_DAMAGE";
                    } else if (situations.contains(EnvironmentSituation.CAR_COMES_FROM_DOWN)) {
                        utility = CAR_DAMAGE_UTILITY[0];
                        result[1] =  "CAR_DAMAGE";
                    } else {
                        utility = FAST_CROSSING_UTILITY;
                    }
                }
            }
        }

        result[0] = utility;

        return result;
    }

}
