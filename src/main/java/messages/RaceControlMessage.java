package messages;

public class RaceControlMessage {

    public String type;
    public String raceName;

    public RaceControlMessage() {
    }

    public RaceControlMessage(
            String type,
            String raceName
    ) {
        this.type = type;
        this.raceName = raceName;
    }
}