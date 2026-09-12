package main;

import ontology.OntologyManager;

public class OntologyTest {

    public static void main(String[] args) {


        OntologyManager ontologyManager =
                new OntologyManager();

        System.out.println("Participants:");

        for (String driver : ontologyManager.getParticipants("Race1")) {
            System.out.println(driver);
        }

        System.out.println();


        System.out.println("DRIVERS:");

        for (String driver :
                ontologyManager.getAllDrivers()) {

            System.out.println(
                    driver +
                            " | Team=" +
                            ontologyManager.getTeam(driver) +
                            " | Car=" +
                            ontologyManager.getCar(driver)+
                            " | Skill=" +
                            ontologyManager.getDriverSkill(driver)+
                            " | CarPerformance=" +
                            ontologyManager.getCarPerformance(
                                    ontologyManager.getCar(driver)
                            )+
                            " | Strategy=" +
                            ontologyManager.getDrivingStyle(driver)
            );


        }

        System.out.println("\nTEAMS:");
        System.out.println(ontologyManager.getAllTeams());

        System.out.println("\nCARS:");
        System.out.println(ontologyManager.getAllCars());
    }
}