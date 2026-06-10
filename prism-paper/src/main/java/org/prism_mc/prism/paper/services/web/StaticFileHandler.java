/*
 * prism
 *
 * Copyright (c) 2022 M Botsko (viveleroi)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.prism_mc.prism.paper.services.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StaticFileHandler implements HttpHandler {

    /**
     * Placeholder baked into the web build's asset/API URLs (via Vite's base) and replaced at
     * serve time with the configured base href. Must match the value in prism-web/vite.config.ts.
     */
    private static final String BASE_PATH_PLACEHOLDER = "/__PRISM_BASE_PATH__/";

    /**
     * Text content types whose bodies are rewritten to replace the base path placeholder.
     */
    private static final Set<String> REWRITABLE_TYPES = Set.of("text/html", "text/css", "application/javascript");

    /**
     * Supported file extensions mapped to their content types.
     */
    private static final Map<String, String> MIME_TYPES = Map.of(
        "html",
        "text/html",
        "css",
        "text/css",
        "js",
        "application/javascript",
        "json",
        "application/json",
        "png",
        "image/png",
        "svg",
        "image/svg+xml",
        "woff2",
        "font/woff2",
        "woff",
        "font/woff"
    );

    /**
     * The context prefix to strip from incoming requests (empty, or e.g. "/prism").
     */
    private final String prefix;

    /**
     * The base href injected into served HTML/JS/CSS in place of the placeholder (e.g. "/" or "/prism/").
     */
    private final String baseHref;

    /**
     * Constructor.
     *
     * @param prefix The context prefix to strip from request paths
     * @param baseHref The base href to inject into served text assets
     */
    public StaticFileHandler(String prefix, String baseHref) {
        this.prefix = prefix;
        this.baseHref = baseHref;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        // Strip the configured base prefix so resources resolve against the web root regardless of
        // the public sub-path the app is hosted under.
        if (!prefix.isEmpty() && path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }

        if (path.isEmpty()) {
            path = "/";
        }

        // Reject path traversal attempts before resolving the classpath resource.
        if (path.contains("..")) {
            sendNotFound(exchange);
            return;
        }

        // Default to index.html for the root and extensionless client-side (SPA) routes.
        if ("/".equals(path) || !path.contains(".")) {
            path = "/index.html";
        }

        String resourcePath = "web" + path;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                sendNotFound(exchange);
                return;
            }

            String ext = path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ENGLISH);
            String contentType = MIME_TYPES.getOrDefault(ext, "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] content = is.readAllBytes();

            // Rewrite the base path placeholder in text assets to the configured base href.
            if (REWRITABLE_TYPES.contains(contentType)) {
                content = new String(content, StandardCharsets.UTF_8)
                    .replace(BASE_PATH_PLACEHOLDER, baseHref)
                    .getBytes(StandardCharsets.UTF_8);
            }

            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }
    }

    /**
     * Send a plain 404 Not Found response.
     *
     * @param exchange The HTTP exchange
     * @throws IOException If the response cannot be written
     */
    private void sendNotFound(HttpExchange exchange) throws IOException {
        byte[] body = "Not Found".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
