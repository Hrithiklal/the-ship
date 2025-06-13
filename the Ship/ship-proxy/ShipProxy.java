import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.nio.charset.StandardCharsets; 
import java.util.concurrent.atomic.AtomicBoolean;

public class ShipProxy {
    private static final int CLIENT_PORT = 8080;
    private static final String SERVER_HOST = "offshore-proxy";
    //private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9090;
    private static final BlockingQueue<ProxyTask> requestQueue = new LinkedBlockingQueue<>();
    private static final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public static void main(String[] args) {
        startProxyServer();
    }

    private static void startProxyServer() {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(CLIENT_PORT)) {
            System.out.println("Ship proxy listening on port " + CLIENT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClientRequest(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Error in proxy server: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
           try {
                clientSocket.setSoTimeout(30000);
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStream out = clientSocket.getOutputStream()
            ) {
                StringBuilder requestBuilder = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    requestBuilder.append(line).append("\r\n");
                }

                String request = requestBuilder.toString();
                if (request.isEmpty()) return;

                //Socket clientSocket = serverSocket.accept();
                OutputStream clientOut = clientSocket.getOutputStream();
                ProxyTask task = new ProxyTask(request,clientOut, clientSocket);
                requestQueue.put(task);

                if (isProcessing.compareAndSet(false, true)) {
                    processQueue();
                }
             } catch (SocketTimeoutException e) {
            System.err.println("Client read timed out: " + e.getMessage());
        }
        } catch (Exception e) {
            System.err.println("Error handling client request: " + e.getMessage());
        }
    }

    private static void processQueue() {
        new Thread(() -> {
            while (!requestQueue.isEmpty()) {
                try {
                    ProxyTask task = requestQueue.take();
                    processTask(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("Error processing task:" + e.getMessage());
                }
            }
            isProcessing.set(false);
            if (!requestQueue.isEmpty() && isProcessing.compareAndSet(false, true)) {
                processQueue();
            }
        }).start();
    }

    private static void processTask(ProxyTask task) {
        try (
            Socket offshoreSocket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter offshoreOut = new PrintWriter(offshoreSocket.getOutputStream(), true);
            BufferedReader offshoreIn = new BufferedReader(new InputStreamReader(offshoreSocket.getInputStream()))
        ) {
            offshoreSocket.setSoTimeout(30000);
            offshoreOut.println(task.getRequest());
            offshoreOut.println("END_OF_REQUEST");
            offshoreOut.flush();

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            try {
                System.out.println("1");
                while ((line = offshoreIn.readLine()) != null) {
                    if ("END_OF_RESPONSE".equals(line)) break;
                    responseBuilder.append(line).append("\r\n");
                    if (task.getClientSocket().isClosed()) {
                        System.out.println("Client disconnected during response");
                        return;
                    }
                }
                if (!task.getClientSocket().isClosed()) {
                    OutputStream clientOut = task.getClientOut();
                    synchronized (clientOut) {  
                        clientOut.write(responseBuilder.toString().getBytes(StandardCharsets.UTF_8));
                        clientOut.flush();
                    }
                } else {
                    System.err.println("Client disconnected before response");
                }
                    } catch (SocketException se) {
                        System.err.println("Socket connection closed: ytvgy" + se.getMessage());
                    } catch (IOException ioe) {
                        System.err.println("Error reading/writing data: " + ioe.getMessage());
                    }
                } catch (IOException e) {
                    System.err.println("Error establishing connection: " + e.getMessage());
                }
            }

    private static class ProxyTask {
        private final String request;
        private final OutputStream clientOut;
        private final Socket clientSocket;

        public ProxyTask(String request, OutputStream clientOut, Socket clientSocket) {
            this.request = request;
            this.clientOut = clientOut;
            this.clientSocket = clientSocket;  
        }

        public String getRequest() {
            return request;
        }

        public OutputStream getClientOut() {
            return clientOut;
        }

        public Socket getClientSocket() {  
        return clientSocket;
    }
    }
}

