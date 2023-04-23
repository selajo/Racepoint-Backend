package api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Generiert die korrekten JSON-Pakete fuer das Frontend.
 * Exkludiert und inkludiert Felder in die JSON-Klassen.
 */
@Configuration
public class JacksonConfiguration {
    /**
     * Wird verwendet, damit JSON als Standard-Format der Nachrichten verwendet wird
     * @return Objektmapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(
                mapper.getSerializationConfig().
                        getDefaultVisibilityChecker().
                        withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                        withGetterVisibility(JsonAutoDetect.Visibility.NONE)).
                setAnnotationIntrospector(new IgnoreInheritedIntrospector());
        return mapper;
    }
}

/**
 * Verwaltet, welche Daten ignoriert werden sollen.
 */
class IgnoreInheritedIntrospector extends JacksonAnnotationIntrospector {
    /**
     * Wird verwendet, um zu signalisieren, dass Member mit einem Ignore-Field aus den Nachrichten entfernt werden
     * @param m Zu ueberpruefender Member
     * @return Ob ein Member einen Ignore-Field hat
     */
    @Override
    public boolean hasIgnoreMarker(final AnnotatedMember m) {
        return m.getDeclaringClass() == File.class || super.hasIgnoreMarker(m);
    }
}
