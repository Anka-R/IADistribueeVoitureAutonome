import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Camera implements Agent {

    /**
     * Donne des informations sur ce que voit la camera
     * @return
     */
    @Override
    public List<EnvironmentSituation> getInformation() {
        List<EnvironmentSituation> situations = new ArrayList<>();
        Random random = new Random();
        int situationsNumber = random.nextInt(1, 5); // Entre 1 et 3 évènements simultanés
        EnvironmentSituation[] allSituations = EnvironmentSituation.values();

        // On détermine quelles routes possèdent des feux rouges
        situations.add(allSituations[random.nextInt(0, 2)]);

        for (int i = 1 ; i < situationsNumber ; i++) {
            EnvironmentSituation chosenSituation;

            do {
                // On choisit une situation aléatoire parmis toutes les situations possibles
                chosenSituation = allSituations[random.nextInt(2, allSituations.length)];

            } while (situations.contains(chosenSituation));

            situations.add(chosenSituation);
        }

        return situations;
    }
}
