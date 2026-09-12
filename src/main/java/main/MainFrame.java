package main;

import agents.StrategyAgent;
import database.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private JComboBox<String> raceComboBox;
    private JButton runButton;
    private JTextArea resultArea;
    private JButton historyButton;
    private JButton clearButton;
    private JButton ontologyButton;
    private JButton clearOntologyButton;

    public MainFrame() {
        setTitle("F1 Strategy Advisor");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        attachListeners();
    }

    private void initComponents() {
        raceComboBox = new JComboBox<>(new String[]{"Race1", "Race2", "Race3", "Race4", "Race5"});
        runButton = new JButton("Run Strategy");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        historyButton = new JButton("Show History");

        ontologyButton = new JButton("Show Ontology");
        clearOntologyButton = new JButton("Clear Ontology");
    }

    private void layoutComponents() {
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Select Race:"));
        topPanel.add(raceComboBox);
        topPanel.add(runButton);

        clearButton = new JButton("Clear History");
        topPanel.add(clearButton);

        JScrollPane scrollPane = new JScrollPane(resultArea);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        topPanel.add(historyButton);
        topPanel.add(ontologyButton);
        topPanel.add(clearOntologyButton);


    }

    private void attachListeners() {
        runButton.addActionListener(e -> {
            String selectedRace = (String) raceComboBox.getSelectedItem();

            resultArea.setText("Running agents for " + selectedRace + "...\n");

            LiveDashboardFrame dashboard =
                    new LiveDashboardFrame();

            dashboard.setVisible(true);

            new Thread(() -> {
                StrategyAgent.lastResult = "Processing...";
                Launcher.launchAgents(selectedRace);


                SwingUtilities.invokeLater(() -> resultArea.setText(StrategyAgent.lastResult));
            }).start();
        });
        historyButton.addActionListener(ev -> {
            HistoryFrame historyFrame = new HistoryFrame();
            historyFrame.setVisible(true);
        });
        clearButton.addActionListener(e -> {
            DatabaseManager db = new DatabaseManager();
            db.clearHistory();
            resultArea.setText("History cleared.");
        });
        ontologyButton.addActionListener(e -> {
            ontology.OntologyManager om = new ontology.OntologyManager();

            java.util.List<String> strategies = om.getAllStrategies();

            JTextArea area = new JTextArea(15, 60);
            area.setEditable(false);

            for (String strategy : strategies) {
                area.append(strategy + "\n");
            }

            JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(area),
                    "Ontology Strategies",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        clearOntologyButton.addActionListener(e -> {
            ontology.OntologyManager om = new ontology.OntologyManager();
            om.clearGeneratedStrategies();

            JOptionPane.showMessageDialog(
                    this,
                    "Ontology strategies cleared!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}