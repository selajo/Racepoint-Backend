package api;

import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Wird als zusaetzlichen Thread gestartet, der auf Anfragen reagiert
 */
@SpringBootApplication
public class RacepointApiApplication {
	/**
	 * Startet den Thread, der auf Anfragen reagiert
	 * @param args Uebergabeprameter fuer die Applikation
	 */
	public static void main(String[] args) {
		//Port-Info einlesen
		try {
			String fileContent = new String(Files.readAllBytes(Paths.get("ports_config.json")));
			JSONObject jsonObject = new JSONObject(fileContent);
			Ports.setPorts(jsonObject.getString("port_backend"), jsonObject.getString("port_frontend"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


		SpringApplication app = new SpringApplication(RacepointApiApplication.class);
		app.setDefaultProperties(Collections.singletonMap("server.port", Ports.backend_port));
		app.run(args);

	}

	/**
	 * Erlaubt den Zugriff auf dieses Projekt fuer die angegeben Ports aus der Konfigurationsdatei
	 * @return Konfiguration für den Zugriff fuer das Frontend
	 */
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		String[] allowDomains = new String[2];
		allowDomains[0] = Ports.domain_frontend;
		allowDomains[1] = Ports.domain_backend;

		//System.out.println("CORS configuration....");
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins(allowDomains);
			}
		};
	}
}
