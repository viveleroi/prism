package network.darkhelmet.prism.api.services.configuration;

public interface IModificationConfiguration {
    /**
     * Check if the block black list matches any strings.
     *
     * @param strings The strings
     * @return True if any found in black list
     */
    boolean blockBlacklistContainsAny(String... strings);
}
