import java.util.List;
import java.util.Map;
import java.util.Random;

public class Environment {

    /**
     * Nombre de mètres entre la voiture au début du sénario et le premier feu tricolore
     */
    public static final int DISTANCE_TO_TRAFFIC_LIGHT = 50;

    /**
     * Nombre de mètres entre le premier feu tricolore et le centre de l'intersection
     */
    public static final int DISTANCE_TO_INTERSECTION_CENTER = 20;

    /**
     * Vitesse en km/h à adopter dans une intersection
     */
    public static final int SPEED_IN_INTERSECTION = 20;

    public static void main(String[] args) {
        Random random = new Random();

        // Vitesse de la voiture aléatoire (entre 40 et 80 km/h) + Itinéraire aléatoire (sans prendre en compte le demi-tour)
        SelfDrivingCar selfDrivingCar = new SelfDrivingCar(random.nextInt(40, 85),
                IntersectionRoads.values()[random.nextInt(1, IntersectionRoads.values().length)]);

        RetractableBollardIntersection bollard = new RetractableBollardIntersection(random.nextBoolean());

        Camera camera = new Camera();
        DecisionMaker decisionMaker = new DecisionMaker();

        Map<Integer, String> decisions = decisionMaker.makeDecision(selfDrivingCar, bollard, camera);

        System.out.println("Décisions : ");

        for (Map.Entry<Integer, String> decision : decisions.entrySet()) {
            System.out.println("Étape " + decision.getKey() + " : " + decision.getValue() + ".");
        }
    }

    /**
     * Permet d'afficher toutes les données concernant l'environnement
     * @param situations
     * @param carSpeed
     * @param carItinerary
     * @param bollardsRaised
     */
    public static void printSituation(List<EnvironmentSituation> situations, int carSpeed, IntersectionRoads carItinerary, boolean bollardsRaised) {
        System.out.println("Environnement :");

        for(EnvironmentSituation situation : situations) {
            switch (situation) {
                case RED_TRAFFIC_LIGHT_UP_DOWN_ROAD -> {
                    System.out.println("Le feu sur la voie de la voiture est rouge.");
                }
                case RED_TRAFFIC_LIGHT_LEFT_RIGHT_ROAD -> {
                    System.out.println("Le feu sur la voie de la voiture est vert.");
                }
                case PEDESTRIAN_UP_ROAD -> {
                    System.out.println("Il y a un piéton qui traverse sur la route du haut.");
                }
                case PEDESTRIAN_LEFT_ROAD -> {
                    System.out.println("Il y a un piéton qui traverse sur la route de gauche.");
                }
                case PEDESTRIAN_RIGHT_ROAD -> {
                    System.out.println("Il y a un piéton qui traverse sur la route de droite.");
                }
                case PEDESTRIAN_DOWN_ROAD -> {
                    System.out.println("Il y a un piéton qui traverse sur la route du bas.");
                }
                case CAR_COMES_FROM_DOWN -> {
                    System.out.println("Il y a une voiture qui arrive sur la route du bas.");
                }
            }
        }

        System.out.println("La voiture roule à une vittesse de " + carSpeed + " km/h.");

        switch (carItinerary) {
            case LEFT -> {
                System.out.println("La voiture souhaite aller sur la route de gauche.");
            }
            case RIGHT -> {
                System.out.println("La voiture souhaite aller sur la route de droite.");
            }
            case DOWN -> {
                System.out.println("La voiture souhaite aller sur la route du bas.");
            }
        }

        if (bollardsRaised) {
            System.out.println("Les bornes escamotables sont levées");
        } else {
            System.out.println("Les bornes escamotables sont baissées");
        }

        System.out.print("\n");
    }
}
