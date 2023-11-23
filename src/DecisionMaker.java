import java.util.List;

public class DecisionMaker {

    /**
     * Récupère les informations de tous les agents et prend une décision
     */
    public void makeDecision(SelfDrivingCar selfDrivingCar, RetractableBollardIntersection bollards, Camera camera) {
        Object[] carInformation = selfDrivingCar.getInformation();
        int carSpeed = (int) carInformation[0];
        IntersectionRoads carItinerary = (IntersectionRoads) carInformation[1];

        boolean bollardsRaised = bollards.getInformation();

        List<String> cameraInformation = camera.getInformation();
    }

}
