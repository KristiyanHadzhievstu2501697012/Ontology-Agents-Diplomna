package main;

import javax.swing.*;
import java.awt.*;

public class RaceResultFrame extends JFrame {

    public RaceResultFrame(
            String race,
            String driver,
            int totalLaps,
            String raceTime,
            int pitStops,
            String finalTyre,
            double averageSpeed,
            double maxSpeed
    ) {

        setTitle("Race Results");

        setSize(420, 430);

        setLocationRelativeTo(null);

        setLayout(new GridLayout(0,1));

        JLabel title = new JLabel("RACE FINISHED", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        add(title);

        add(new JLabel("Race : " + race));
        add(new JLabel("Driver : " + driver));
        add(new JLabel("Total Laps : " + totalLaps));
        add(new JLabel("Race Time : " + raceTime));
        add(new JLabel("Pit Stops : " + pitStops));
        add(new JLabel("Final Tyre : " + finalTyre));

        add(new JLabel(
                "Average Speed : " +
                        String.format("%.1f km/h", averageSpeed)
        ));

        add(new JLabel(
                "Maximum Speed : " +
                        String.format("%.1f km/h", maxSpeed)
        ));

        JButton ok = new JButton("OK");

        ok.addActionListener(e -> dispose());

        add(ok);

        setVisible(true);
    }
}