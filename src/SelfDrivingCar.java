public class SelfDrivingCar implements Agent {

    /**
     * Accélaration moyenne d'une voiture : 3m/s²
     */
    private static final int ACCELERATION = 3;

    /**
     * Vitesse de la voiture en km/h
     */
    private int speed;

    /**
     * Route sur laquelle la voiture souhaite aller
     */
    private IntersectionRoads itinerary;

    /**
     * Nombre de mètres entre la voiture et le prochain obstacle (feu rouge, bornes escamotables, piéton, ...)
     */
    private int distanceToNextObstacle;

    /**
     * Constructeur par défaut
     * @param speed
     * @param itinerary
     */
    public SelfDrivingCar(int speed, IntersectionRoads itinerary, int initialDistanceToTrafficLight) {
        this.speed = speed;
        this.itinerary = itinerary;
        this.distanceToNextObstacle = initialDistanceToTrafficLight;
    }

    /**
     * Donne des informations sur la vitesse et la trajectoire de la voiture
     * @return
     */
    @Override
    public Object[] getInformation() {
        Object[] carInformation = new Object[2];
        carInformation[0] = this.speed;
        carInformation[1] = this.itinerary;

        return carInformation;
    }

    /**
     * Permet de déterminer le nombre de mètre parcouru avant d'atteindre une vitesse
     * @param newSpeed : nouvelle vitesse à atteindre
     * @return : Le nombre de mètres parcouru avant d'atteindre la vitesse désirée
     */
    public static int distanceToAdjustSpeed(int oldSpeed, int newSpeed) {
        int distance = 0;
        int averageSpeed = Math.abs(newSpeed - oldSpeed);

        if (newSpeed > oldSpeed) {
            double timeSpent = kmPerHInMPerS(newSpeed) / ACCELERATION;

            distance = (int) ((kmPerHInMPerS(averageSpeed) / 2) * timeSpent);
        } else if (newSpeed < oldSpeed) {
            distance = (int) Math.pow((double) averageSpeed / 10, 2);
        }

        return distance;
    }

    /**
     * Convertit une vitesse en km/h en m/s
     * @param kmPerH
     * @return
     */
    private static double kmPerHInMPerS(int kmPerH) {
        return (double) (kmPerH * 1000) /3600;
    }

    /**
     * Getter de distanceToNextObstacle
     * @return
     */
    public int getDistanceToNextObstacle() {
        return distanceToNextObstacle;
    }

    /**
     * Setter de distanceToNextObstacle
     * @param distanceToNextObstacle
     */
    public void setDistanceToNextObstacle(int distanceToNextObstacle) {
        this.distanceToNextObstacle = distanceToNextObstacle;
    }

    /**
     * Setter de speed
     * @param speed
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
