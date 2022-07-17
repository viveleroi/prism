package network.darkhelmet.prism.idb;

public interface DatabaseTiming extends AutoCloseable {
    DatabaseTiming startTiming();

    void stopTiming();

    default void close() {
        this.stopTiming();
    }
}
