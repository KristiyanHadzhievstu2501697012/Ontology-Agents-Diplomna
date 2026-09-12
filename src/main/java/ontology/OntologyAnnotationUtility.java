package ontology;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.Set;

public class OntologyAnnotationUtility {

    private static final String ONTOLOGY_PATH =
            "./data/f1_ontology.owl";

    public static void main(String[] args) throws Exception {

        File ontologyFile = new File(ONTOLOGY_PATH);

        OWLOntologyManager manager =
                OWLManager.createOWLOntologyManager();

        OWLOntology ontology =
                manager.loadOntologyFromOntologyDocument(
                        ontologyFile
                );

        OWLDataFactory dataFactory =
                manager.getOWLDataFactory();

        OWLAnnotationProperty labelProperty =
                dataFactory.getRDFSLabel();

        OWLAnnotationProperty commentProperty =
                dataFactory.getRDFSComment();

        annotateEntities(
                manager,
                ontology,
                dataFactory,
                ontology.getClassesInSignature(),
                labelProperty,
                commentProperty,
                "Class"
        );

        annotateEntities(
                manager,
                ontology,
                dataFactory,
                ontology.getObjectPropertiesInSignature(),
                labelProperty,
                commentProperty,
                "Object property"
        );

        annotateEntities(
                manager,
                ontology,
                dataFactory,
                ontology.getDataPropertiesInSignature(),
                labelProperty,
                commentProperty,
                "Data property"
        );

        annotateEntities(
                manager,
                ontology,
                dataFactory,
                ontology.getIndividualsInSignature(),
                labelProperty,
                commentProperty,
                "Individual"
        );

        manager.saveOntology(
                ontology,
                IRI.create(ontologyFile)
        );

        System.out.println(
                "Ontology annotations completed successfully."
        );
    }

    private static void annotateEntities(
            OWLOntologyManager manager,
            OWLOntology ontology,
            OWLDataFactory dataFactory,
            Set<? extends OWLEntity> entities,
            OWLAnnotationProperty labelProperty,
            OWLAnnotationProperty commentProperty,
            String entityType
    ) {

        for (OWLEntity entity : entities) {

            String shortName =
                    entity.getIRI().getShortForm();

            boolean hasLabel =
                    ontology.annotationAssertionAxioms(
                                    entity.getIRI()
                            )
                            .anyMatch(
                                    axiom ->
                                            axiom.getProperty()
                                                    .equals(labelProperty)
                            );

            if (!hasLabel) {

                OWLAnnotation label =
                        dataFactory.getOWLAnnotation(
                                labelProperty,
                                dataFactory.getOWLLiteral(
                                        shortName,
                                        "en"
                                )
                        );

                OWLAnnotationAssertionAxiom labelAxiom =
                        dataFactory
                                .getOWLAnnotationAssertionAxiom(
                                        entity.getIRI(),
                                        label
                                );

                manager.addAxiom(
                        ontology,
                        labelAxiom
                );
            }

            boolean hasComment =
                    ontology.annotationAssertionAxioms(
                                    entity.getIRI()
                            )
                            .anyMatch(
                                    axiom ->
                                            axiom.getProperty()
                                                    .equals(commentProperty)
                            );

            if (!hasComment) {

                String commentText =
                        createComment(
                                shortName,
                                entityType
                        );

                OWLAnnotation comment =
                        dataFactory.getOWLAnnotation(
                                commentProperty,
                                dataFactory.getOWLLiteral(
                                        commentText,
                                        "en"
                                )
                        );

                OWLAnnotationAssertionAxiom commentAxiom =
                        dataFactory
                                .getOWLAnnotationAssertionAxiom(
                                        entity.getIRI(),
                                        comment
                                );

                manager.addAxiom(
                        ontology,
                        commentAxiom
                );
            }
        }
    }

    private static String createComment(
            String shortName,
            String entityType
    ) {

        return switch (entityType) {

            case "Class" ->
                    "Class representing " +
                            formatName(shortName) +
                            " in the Formula 1 strategy ontology.";

            case "Object property" ->
                    "Object property representing the relation " +
                            formatName(shortName) +
                            " in the Formula 1 strategy ontology.";

            case "Data property" ->
                    "Data property representing " +
                            formatName(shortName) +
                            " in the Formula 1 strategy ontology.";

            case "Individual" ->
                    "Individual representing " +
                            formatName(shortName) +
                            " in the Formula 1 strategy ontology.";

            default ->
                    "Entity used in the Formula 1 strategy ontology.";
        };
    }

    private static String formatName(
            String name
    ) {

        return name
                .replaceAll(
                        "([a-z])([A-Z])",
                        "$1 $2"
                )
                .replace('_', ' ')
                .toLowerCase();
    }
}