/*import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class OffshoreProxy {
    private static final int SERVER_PORT = 9090;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Offshore proxy server listening on port " + SERVER_PORT);
            
            while (true) {
                Socket shipSocket = serverSocket.accept();
                executor.submit(() -> handleShipConnection(shipSocket));
            }
        } catch (IOException e) {
            System.err.println("Error in offshore proxy server: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

        private static void handleShipConnection(Socket shipSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(shipSocket.getInputStream()));
            PrintWriter out = new PrintWriter(shipSocket.getOutputStream(), true)
        ) {
            System.out.println("Ship connected: " + shipSocket.getRemoteSocketAddress());

            // Read full request until END_OF_REQUEST
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.equals("END_OF_REQUEST")) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString().trim();
            if (request.isEmpty()) {
                System.out.println("Empty request received. Ignoring.");
                return;
            }

            System.out.println("Processing request:\n" + request);

            // Extract method and URL
            String[] requestLines = request.split("\r\n");
            if (requestLines.length == 0) return;

            String[] requestParts = requestLines[0].split(" ");
            if (requestParts.length < 2) return;

            String method = requestParts[0];
            String url = requestParts[1];

            // Make actual HTTP request
            String response = makeHttpRequest(method, url, request);

            // Send response back
            out.println(response);
            out.println("END_OF_RESPONSE");
            out.flush();

        } catch (IOException e) {
            System.err.println("Error handling ship connection: " + e.getMessage());
        } finally {
            try {
                shipSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing socket: " + ex.getMessage());
            }
        }
    }


    private static String makeHttpRequest(String method, String url, String originalRequest) {
        try {
            URI uri = new URI(url.startsWith("http") ? url : "http://" + url);
            URL parsedUrl = uri.toURL();
            System.out.println(parsedUrl);
            HttpURLConnection connection = (HttpURLConnection) parsedUrl.openConnection();
            connection.setRequestMethod(method);

            // Copy headers from original request
            String[] lines = originalRequest.split("\r\n");
            for (int i = 1; i < lines.length; i++) {
                String[] header = lines[i].split(": ", 2);
                if (header.length == 2) {
                    connection.setRequestProperty(header[0], header[1]);
                }
            }
            
            // Get response
            int responseCode = connection.getResponseCode();
            System.out.println("responseCode "+responseCode);
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 ").append(responseCode).append(" ").append(connection.getResponseMessage()).append("\r\n");
            
            // Copy response headers
            connection.getHeaderFields().forEach((key, values) -> {
                if (key != null) {
                    values.forEach(value -> {
                        response.append(key).append(": ").append(value).append("\r\n");
                    });
                }
            });
            response.append("\r\n");
            
            // Copy response body
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode < 400 ? connection.getInputStream() : connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\r\n");
                }
            }
            return response.toString();
        } catch (Exception e) {
            return "HTTP/1.1 500 Internal Server Error\r\n\r\nError processing request: " + e.getMessage();
        }
    }
}*/

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class OffshoreProxy {
    private static final int SERVER_PORT = 9090;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Offshore proxy server listening on port " + SERVER_PORT);
            while (true) {
                Socket shipSocket = serverSocket.accept();
                executor.submit(() -> handleShipConnection(shipSocket));
            }
        } catch (IOException e) {
            System.err.println("Error in offshore proxy server: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static void handleShipConnection(Socket shipSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(shipSocket.getInputStream()));
            PrintWriter out = new PrintWriter(shipSocket.getOutputStream(), true)
        ) {
            System.out.println("Ship connected: " + shipSocket.getRemoteSocketAddress());

            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.equals("END_OF_REQUEST")) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString().trim();
            if (request.isEmpty()) return;

            System.out.println("Processing request:\n" + request);

            String[] requestLines = request.split("\r\n");
            if (requestLines.length == 0) return;

            String[] requestParts = requestLines[0].split(" ");
            if (requestParts.length < 2) return;

            String method = requestParts[0];
            String url = requestParts[1];

            String response = makeHttpRequest(method, url, request);

            out.println(response);
            out.println("END_OF_RESPONSE");
            out.flush();

        } catch (Exception e) {
            System.err.println("Error handling ship connection: " + e.getMessage());
        } finally {
            try {
                shipSocket.close();
            } catch (IOException ex) {
                System.err.println("Error closing ship socket: " + ex.getMessage());
            }
        }
    }

    private static String makeHttpRequest(String method, String url, String originalRequest) {
        try {
            URI uri = new URI(url.startsWith("http") ? url : "http://" + url);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod(method);

            String[] lines = originalRequest.split("\r\n");
            for (int i = 1; i < lines.length; i++) {
                String[] header = lines[i].split(": ", 2);
                if (header.length == 2) {
                    connection.setRequestProperty(header[0], header[1]);
                }
            }

            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            response.append("HTTP/1.1 ").append(responseCode).append(" ").append(connection.getResponseMessage()).append("\r\n");

            connection.getHeaderFields().forEach((key, values) -> {
                if (key != null) {
                    values.forEach(value -> {
                        response.append(key).append(": ").append(value).append("\r\n");
                    });
                }
            });

            response.append("\r\n");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    responseCode < 400 ? connection.getInputStream() : connection.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line).append("\r\n");
                }
            }

            return response.toString();

        } catch (Exception e) {
            return "HTTP/1.1 500 Internal Server Error\r\n\r\nError processing request: " + e.getMessage();
        }
    }
}
