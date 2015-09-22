package io.lighty;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

public class Main {

    private static final String DEFAULT_CONFIG_FILE = "proxy.properties";
    private static final String LOCAL_PORT = "localPort";
    private static final String REMOTE_HOST = "remoteHost";
    private static final String REMOTE_PORT = "remotePort";
    private static final String[] VALID_PARAMETERS = {LOCAL_PORT, REMOTE_HOST, REMOTE_PORT};

    public static void main(String[] args) throws Exception {
        try (InputStream is = getConfigInputStream(args)) {
            Iterable<Config> configs = readConfigFile(is);
            for (Config config : configs) {
                igniteLighty(config);
            }
        }
    }

    private static InputStream getConfigInputStream(String[] args) throws IOException {
        if (args.length > 0) {
            Path configFile = Paths.get(args[0]);
            if (!Files.exists(configFile)) {
                throw new IllegalArgumentException("Illegal config path specified " + configFile.toAbsolutePath());
            }
            return Files.newInputStream(configFile);
        } else {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
        }
    }

    public static Iterable<Config> readConfigFile(InputStream is) throws Exception {
        Properties properties = new Properties();
        properties.load(is);
        Map<String, Config> configs = new HashMap<>();

        properties.forEach((k, v) -> {
            String[] parts = ((String) k).split("\\.");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Illegal parameter specified " + k);
            }

            String configTag = parts[0];
            String param = parts[1];
            validateParameterName(param);

            Config config = configs.computeIfAbsent(configTag, $1 -> new Config());
            switch (param) {
                case LOCAL_PORT:
                    config.localPort = Integer.parseUnsignedInt((String) v);
                    break;
                case REMOTE_HOST:
                    config.remoteHost = (String) v;
                    break;
                case REMOTE_PORT:
                    config.remotePort = Integer.parseUnsignedInt((String) v);
                    break;
            }
        });

        configs.values().forEach(Main::validateConfig);
        return configs.values();
    }

    private static void validateParameterName(String parameter) {
        if (!Stream.of(VALID_PARAMETERS).anyMatch(parameter::equals)) {
            throw new IllegalArgumentException("Illegal parameter name " + parameter);
        }
    }

    private static void validateConfig(Config config) {
        if (config.localPort <= 0) {
            throw new IllegalArgumentException("Illegal local port parameter");
        }
        if (config.remoteHost == null || config.remoteHost.isEmpty()) {
            throw new IllegalArgumentException("Illegal remote host parameter");
        }
        if (config.remotePort <= 0) {
            throw new IllegalArgumentException("Illegal remote port parameter " + config.remotePort);
        }
        InetSocketAddress localAddress = new InetSocketAddress(config.localPort);
        if (localAddress.isUnresolved()) {
            throw new IllegalArgumentException("Unresolved local address " + localAddress);
        }
        InetSocketAddress remoteAddress = new InetSocketAddress(config.remoteHost, config.remotePort);
        if (remoteAddress.isUnresolved()) {
            throw new IllegalArgumentException("Unresolved remote address " + remoteAddress);
        }
    }

    private static void igniteLighty(Config config) throws Exception {
        InetSocketAddress from = new InetSocketAddress(config.localPort);
        InetSocketAddress to = new InetSocketAddress(config.remoteHost, config.remotePort);
        new LightyServer(from, to).start();
    }

    private static final class Config {
        int localPort;
        String remoteHost;
        int remotePort;
    }
}
