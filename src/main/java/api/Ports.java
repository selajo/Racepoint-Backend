package api;

/**
 * Stellt die Struktur der Konfigurationsdatei dar, die die Ports von Front- und Backend beschreibt
 */
public class Ports {
    static String backend_port;
    static String frontend_port;

    static String domain_backend;
    static String domain_frontend;

    /**
     * Setzt die Ports und Domaenen von Front- und Backend
     * @param backend_port Backend-Port
     * @param frontend_port Frontend-Port
     */
    public static void setPorts(String backend_port, String frontend_port) {
        Ports.backend_port = backend_port;
        Ports.frontend_port = frontend_port;

        String domainBase = "http://localhost:";
        Ports.domain_backend = domainBase + backend_port;
        Ports.domain_frontend = domainBase + frontend_port;
    }
}
