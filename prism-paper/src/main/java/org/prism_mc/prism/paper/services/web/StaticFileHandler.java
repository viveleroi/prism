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

public class StaticFileHandler implements HttpHandler {

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

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

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
