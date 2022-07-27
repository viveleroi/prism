package network.darkhelmet.prism.loader.services.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;

import network.darkhelmet.prism.api.services.configuration.IModificationConfiguration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class ModificationConfiguration implements IModificationConfiguration {
    @Comment("List materials that should be excluded from modifications.")
    @Getter
    private List<String> blockBlacklist = new ArrayList<>();

    /**
     * Constructor.
     */
    public ModificationConfiguration() {
        blockBlacklist.add("bedrock");
        blockBlacklist.add("tnt");
    }

    @Override
    public boolean blockBlacklistContainsAny(String... values) {
        return blockBlacklist.stream().anyMatch(str ->
            Arrays.stream(values).anyMatch(v -> v.equalsIgnoreCase(str)));
    }
}
