package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:f1_strategy.db";

    public DatabaseManager() {
        createTableIfNotExists();
        addRaceHistoryColumnsIfMissing();
    }

    private Connection connect() throws Exception {
        return DriverManager.getConnection(DB_URL);
    }

    private void createTableIfNotExists() {
        String createSql = """
            CREATE TABLE IF NOT EXISTS race_strategy_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                race_name TEXT NOT NULL,
                circuit TEXT,
                driver TEXT,
                track_condition TEXT,
                weather TEXT,
                temperature INTEGER,
                laps INTEGER,
                recommended_tyre TEXT,
                pit_stops INTEGER
            );
            """;

        String createRaceHistorySql = """
    CREATE TABLE IF NOT EXISTS race_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        race_name TEXT NOT NULL,
        total_laps INTEGER,
        completed_laps INTEGER,
        completed_pit_stops INTEGER,
        final_fuel REAL,
        final_tyre TEXT,
        final_tyre_wear REAL,
        final_grip REAL,
        final_air_temperature REAL,
        final_track_temperature REAL,
        final_rain_probability REAL,
        finished_at TEXT DEFAULT CURRENT_TIMESTAMP
    );
    """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createSql);
            stmt.execute(createRaceHistorySql);

            System.out.println("Database tables are ready.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRaceHistoryColumnsIfMissing() {
        String[] alterStatements = {
                "ALTER TABLE race_history ADD COLUMN circuit TEXT",
                "ALTER TABLE race_history ADD COLUMN driver TEXT",
                "ALTER TABLE race_history ADD COLUMN team TEXT",
                "ALTER TABLE race_history ADD COLUMN track_condition TEXT",
                "ALTER TABLE race_history ADD COLUMN weather TEXT"
        };

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            for (String sql : alterStatements) {
                try {
                    stmt.executeUpdate(sql);
                } catch (Exception ignored) {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveStrategyResult(
            String raceName,
            String circuit,
            String driver,
            String trackCondition,
            String weather,
            int temperature,
            int laps,
            String recommendedTyre,
            int pitStops
    ) {
        String sql = """
        INSERT INTO race_strategy_results
        (race_name, circuit, driver, track_condition, weather, temperature, laps, recommended_tyre, pit_stops)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, raceName);
            pstmt.setString(2, circuit);
            pstmt.setString(3, driver);
            pstmt.setString(4, trackCondition);
            pstmt.setString(5, weather);
            pstmt.setInt(6, temperature);
            pstmt.setInt(7, laps);
            pstmt.setString(8, recommendedTyre);
            pstmt.setInt(9, pitStops);

            pstmt.executeUpdate();
            System.out.println("Strategy result saved to database.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveRaceHistory(
            String raceName,
            String circuit,
            String driver,
            String team,
            String trackCondition,
            String weather,
            int totalLaps,
            int completedLaps,
            int completedPitStops,
            double finalFuel,
            String finalTyre,
            double finalTyreWear,
            double finalGrip,
            double finalAirTemperature,
            double finalTrackTemperature,
            double finalRainProbability
    ) {
        String sql = """
       
                INSERT INTO race_history (
                                                    race_name,
                                                    circuit,
                                                    driver,
                                                    team,
                                                    track_condition,
                                                    weather,
                                                    total_laps,
                                                    completed_laps,
                                                    completed_pit_stops,
                                                    final_fuel,
                                                    final_tyre,
                                                    final_tyre_wear,
                                                    final_grip,
                                                    final_air_temperature,
                                                    final_track_temperature,
                                                    final_rain_probability
                                                )
                                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, raceName);
            pstmt.setString(2, circuit);
            pstmt.setString(3, driver);
            pstmt.setString(4, team);
            pstmt.setString(5, trackCondition);
            pstmt.setString(6, weather);

            pstmt.setInt(7, totalLaps);
            pstmt.setInt(8, completedLaps);
            pstmt.setInt(9, completedPitStops);

            pstmt.setDouble(10, Math.round(finalFuel * 100.0) / 100.0);
            pstmt.setString(11, finalTyre);
            pstmt.setDouble(12, Math.round(finalTyreWear * 100.0) / 100.0);
            pstmt.setDouble(13, Math.round(finalGrip * 100.0) / 100.0);
            pstmt.setDouble(14, finalAirTemperature);
            pstmt.setDouble(15, finalTrackTemperature);
            pstmt.setDouble(16, Math.round(finalRainProbability * 100.0) / 100.0);

            pstmt.executeUpdate();

            System.out.println(
                    "Completed race saved to race history."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printAllResults() {
        String sql = "SELECT * FROM race_strategy_results";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Race: " + rs.getString("race_name"));
                System.out.println("Track: " + rs.getString("track_condition"));
                System.out.println("Weather: " + rs.getString("weather"));
                System.out.println("Temperature: " + rs.getInt("temperature"));
                System.out.println("Laps: " + rs.getInt("laps"));
                System.out.println("Tyre: " + rs.getString("recommended_tyre"));
                System.out.println("Pit Stops: " + rs.getInt("pit_stops"));
                System.out.println("--------------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAllResultsAsString() {
        StringBuilder sb = new StringBuilder();

        String sql = "SELECT * FROM race_strategy_results";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                sb.append("Race: ").append(rs.getString("race_name")).append("\n");
                sb.append("Track: ").append(rs.getString("track_condition")).append("\n");
                sb.append("Weather: ").append(rs.getString("weather")).append("\n");
                sb.append("Temp: ").append(rs.getInt("temperature")).append("\n");
                sb.append("Laps: ").append(rs.getInt("laps")).append("\n");
                sb.append("Tyre: ").append(rs.getString("recommended_tyre")).append("\n");
                sb.append("PitStops: ").append(rs.getInt("pit_stops")).append("\n");
                sb.append("------------------------\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    public Object[][] getAllResultsAsTableData() {
        String sql = """
            SELECT race_name, circuit, driver, track_condition, weather, temperature, laps, recommended_tyre, pit_stops
            FROM race_strategy_results
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            java.util.List<Object[]> rows = new java.util.ArrayList<>();

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getString("race_name"),
                        rs.getString("circuit"),
                        rs.getString("driver"),
                        rs.getString("track_condition"),
                        rs.getString("weather"),
                        rs.getInt("temperature"),
                        rs.getInt("laps"),
                        rs.getString("recommended_tyre"),
                        rs.getInt("pit_stops")
                });
            }

            return rows.toArray(new Object[0][]);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Object[0][];
    }

    public void clearHistory() {
        String clearStrategyHistory =
                "DELETE FROM race_strategy_results";

        String clearRaceHistory =
                "DELETE FROM race_history";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(clearStrategyHistory);
            stmt.executeUpdate(clearRaceHistory);

            System.out.println("All history cleared.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object[][] getRaceHistoryTableData() {

        String sql = """
        
                SELECT
                             race_name,
                             circuit,
                             driver,
                             team,
                             track_condition,
                             weather,
                             total_laps,
                             completed_laps,
                             completed_pit_stops,
                             final_fuel,
                             final_tyre,
                             final_tyre_wear,
                             final_grip,
                             final_rain_probability,
                             finished_at
                         FROM race_history
        ORDER BY finished_at DESC
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            java.util.List<Object[]> rows = new java.util.ArrayList<>();

            while (rs.next()) {

                rows.add(new Object[]{
                        rs.getString("race_name"),
                        rs.getString("circuit"),
                        rs.getString("driver"),
                        rs.getString("team"),
                        rs.getString("track_condition"),
                        rs.getString("weather"),
                        rs.getInt("total_laps"),
                        rs.getInt("completed_laps"),
                        rs.getInt("completed_pit_stops"),
                        rs.getDouble("final_fuel"),
                        rs.getString("final_tyre"),
                        rs.getDouble("final_tyre_wear"),
                        rs.getDouble("final_grip"),
                        rs.getDouble("final_rain_probability"),
                        rs.getString("finished_at")
                });
            }

            return rows.toArray(new Object[0][]);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Object[0][];
    }
}