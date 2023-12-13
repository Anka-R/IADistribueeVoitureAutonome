import java.util.Random;

public class RetractableBollardIntersection implements Agent {

    private boolean bollardRaised;

    /**
     * Constructeur par défaut
     */
    public RetractableBollardIntersection() {
        Random random = new Random();
        this.bollardRaised = random.nextBoolean();
    }

    /**
     * Donne des informations sur l'état (levées ou baissées) des bornes escamotables
     * @return
     */
    @Override
    public Boolean getInformation() {
        return bollardRaised;
    }

    /**
     * Setter de l'état des bornes
     * @param newValue
     */
    public void setBollardRaised(boolean newValue) {
        this.bollardRaised = newValue;
    }
}
