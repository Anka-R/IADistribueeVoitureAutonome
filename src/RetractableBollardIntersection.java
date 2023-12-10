public class RetractableBollardIntersection implements Agent {

    private boolean bollardRaised;

    /**
     * Constructeur par défaut
     * @param bollardRaised
     */
    public RetractableBollardIntersection(boolean bollardRaised) {
        this.bollardRaised = bollardRaised;
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
