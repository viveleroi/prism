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

package network.darkhelmet.prism.loader.services.dependencies.relocation;

import java.util.Objects;

public final class Relocation {
    /**
     * The relocation destination.
     */
    private static final String RELOCATION_PREFIX = "network.darkhelmet.prism.libs.";

    /**
     * The pattern.
     */
    private final String pattern;

    /**
     * The relocated pattern.
     */
    private final String relocatedPattern;

    /**
     * Create a new relocation.
     *
     * @param id The id
     * @param pattern The pattern
     * @return The relocation
     */
    public static Relocation of(String id, String pattern) {
        return new Relocation(pattern.replace("{}", "."), RELOCATION_PREFIX + id);
    }

    /**
     * Constructor.
     *
     * @param pattern The pattern
     * @param relocatedPattern The pattern to relocate
     */
    private Relocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    /**
     * Get the pattern.
     *
     * @return The pattern
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * The relocated pattern.
     *
     * @return The relocated pattern
     */
    public String getRelocatedPattern() {
        return this.relocatedPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Relocation that = (Relocation) o;
        return Objects.equals(this.pattern, that.pattern)
            && Objects.equals(this.relocatedPattern, that.relocatedPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.pattern, this.relocatedPattern);
    }
}