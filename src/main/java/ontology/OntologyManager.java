package ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OntologyManager {

    private OWLOntology ontology;
    private OWLOntologyManager manager;
    private OWLDataFactory dataFactory;
    private OWLReasoner reasoner;
    private String baseIri;

    private static final Path DATA_DIRECTORY =
            Paths.get("data");

    private static final Path DATA_ONTOLOGY_PATH =
            DATA_DIRECTORY.resolve("f1_ontology.owl");

    public OntologyManager() {
        try {
            manager = OWLManager.createOWLOntologyManager();

            Files.createDirectories(DATA_DIRECTORY);

            if (Files.notExists(DATA_ONTOLOGY_PATH)) {

                try (InputStream inputStream =
                             getClass()
                                     .getClassLoader()
                                     .getResourceAsStream("f1_ontology.owl")) {

                    if (inputStream == null) {
                        throw new RuntimeException(
                                "Cannot find f1_ontology.owl in resources"
                        );
                    }

                    Files.copy(
                            inputStream,
                            DATA_ONTOLOGY_PATH
                    );
                }

                System.out.println(
                        "Ontology copied to: " +
                                DATA_ONTOLOGY_PATH.toAbsolutePath()
                );
            }

            ontology =
                    manager.loadOntologyFromOntologyDocument(
                            DATA_ONTOLOGY_PATH.toFile()
                    );

            dataFactory =
                    manager.getOWLDataFactory();

            reasoner =
                    new Reasoner.ReasonerFactory()
                            .createReasoner(ontology);

            reasoner.precomputeInferences();

            System.out.println(
                    "HermiT reasoner initialized. Ontology consistent: "
                            + reasoner.isConsistent()
            );

            Optional<IRI> iri =
                    ontology
                            .getOntologyID()
                            .getOntologyIRI();

            if (iri.isEmpty()) {
                throw new RuntimeException(
                        "Ontology IRI is missing"
                );
            }

            baseIri =
                    iri.get().toString();

            System.out.println(
                    "Ontology loaded from data directory: " +
                            DATA_ONTOLOGY_PATH.toAbsolutePath()
            );

            System.out.println(
                    "Ontology IRI: " +
                            baseIri
            );


        } catch (Exception e) {
            throw new RuntimeException(
                    "Error loading ontology",
                    e
            );
        }
    }

    public String getTrackCondition(String raceName) {
        return getObjectProperty(raceName, "hasTrackCondition");
    }

    public String getWeather(String raceName) {
        return getObjectProperty(raceName, "hasWeather");
    }

    private String getObjectProperty(String individualName, String propertyName) {
        OWLNamedIndividual individual =
                dataFactory.getOWLNamedIndividual(IRI.create(baseIri + "#" + individualName));

        OWLObjectProperty property =
                dataFactory.getOWLObjectProperty(IRI.create(baseIri + "#" + propertyName));

        for (OWLObjectPropertyAssertionAxiom ax : ontology.getObjectPropertyAssertionAxioms(individual)) {
            if (ax.getProperty().asOWLObjectProperty().equals(property)) {
                return ax.getObject().asOWLNamedIndividual().getIRI().getShortForm();
            }
        }

        return null;
    }
    public double getAirTemperature(String raceName) {
        return getRequiredDataPropertyValueAsDouble(raceName, "airTemperature");
    }

    public int getLaps(String raceName) {
        return getRequiredDataPropertyValueAsInt(raceName, "laps");
    }

    private int getRequiredDataPropertyValueAsInt(String individualName, String propertyName) {
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(
                IRI.create(baseIri + "#" + individualName)
        );

        OWLDataProperty property = dataFactory.getOWLDataProperty(
                IRI.create(baseIri + "#" + propertyName)
        );

        return ontology.dataPropertyAssertionAxioms(individual)
                .filter(ax -> ax.getProperty().asOWLDataProperty().equals(property))
                .map(ax -> ax.getObject().getLiteral())
                .mapToInt(Integer::parseInt)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing required property '" + propertyName +
                                "' for individual '" + individualName + "'"
                ));
    }

    public double getDrivingStyleThreshold(
            String drivingStyle,
            String thresholdProperty
    ) {
        return getRequiredDataPropertyValueAsDouble(
                drivingStyle,
                thresholdProperty
        );
    }

    private double getRequiredDataPropertyValueAsDouble(String individualName, String propertyName) {
        OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(
                IRI.create(baseIri + "#" + individualName)
        );

        OWLDataProperty property = dataFactory.getOWLDataProperty(
                IRI.create(baseIri + "#" + propertyName)
        );

        return ontology.dataPropertyAssertionAxioms(individual)
                .filter(ax -> ax.getProperty().asOWLDataProperty().equals(property))
                .map(ax -> ax.getObject().getLiteral())
                .mapToDouble(Double::parseDouble)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Missing required property '" + propertyName +
                                "' for individual '" + individualName + "'"
                ));
    }

    public String getCircuit(String raceName) {
        return getObjectProperty(raceName, "hasCircuit");
    }

    public String getDriver(String raceName) {
        return getObjectProperty(raceName, "hasFocusDriver");
    }

    public String getTeam(String driverName) {
        return getObjectProperty(driverName, "drivesFor");
    }
    public int getDriverSkill(String driverName) {
        return getRequiredDataPropertyValueAsInt(driverName, "driverSkill");
    }
    public String getDrivingStyle(String driverName) {
        String drivingStyle = getObjectProperty(driverName, "hasDrivingStyle");

        if (drivingStyle == null || drivingStyle.isBlank()) {
            throw new IllegalStateException(
                    "Missing required property 'hasDrivingStyle' for driver '" +
                            driverName + "'"
            );
        }

        return drivingStyle;
    }

    public int getCarPerformance(String carName) {
        return getRequiredDataPropertyValueAsInt(carName, "carPerformance");
    }
    public String getCar(String driverName) {
        return getObjectProperty(driverName, "drivesCar");
    }

    public List<String> getAllDrivers() {
        return getIndividualsByClass("Driver");
    }

    public List<String> getAllTeams() {
        return getIndividualsByClass("Team");
    }

    public List<String> getAllCars() {
        return getIndividualsByClass("Car");
    }

    private List<String> getIndividualsByClass(String className) {

        List<String> results = new ArrayList<>();

        OWLClass owlClass =
                dataFactory.getOWLClass(
                        IRI.create(baseIri + "#" + className)
                );

        for (OWLNamedIndividual individual :
                ontology.getIndividualsInSignature()) {

            boolean belongsToClass =
                    ontology.getClassAssertionAxioms(individual)
                            .stream()
                            .anyMatch(axiom ->
                                    axiom.getClassesInSignature()
                                            .contains(owlClass)
                            );

            if (belongsToClass) {
                results.add(
                        individual.getIRI().getShortForm()
                );
            }
        }

        results.sort(String::compareToIgnoreCase);

        return results;
    }
    public void saveStrategyToOntology(String raceName, String recommendedTyre, int pitStops) {
        try {
            String strategyName = "GeneratedStrategy_" + raceName;

            OWLClass strategyClass = dataFactory.getOWLClass(IRI.create(baseIri + "#Strategy"));
            OWLNamedIndividual strategyIndividual =
                    dataFactory.getOWLNamedIndividual(IRI.create(baseIri + "#" + strategyName));

            OWLClassAssertionAxiom classAssertion =
                    dataFactory.getOWLClassAssertionAxiom(strategyClass, strategyIndividual);

            manager.addAxiom(ontology, classAssertion);

            OWLObjectProperty hasTyre =
                    dataFactory.getOWLObjectProperty(IRI.create(baseIri + "#hasTyre"));

            OWLNamedIndividual tyreIndividual =
                    dataFactory.getOWLNamedIndividual(IRI.create(baseIri + "#" + recommendedTyre));

            OWLObjectPropertyAssertionAxiom tyreAssertion =
                    dataFactory.getOWLObjectPropertyAssertionAxiom(
                            hasTyre,
                            strategyIndividual,
                            tyreIndividual
                    );

            manager.addAxiom(ontology, tyreAssertion);

            OWLDataProperty pitStopsProperty =
                    dataFactory.getOWLDataProperty(IRI.create(baseIri + "#pitStops"));

            OWLDataPropertyAssertionAxiom pitStopsAssertion =
                    dataFactory.getOWLDataPropertyAssertionAxiom(
                            pitStopsProperty,
                            strategyIndividual,
                            pitStops
                    );

            manager.addAxiom(ontology, pitStopsAssertion);

            manager.saveOntology(
                    ontology,
                    IRI.create(
                            DATA_ONTOLOGY_PATH.toFile()
                    )
            );

            System.out.println("Strategy saved to ontology: " + strategyName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<String> getAllStrategies() {
        List<String> results = new ArrayList<>();

        OWLClass strategyClass =
                dataFactory.getOWLClass(IRI.create(baseIri + "#Strategy"));

        for (OWLNamedIndividual ind : ontology.getIndividualsInSignature()) {
            boolean isStrategy = ontology.getClassAssertionAxioms(ind)
                    .stream()
                    .anyMatch(ax -> ax.getClassesInSignature().contains(strategyClass));

            if (isStrategy) {
                String name = ind.getIRI().getShortForm();

                String tyre = getObjectProperty(name, "hasTyre");
                int pits = getRequiredDataPropertyValueAsInt(name, "pitStops");

                results.add(name + " | Tyre=" + tyre + " | PitStops=" + pits);
            }
        }

        return results;
    }
    public void clearGeneratedStrategies() {
        try {
            OWLClass strategyClass =
                    dataFactory.getOWLClass(IRI.create(baseIri + "#Strategy"));

            List<OWLNamedIndividual> toRemove = new ArrayList<>();

            for (OWLNamedIndividual ind : ontology.getIndividualsInSignature()) {
                String name = ind.getIRI().getShortForm();

                if (name.startsWith("GeneratedStrategy_")) {
                    toRemove.add(ind);
                }
            }

            for (OWLNamedIndividual ind : toRemove) {
                java.util.Set<OWLAxiom> axioms = ontology.getReferencingAxioms(ind);
                manager.removeAxioms(ontology, axioms);
            }

            manager.saveOntology(
                    ontology,
                    IRI.create(
                            DATA_ONTOLOGY_PATH.toFile()
                    )
            );

            System.out.println("Generated strategies cleared from ontology.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getParticipants(String raceName) {

        List<String> participants = new ArrayList<>();

        OWLNamedIndividual race =
                dataFactory.getOWLNamedIndividual(
                        IRI.create(baseIri + "#" + raceName)
                );

        OWLObjectProperty hasParticipant =
                dataFactory.getOWLObjectProperty(
                        IRI.create(baseIri + "#hasParticipant")
                );

        for (OWLObjectPropertyAssertionAxiom ax :
                ontology.getObjectPropertyAssertionAxioms(race)) {

            if (ax.getProperty().asOWLObjectProperty().equals(hasParticipant)) {

                participants.add(
                        ax.getObject()
                                .asOWLNamedIndividual()
                                .getIRI()
                                .getShortForm()
                );
            }
        }

        return participants;
    }
    public boolean isInferredInstanceOf(
            String individualName,
            String className
    ) {
        OWLNamedIndividual individual =
                dataFactory.getOWLNamedIndividual(
                        IRI.create(baseIri + "#" + individualName)
                );

        OWLClass owlClass =
                dataFactory.getOWLClass(
                        IRI.create(baseIri + "#" + className)
                );

        return reasoner
                .getTypes(individual, false)
                .containsEntity(owlClass);
    }

    public String getInferredRaceType(String raceName) {

        if (isInferredInstanceOf(raceName, "WetRace")) {
            return "WetRace";
        }

        if (isInferredInstanceOf(raceName, "DryRace")) {
            return "DryRace";
        }

        return "UnknownRace";
    }
}