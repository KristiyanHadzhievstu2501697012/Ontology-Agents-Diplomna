package simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class SimulationRandom {

    private static final int VALUES_PER_STREAM = 100_000;

    private static long seed = 1L;


    private static Random random = new Random(seed);

    private static final Map<String, double[]> streams =
            new HashMap<>();

    private static final Map<String, Integer> positions =
            new HashMap<>();

    private SimulationRandom() {
    }

    static {
        initializeStreams();
    }

    public static synchronized void setSeed(long newSeed) {

        seed = newSeed;
        random = new Random(seed);

        initializeStreams();
    }

    public static synchronized void reset() {

        random = new Random(seed);

        initializeStreams();
    }

    public static synchronized long getSeed() {
        return seed;
    }

    private static void initializeStreams() {

        streams.clear();
        positions.clear();

        createStream("RACE");
        createStream("WEATHER");
        createStream("TELEMETRY");
        createStream("BASELINE_RANDOM");
    }

    private static void createStream(String name) {

        double[] values =
                new double[VALUES_PER_STREAM];

        for (int i = 0; i < VALUES_PER_STREAM; i++) {
            values[i] = random.nextDouble();
        }

        streams.put(name, values);
        positions.put(name, 0);
    }

    private static double nextValue(String streamName) {

        double[] values =
                streams.get(streamName);

        if (values == null) {
            throw new IllegalArgumentException(
                    "Unknown random stream: " + streamName
            );
        }

        int position =
                positions.get(streamName);

        if (position >= values.length) {
            throw new IllegalStateException(
                    "Random stream exhausted: " + streamName
            );
        }

        positions.put(
                streamName,
                position + 1
        );

        return values[position];
    }

    public static synchronized double nextDouble(
            String streamName,
            double origin,
            double bound
    ) {

        double value =
                nextValue(streamName);

        return origin
                + (bound - origin) * value;
    }

    public static synchronized int nextInt(
            String streamName,
            int origin,
            int bound
    ) {

        if (bound <= origin) {
            throw new IllegalArgumentException(
                    "Bound must be greater than origin."
            );
        }

        double value =
                nextValue(streamName);

        return origin
                + (int) (value * (bound - origin));
    }

    public static synchronized boolean nextBoolean(
            String streamName
    ) {

        return nextValue(streamName) >= 0.5;
    }
}