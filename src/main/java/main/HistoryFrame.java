package main;

import database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class HistoryFrame extends JFrame {

    public HistoryFrame() {
        setTitle("Race History");
        setSize(1500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        DatabaseManager db = new DatabaseManager();

        String[] columns = {
                "Race",
                "Circuit",
                "Driver",
                "Team",
                "Track Condition",
                "Weather",
                "Total Laps",
                "Completed Laps",
                "Pit Stops",
                "Final Fuel",
                "Final Tyre",
                "Tyre Wear",
                "Grip",
                "Rain Probability",
                "Finished At"
        };

        Object[][] data = db.getRaceHistoryTableData();

        JTable table = new JTable(data, columns);
        table.setRowHeight(26);
        table.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        table.getTableHeader().setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer numberRenderer =
                new DefaultTableCellRenderer();

        numberRenderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        for (int column = 1;
             column < table.getColumnCount();
             column++) {

            table.getColumnModel()
                    .getColumn(column)
                    .setCellRenderer(numberRenderer);
        }

        table.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(80);

        table.getColumnModel()
                .getColumn(5)
                .setPreferredWidth(120);

        table.getColumnModel()
                .getColumn(9)
                .setPreferredWidth(150);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);
        table.getColumnModel().getColumn(8).setPreferredWidth(90);
        table.getColumnModel().getColumn(9).setPreferredWidth(100);
        table.getColumnModel().getColumn(10).setPreferredWidth(110);
        table.getColumnModel().getColumn(11).setPreferredWidth(100);
        table.getColumnModel().getColumn(12).setPreferredWidth(90);
        table.getColumnModel().getColumn(13).setPreferredWidth(130);
        table.getColumnModel().getColumn(14).setPreferredWidth(160);

        JScrollPane scrollPane =
                new JScrollPane(table);

        JPanel titlePanel =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        JLabel titleLabel =
                new JLabel(
                        "Completed Race History"
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        titlePanel.add(titleLabel);

        setLayout(new BorderLayout());
        add(titlePanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}