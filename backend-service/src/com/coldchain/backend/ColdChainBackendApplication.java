package com.coldchain.backend;

import com.coldchain.backend.repository.MockDataRepository;
import com.coldchain.backend.service.ColdChainService;
import com.coldchain.backend.web.ApiHandler;
import com.coldchain.backend.web.RootHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ColdChainBackendApplication {
    public static void main(String[] args) throws IOException {
        MockDataRepository repository = new MockDataRepository();
        ColdChainService service = new ColdChainService(repository);
        int port = resolvePort();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/v1", new ApiHandler(service));
        server.createContext("/health", new RootHandler(port));
        server.createContext("/", new RootHandler(port));
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("Cold chain backend server started on http://localhost:" + port);
    }

    private static int resolvePort() {
        String portValue = System.getenv().getOrDefault("COLDCHAIN_PORT", "18081");
        try {
            return Integer.parseInt(portValue);
        } catch (NumberFormatException exception) {
            return 18081;
        }
    }
}
