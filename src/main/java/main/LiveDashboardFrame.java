package main;

import javax.swing.*;
import java.awt.*;

public class LiveDashboardFrame extends JFrame {

    private static LiveDashboardFrame instance;

    private final JLabel raceLabel = new JLabel();
    private final JLabel driverLabel = new JLabel();
    private final JLabel teamLabel = new JLabel();
    private final JLabel lapLabel = new JLabel();
    private final JLabel raceTimeLabel = new JLabel();
    private final JLabel lastLapLabel = new JLabel();
    private final JLabel fastestLapLabel = new JLabel();

    private JProgressBar fuelBar;
    private JProgressBar tyreWearBar;
    private JProgressBar gripBar;

    private final JLabel fuelLabel = new JLabel();
    private final JLabel tyreLabel = new JLabel();
    private final JLabel wearLabel = new JLabel();
    private final JLabel gripLabel = new JLabel();

    private final JLabel weatherLabel = new JLabel();
    private final JLabel rainLabel = new JLabel();

    private final JLabel speedLabel = new JLabel();
    private final JLabel rpmLabel = new JLabel();
    private final JLabel engineTempLabel = new JLabel();
    private final JLabel ersLabel = new JLabel();
    private final JLabel drsLabel = new JLabel();
    private final JLabel brakeLabel = new JLabel();

    private JLabel recommendedTyreLabel;
    private JLabel pitStopLabel;
    private JLabel reasonLabel;

    private JLabel statusLabel;


    public LiveDashboardFrame() {

        instance = this;

        setTitle("Live Race Dashboard");
        setSize(420, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(0, 1));

        statusLabel = new JLabel("STATUS : NORMAL");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(0, 170, 0));
        statusLabel.setForeground(Color.WHITE);

        add(statusLabel);
        addTitle("Race");

        add(raceLabel);
        add(driverLabel);
        add(teamLabel);
        add(lapLabel);
        add(raceTimeLabel);
        add(lastLapLabel);
        add(fastestLapLabel);

        addTitle("Car");

        fuelBar = new JProgressBar(0, 100);
        fuelBar.setValue(100);
        fuelBar.setStringPainted(true);

        add(fuelLabel);
        add(fuelBar);
        add(tyreLabel);
        tyreWearBar = new JProgressBar(0, 100);
        tyreWearBar.setValue(0);
        tyreWearBar.setStringPainted(true);

        gripBar = new JProgressBar(0, 100);
        gripBar.setValue(100);
        gripBar.setStringPainted(true);


        add(tyreWearBar);
        add(wearLabel);
        add(gripLabel);
        add(gripBar);

        addTitle("Weather");

        add(weatherLabel);
        add(rainLabel);

        addTitle("Telemetry");

        JLabel strategyTitle = new JLabel("AI Strategy");
        strategyTitle.setFont(new Font("Arial", Font.BOLD, 18));
        strategyTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(strategyTitle);

        add(speedLabel);
        add(rpmLabel);
        add(engineTempLabel);
        add(ersLabel);
        add(drsLabel);
        add(brakeLabel);

        recommendedTyreLabel = new JLabel("Recommended Tyre : -");
        pitStopLabel = new JLabel("Pit Stop : -");
        reasonLabel = new JLabel("Reason : -");


        add(recommendedTyreLabel);
        add(pitStopLabel);
        add(reasonLabel);


    }

    public static LiveDashboardFrame getInstance() {
        return instance;
    }

    private void addTitle(String text) {

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);

        lbl.setFont(new Font("Arial", Font.BOLD, 18));

        add(lbl);
    }

    public void updateDashboard(
            String race,
            String driver,
            String team,
            int lap,
            int totalLaps,
            double fuel,
            String tyre,
            double wear,
            double grip,
            String weather,
            double rain,
            double speed,
            int rpm,
            double engineTemp,
            double ers,
            boolean drs,
            double brakeTemp,
            double lastLapTime,
            double fastestLapTime
    ) {
        SwingUtilities.invokeLater(() -> {
            raceLabel.setText("Race : " + race);
            driverLabel.setText("Driver : " + driver);
            teamLabel.setText("Team : " + team);
            lapLabel.setText("Lap : " + lap + " / " + totalLaps);

            fuelLabel.setText(
                    "Fuel : " +
                            String.format("%.1f", fuel) +
                            " %"
            );

            fuelBar.setValue((int) fuel);
            fuelBar.setString(String.format("%.1f %%", fuel));

            if (fuel > 50.0) {
                fuelBar.setForeground(new Color(0, 170, 0));
            } else if (fuel > 20.0) {
                fuelBar.setForeground(Color.ORANGE);
            } else {
                fuelBar.setForeground(Color.RED);
            }

            tyreLabel.setText("Tyre : " + tyre);

           // wearLabel.setText(
             //       "Tyre Wear : " +
               //             String.format("%.1f", wear) +
                 //           " %"
            //);

            tyreWearBar.setValue((int) wear);
            tyreWearBar.setString(
                    String.format("%.1f %%", wear)
            );

            if (wear < 50.0) {
                tyreWearBar.setForeground(new Color(0, 170, 0));
            } else if (wear < 75.0) {
                tyreWearBar.setForeground(Color.ORANGE);
            } else {
                tyreWearBar.setForeground(Color.RED);
            }

            gripLabel.setText(
                    "Grip : " +
                            String.format("%.1f", grip) +
                            " %"
            );

            gripBar.setValue((int) grip);
            gripBar.setString(String.format("%.1f %%", grip));

            if (grip > 60.0) {
                gripBar.setForeground(new Color(0, 170, 0));
            } else if (grip > 35.0) {
                gripBar.setForeground(Color.ORANGE);
            } else {
                gripBar.setForeground(Color.RED);
            }

            weatherLabel.setText("Weather : " + weather);

            rainLabel.setText(
                    "Rain : " +
                            String.format("%.1f", rain) +
                            " %"
            );

            speedLabel.setText(
                    "Speed : " +
                            String.format("%.1f", speed) +
                            " km/h"
            );

            rpmLabel.setText("RPM : " + rpm);

            engineTempLabel.setText(
                    "Engine Temp : " +
                            String.format("%.1f", engineTemp) +
                            " °C"
            );

            ersLabel.setText(
                    "ERS : " +
                            String.format("%.1f", ers) +
                            " %"
            );

            drsLabel.setText(
                    "DRS : " +
                            (drs ? "Enabled" : "Disabled")
            );

            brakeLabel.setText(
                    "Brake Temp : " +
                            String.format("%.1f", brakeTemp) +
                            " °C"
            );

            lastLapLabel.setText(
                    "Last Lap : " + formatLapTime(lastLapTime)
            );

            fastestLapLabel.setText(
                    "Fastest Lap : " + formatLapTime(fastestLapTime)
            );

        });
    }

    public void updateStrategy(
            String tyre,
            boolean pitRequired,
            String reason
    ) {

        SwingUtilities.invokeLater(() -> {

            recommendedTyreLabel.setText(
                    "Recommended Tyre : " + tyre
            );

            pitStopLabel.setText(
                    "Pit Stop : " + (pitRequired ? "YES" : "NO")
            );

            reasonLabel.setText(
                    "Reason : " + reason
            );


        });

    }

    public void updateStatus(boolean pitRequired) {

        SwingUtilities.invokeLater(() -> {

            if (pitRequired) {

                statusLabel.setText("STATUS : PIT NOW");
                statusLabel.setBackground(Color.RED);

            } else {

                statusLabel.setText("STATUS : NORMAL");
                statusLabel.setBackground(new Color(0,170,0));

            }

        });

    }

    public void updateRaceTime(long seconds) {

        SwingUtilities.invokeLater(() -> {

            long minutes = seconds / 60;
            long secs = seconds % 60;

            raceTimeLabel.setText(
                    "Race Time : " +
                            String.format("%02d:%02d", minutes, secs)
            );

        });

    }

    private String formatLapTime(double totalSeconds) {

        if (totalSeconds <= 0 ||
                totalSeconds == Double.MAX_VALUE) {
            return "--:--.---";
        }

        int minutes = (int) (totalSeconds / 60);
        double seconds = totalSeconds % 60;

        return String.format(
                "%d:%06.3f",
                minutes,
                seconds
        );
    }

}