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

        int stepCount = 1;
        int situationNumber = 0;
        boolean abnormalSituation = false;
        while (situationNumber < cameraInformation.size() && !abnormalSituation) {
            EnvironmentSituation situation = cameraInformation.get(situationNumber);
            boolean bollardsRaised = bollards.getInformation();

            if (situation.equals(EnvironmentSituation.RED_TRAFFIC_LIGHT_UP_DOWN_ROAD)
                    || situation.equals(EnvironmentSituation.PEDESTRIAN_UP_ROAD) || bollardsRaised) {

                boolean carCanStop = carCanReachSpeed(carSpeed, 0, selfDrivingCar.getDistanceToNextObstacle());
                steps.put(stepCount, firstObstacleDecisionText(carCanStop, situation, bollardsRaised));

                if (bollardsRaised) {
                    bollards.setBollardRaised(false);
                }

                selfDrivingCar.setSpeed(0);
                selfDrivingCar.setDistanceToNextObstacle(20);

                abnormalSituation = !carCanStop;

            } else if ((situation.equals(EnvironmentSituation.PEDESTRIAN_LEFT_ROAD) && carItinerary.equals(IntersectionRoads.LEFT))
                    || ((situation.equals(EnvironmentSituation.PEDESTRIAN_RIGHT_ROAD)
                    || situation.equals(EnvironmentSituation.CAR_COMES_FROM_DOWN)) && carItinerary.equals(IntersectionRoads.RIGHT))
                    || (situation.equals(EnvironmentSituation.PEDESTRIAN_DOWN_ROAD) && carItinerary.equals(IntersectionRoads.DOWN))) {

                boolean carCanStop = carCanReachSpeed(carSpeed, 0, selfDrivingCar.getDistanceToNextObstacle());

            }

            situationNumber++;
            stepCount++;
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

}
