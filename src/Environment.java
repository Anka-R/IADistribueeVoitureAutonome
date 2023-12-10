public class Environment {

    public static void main(String[] args) {
        SelfDrivingCar selfDrivingCar = new SelfDrivingCar(60, IntersectionRoads.LEFT, 50);
        RetractableBollardIntersection bollard = new RetractableBollardIntersection(false);
        Camera camera = new Camera();
        DecisionMaker decisionMaker = new DecisionMaker();

        System.out.println(decisionMaker.makeDecision(selfDrivingCar, bollard, camera));
    }
}
