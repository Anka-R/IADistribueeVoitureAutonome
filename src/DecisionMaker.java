import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionMaker {

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

}
