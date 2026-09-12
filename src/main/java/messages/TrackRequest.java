package messages;

public class TrackRequest {

    public String type;
    public String raceName;

    public TrackRequest() {
    }

    public TrackRequest(String raceName) {
        this.type = "TRACK_REQUEST";
        this.raceName = raceName;
    }
}