public class Environment {
    private SelfDrivingCar selfDrivingCar;
    private RetractableBollardIntersection bollards;
    private Camera camera;

    private DecisionMaker decisionMaker;

    public static void main(String[] args) {
        SelfDrivingCar selfDrivingCar1 = new SelfDrivingCar(150, IntersectionRoads.LEFT);
        System.out.println(selfDrivingCar1.adjustSpeed(0));
    }
}
