/*
 * This file is part of LuckPerms, licensed under the MIT License.
 * It has been modified for use in Prism.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package network.darkhelmet.prism.loader.services.dependencies;

import com.google.common.collect.ImmutableList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import network.darkhelmet.prism.loader.services.dependencies.relocation.Relocation;

public enum Dependency {
    ASM(
        "org.ow2.asm",
        "asm",
        "9.3",
        "EmM2m1ninJQ5GN4R1tYVLi7GCFzmPlcQUW+MZ9No5Lw="
    ),
    ASM_COMMONS(
        "org.ow2.asm",
        "asm-commons",
        "9.3",
        "o0fCRzLbKurRBrblmWoBWwaj74bnkKT3W2F2Hw0vfzk="
    ),
    JAR_RELOCATOR(
        "me.lucko",
        "jar-relocator",
        "1.5",
        "0D6eM99gKpEYFNDydgnto3Df0ygZGdRVqy5ahtj0oIs="
    ),

    ADVENTURE(
        "net.kyori",
        "adventure-api",
        "4.11.0",
        "0Qi0I7JqoMraFpx+/jF+WNA5vcd/5hMgD3mhAC6IaWU=",
        Relocation.of("adventure", "net{}kyori{}adventure"),
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    ADVENTURE_KEY(
        "net.kyori",
        "adventure-key",
        "4.11.0",
        "cOLFKF2lI0NYV1sGWS76PCuWScqCzWxhdMEPtvboIRU=",
        Relocation.of("adventure", "net{}kyori{}adventure"),
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    ADVENTURE_MINIMESSAGE(
        "net.kyori",
        "adventure-text-minimessage",
        "4.11.0",
        "1TBLc3uqrxeqLPkbHGy3zocRvkX97Waj46Hv3mLyzUw=",
        Relocation.of("adventure", "net{}kyori{}adventure"),
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    ADVENTURE_NBT(
        "net.kyori",
        "adventure-nbt",
        "4.11.0",
        "oGVbavmJbQrqeURpK1SMMGFHoATdMgi9ttNUegGJGPA=",
        Relocation.of("adventure", "net{}kyori{}adventure"),
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    ADVENTURE_PLATFORM(
        "net.kyori",
        "adventure-platform-api",
        "4.1.1",
        "spp6R94hDKkDyrW13o+bMavb7ZrcMH+lTqAFuc6rZuA=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    ADVENTURE_PLATFORM_FACET(
        "net.kyori",
        "adventure-platform-facet",
        "4.1.1",
        "WimYWl49bJwJI8QZVX55w79RKT25zJdwagYatcOidV0=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    ADVENTURE_PLATFORM_BUKKIT(
        "net.kyori",
        "adventure-platform-bukkit",
        "4.1.2",
        "zyChYbTEvOws4Pk2oVEyt4JkTsWhGAMUhQLdNklURo0=",
        Relocation.of("adventure", "net{}kyori{}adventure"),
        Relocation.of("viaversion", "com.viaversion")
    ),
    ADVENTURE_SERIALIZER_GSON(
        "net.kyori",
        "adventure-text-serializer-gson",
        "4.11.0",
        "gT2pfqvYUZfF+xJDT+KD93QFRkxi6we7v970o1nK1Jo=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    ADVENTURE_SERIALIZER_GSON_LEGACY(
        "net.kyori",
        "adventure-text-serializer-gson-legacy-impl",
        "4.11.0",
        "KWEk1YyDaqqM+TUD+fs+AKRHeBKmmkeQC/tiYda+Qf4=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    ADVENTURE_SERIALIZER_PLAIN(
        "net.kyori",
        "adventure-text-serializer-plain",
        "4.11.0",
        "M7IZ8Z92Qo2SpMhbRiA8XRuZgv1jta/t7MJBG7df3vw=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    ADVENTURE_SERIALIZER_LEGACY(
        "net.kyori",
        "adventure-text-serializer-legacy",
        "4.11.0",
        "T449DQv6Llbpe2OGJ78VJoSnnC+b/ZV6Xens9yuylUE=",
        Relocation.of("adventure", "net{}kyori{}adventure")
    ),
    AOPALLIANCE(
        "aopalliance",
        "aopalliance",
        "1.0",
        "Ct3sZw/tzT8RPFyAkdeDKA0j9146y4QbYanNsHk3agg=",
        Relocation.of("aopalliance", "org{}aopalliance")
    ),
    CAFFEINE(
        "com.github.ben-manes.caffeine",
        "caffeine",
        "3.1.1",
        "q8BiKm8//GDjSga8zTNLcM+pa3vngk5zQjlERk34zPY=",
        Relocation.of("caffeine", "com{}github{]ben-manes{}caffeine")
    ),
    CONFIGURATE_CORE(
        "org.spongepowered",
        "configurate-core",
        "4.1.2",
        "vWp7rosBzMzCQRn22zL1sb98oz3mWG9tm47NJq/hRqw=",
        Relocation.of("configurate", "org{}spongepowered{]configurate"),
        Relocation.of("geantyref", "io{}leangen{}geantyref")
    ),
    CONFIGURATE_HOCON(
        "org.spongepowered",
        "configurate-hocon",
        "4.1.2",
        "wkQQ0cS/Z4oelsXptHzs4wezcXDSMaQeSheHxCyTUF8=",
        Relocation.of("configurate", "org{}spongepowered{}configurate"),
        Relocation.of("hocon", "com{}typesafe{}config")
    ),
    EXAMINATION_API(
        "net.kyori",
        "examination-api",
        "1.3.0",
        "ySN//ssFQo9u/4YhYkascM4LR7BMCOp8o1Ag/eV/hJI=",
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    EXAMINATION_STRING(
        "net.kyori",
        "examination-string",
        "1.3.0",
        "fQH8JaS7OvDhZiaFRV9FQfv0YmIW6lhG5FXBSR4Va4w=",
        Relocation.of("examination", "net{}kyori{}examination")
    ),
    GEANTYREF(
        "io{}leangen{}geantyref",
        "geantyref",
        "1.3.13",
        "1t8GCrD79sWLDcgUViKrd0vzv5WL0/62d0zI0851bto=",
        Relocation.of("geantyref", "io{}leangen{}geantyref")
    ),
    GUICE(
        "com{}google{}inject",
        "guice",
        "5.1.0",
        "QTDlC/rEgJnIYPDZA7kYYMgaJJyQ84JF+P7Vj8gXvCY=",
        Relocation.of("inject", "com{}google{}inject"),
        Relocation.of("aopalliance", "org{}aopalliance")
    ),
    GUICE_ASSISTED(
        "com{}google{}inject{}extensions",
        "guice-assistedinject",
        "5.1.0",
        "b4dThU0xXaLrO5LhJKgmgtYpLNc63vEyndjsISUzPtM=",
        Relocation.of("inject", "com{}google{}inject")
    ),
    H2_DRIVER(
        "com.h2database",
        "h2",
        "2.1.214",
        "1iPNwPYdIYz1SajQnxw5H/kQlhFrIuJHVHX85PvnK9A=",
        Relocation.of("h2", "com{}h2database")
    ),
    HIKARI(
        "com.zaxxer",
        "HikariCP",
        "5.0.1",
        "JtSSOX5ndbQpZzeokZvwQEev5YJ/3SwItFV1lUNrOis=",
        Relocation.of("hikari", "com{}zaxxer{}hikari")
    ),
    HOCON_CONFIG(
        "com{}typesafe",
        "config",
        "1.4.2",
        "AHbCSbQ4fYNpFGUo/V2ss++6CY3ALs+ayB3r38LhL9U=",
        Relocation.of("hocon", "com{}typesafe{}config")
    ),
    JACKSON_ANNOTATIONS(
        "com{}fasterxml{}jackson{}core",
        "jackson-annotations",
        "2.13.3",
        "Uyam+83nz4gX82wlQQHNRfas6kJYUYzTyA7luJ9OS5s=",
        Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    JACKSON_CORE(
        "com{}fasterxml{}jackson{}core",
        "jackson-core",
        "2.13.3",
        "qxGajqPMaUcuvA6HC4Sb+75TatV9YT3DhFPM1ZLKaj0=",
        Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    JACKSON_DATABIND(
        "com{}fasterxml{}jackson{}core",
        "jackson-databind",
        "2.13.3",
        "ZES/CNjNRil0Cvw9sSdpOPSUco3rZjzlhcTpH2tF64Q=",
        Relocation.of("jackson", "com{}fasterxml{}jackson")
    ),
    JOOQ(
        "org.jooq",
        "jooq",
        "3.17.2",
        "LjpF3/2ndgUP1XMiiWc6E866n769+n6wmkET4QpVuHg=",
        Relocation.of("jooq", "org{}jooq"),
        Relocation.of("reactivestreams", "org{}reactivestreams"),
        Relocation.of("r2dbc", "org{}r2dbc")
    ),
    MARIADB_DRIVER(
        "org.mariadb.jdbc",
        "mariadb-java-client",
        "3.0.6",
        "l3ynmAt3e1qo0yZ4IEKWoQjz6svE8hCIfjmxmGn60NM=",
        Relocation.of("mariadb", "org{}mariadb")
    ),
    MOONSHINE_CORE(
        "net{}kyori{}moonshine",
        "moonshine-core",
        "2.0.4",
        "YB1EB6/u6yXplav0Gm7r1hEL5EE6LLCi4XKqmhlh3Fc=",
        Relocation.of("moonshine", "net{}kyori{}moonshine"),
        Relocation.of("geantyref", "io{}leangen{}geantyref")
    ),
    MOONSHINE_INTERNAL(
        "net{}kyori{}moonshine",
        "moonshine-internal",
        "2.0.4",
        "ChnFoz57fYxREjrmBZkHHodKP6VrFe70VWKUbn/QVII=",
        Relocation.of("moonshine", "net{}kyori{}moonshine")
    ),
    MOONSHINE_STANDARD(
        "net{}kyori{}moonshine",
        "moonshine-standard",
        "2.0.4",
        "tAqZea9MiMPsjZvXBIlVULvxBQlzGpHWSIgk+9yDGOE=",
        Relocation.of("moonshine", "net{}kyori{}moonshine"),
        Relocation.of("geantyref", "io{}leangen{}geantyref")
    ),
    MYSQL_DRIVER(
        "mysql",
        "mysql-connector-java",
        "8.0.29",
        "1OMtKmAmtazAAwC3OobCj7kmga6WKbIQSO5nAUyRHbY=",
        Relocation.of("mysql", "com{}mysql")
    ),
    NBT_API(
        "de.tr7zw",
        "item-nbt-api-plugin",
        "2.10.0",
        "LTEpEvChcqD2Fz7ddmcjthY2BRRPaow3/FB3+2/crKE=",
        EnumSet.of(DependencyRepository.CODEMC_DH, DependencyRepository.CODEMC),
        Relocation.of("nbtapi", "de{}tr7zw{}nbtapi"),
        Relocation.of("nbtinjector", "de.tr7zw.nbtinjector")
    ),
    P6SPY(
        "p6spy",
        "p6spy",
        "3.9.1",
        "GXqmSmZ22leNrBCXgkrM2nqJ1ashwIKeBb/ekqF29KE=",
        Relocation.of("p6spy", "com{}p6spy{}engine")
    ),
    POSTGRES_DRIVER(
        "org.postgresql",
        "postgresql",
        "42.4.0",
        "/iW5wKLFlFhQTsiIYoU99SLuh/igJWSDXVN8Ka5MsSU=",
        Relocation.of("postgresql", "org{}postgresql")
    ),
    // Jooq dep
    R2DBC(
        "io.r2dbc",
        "r2dbc-spi",
        "1.0.0.RELEASE",
        "pYRsWf6jNkMaSucsoU7b9SmbeEhvowjq+zg/SuDqdOU=",
        Relocation.of("r2dbc", "org{}r2dbc")
    ),
    // Jooq dep
    REACTIVE_STREAMS(
        "org.reactivestreams",
        "reactive-streams",
        "1.0.4",
        "91yll3ibPaxY9hhXuawuEDSmj6Zy2zUFWo+0UJ4yXyg=",
        Relocation.of("reactivestreams", "org{}reactivestreams")
    ),
    TASKCHAIN_BUKKIT(
        "co.aikar",
        "taskchain-bukkit",
        "3.7.2",
        "B/O3+zWGalLs8otAr8tdNnIc/39FDRh6tN5qvNgfEaI=",
        EnumSet.of(DependencyRepository.AIKAR_DH, DependencyRepository.AIKAR),
        Relocation.of("taskchain", "co{}aikar{}taskchain")
    ),
    TASKCHAIN_CORE(
        "co.aikar",
        "taskchain-core",
        "3.7.2",
        "OpSCCN+7v6gqFpsU/LUNOOXzjImwjyE2ShHZ5xFUj/Q=",
        EnumSet.of(DependencyRepository.AIKAR_DH, DependencyRepository.AIKAR),
        Relocation.of("taskchain", "co{}aikar{}taskchain")
    );

    private final String mavenRepoPath;
    private final String version;
    private final byte[] checksum;
    private final Set<DependencyRepository> repositories;
    private final List<Relocation> relocations;
    private static final String MAVEN_FORMAT = "%s/%s/%s/%s-%s.jar";

    Dependency(String groupId, String artifactId, String version, String checksum) {
        this(groupId, artifactId, version, checksum, new Relocation[0]);
    }

    Dependency(String groupId, String artifactId, String version, String checksum, Relocation... relocations) {
        this(groupId, artifactId, version, checksum,
            EnumSet.of(
                DependencyRepository.LUCK_MIRROR,
                DependencyRepository.MAVEN_CENTRAL), relocations);
    }

    Dependency(
            String groupId,
            String artifactId,
            String version,
            String checksum,
            Set<DependencyRepository> repositories,
            Relocation... relocations) {
        this.mavenRepoPath = String.format(MAVEN_FORMAT,
                rewriteEscaping(groupId).replace(".", "/"),
                rewriteEscaping(artifactId),
                version,
                rewriteEscaping(artifactId),
                version
        );
        this.version = version;
        this.checksum = Base64.getDecoder().decode(checksum);
        this.repositories = repositories;
        this.relocations = ImmutableList.copyOf(relocations);
    }

    /**
     * Rewrites escaping.
     * <p>Some patterns are escaped to avoid being touched by shade relocation. It might rewrite
     * the rules and break our custom remapping.</p>
     *
     * @param s The encoded package string
     * @return The java package
     */
    private static String rewriteEscaping(String s) {
        return s.replace("{}", ".");
    }

    /**
     * Format a filename.
     *
     * @param classifier The classifier (remapped or null)
     * @return The filename
     */
    public String fileName(String classifier) {
        String name = name().toLowerCase(Locale.ROOT).replace('_', '-');
        String extra = classifier == null || classifier.isEmpty() ? "" : "-" + classifier;

        return name + "-" + this.version + extra + ".jar";
    }

    /**
     * Get the maven repo path.
     *
     * @return The maven repo path
     */
    String mavenRepoPath() {
        return this.mavenRepoPath;
    }

    /**
     * Get the expected checksum.
     *
     * @return The checksum
     */
    public byte[] checksum() {
        return this.checksum;
    }

    /**
     * Compare checksums.
     *
     * @param hash The file hash
     * @return True if hashes match
     */
    public boolean checksumMatches(byte[] hash) {
        return Arrays.equals(this.checksum, hash);
    }

    /**
     * Get the repositories.
     *
     * @return The repositories
     */
    public Set<DependencyRepository> repositories() {
        return repositories;
    }

    /**
     * Get the relocations.
     *
     * @return The relocations
     */
    public List<Relocation> relocations() {
        return this.relocations;
    }

    /**
     * Creates a {@link MessageDigest} suitable for computing the checksums
     * of dependencies.
     *
     * @return the digest
     */
    public static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}