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
     * Constructeur par défaut
     * @param speed
     * @param itinerary
     */
    public SelfDrivingCar(int speed, IntersectionRoads itinerary) {
        this.speed = speed;
        this.itinerary = itinerary;
    }

    /**
     * Donne des informations sur la vitesse et la trajectoire de la voiture
     * @return
     */
    @Override
    public Object[] getInformation() {
        return null;
    }

    /**
     * Permet d'ajuster la vitesse de la voiture
     * @param newSpeed : nouvelle vitesse à atteindre
     * @return : Le nombre de mètres parcouru avant d'atteindre la vitesse désirée
     */
    public int adjustSpeed(int newSpeed) {
        int distance = 0;
        int averageSpeed = Math.abs(newSpeed - this.speed);

        if (newSpeed > this.speed) {
            double timeSpent = kmPerHInMPerS(newSpeed) / ACCELERATION;

            distance = (int) ((kmPerHInMPerS(averageSpeed) / 2) * timeSpent);
        } else if (newSpeed < this.speed) {
            distance = (int) Math.pow((double) averageSpeed / 10, 2);
        }

        this.speed = newSpeed;

        return distance;
    }

    /**
     * Convertit une vitesse en km/h en m/s
     * @param kmPerH
     * @return
     */
    private double kmPerHInMPerS(int kmPerH) {
        return (double) (kmPerH * 1000) /3600;
    }
}
