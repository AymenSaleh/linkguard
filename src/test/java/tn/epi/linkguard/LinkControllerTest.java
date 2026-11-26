package tn.epi.linkguard;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class LinkControllerTest {

    private final LinkController controller = new LinkController(new UrlAnalyzer());

    @Test
    void analyseRenvoie200AvecResultat() {
        ResponseEntity<AnalysisResult> reponse = controller.analyze("https://www.epi.tn");
        assertEquals(200, reponse.getStatusCode().value());
        AnalysisResult corps = Objects.requireNonNull(reponse.getBody());
        assertEquals(UrlAnalyzer.FAIBLE, corps.level());
    }

    @Test
    void erreurDeValidationRenvoie400() {
        ResponseEntity<Map<String, String>> reponse =
                controller.handleInvalidUrl(new IllegalArgumentException("Le lien est vide."));
        assertEquals(400, reponse.getStatusCode().value());
        Map<String, String> corps = Objects.requireNonNull(reponse.getBody());
        assertEquals("Le lien est vide.", corps.get("erreur"));
    }
}
