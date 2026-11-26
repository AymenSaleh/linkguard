package tn.epi.linkguard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UrlAnalyzerTest {

    private final UrlAnalyzer analyzer = new UrlAnalyzer();

    @Test
    void lienHttpsNormalEstFaible() {
        AnalysisResult resultat = analyzer.analyze("https://www.epi.tn");
        assertEquals(0, resultat.score());
        assertEquals(UrlAnalyzer.FAIBLE, resultat.level());
        assertTrue(resultat.reasons().isEmpty());
    }

    @Test
    void adresseIpSansHttpsEstMoyen() {
        AnalysisResult resultat = analyzer.analyze("http://192.168.1.10/");
        assertEquals(4, resultat.score());
        assertEquals(UrlAnalyzer.MOYEN, resultat.level());
    }

    @Test
    void arobaseEtMotSuspectSontEleves() {
        AnalysisResult resultat = analyzer.analyze("http://paypal.com@evil.example/login");
        assertEquals(5, resultat.score());
        assertEquals(UrlAnalyzer.ELEVE, resultat.level());
    }

    @Test
    void punycodeEstSignale() {
        AnalysisResult resultat = analyzer.analyze("https://xn--pypal-4ve.com");
        assertTrue(resultat.reasons().stream().anyMatch(raison -> raison.contains("punycode")));
    }

    @Test
    void tropDeSousDomainesEstMoyen() {
        AnalysisResult resultat = analyzer.analyze("https://paypal.com.secure.account.evil.example");
        assertEquals(3, resultat.score());
        assertEquals(UrlAnalyzer.MOYEN, resultat.level());
    }

    @Test
    void lienTropLongEstSignale() {
        AnalysisResult resultat = analyzer.analyze("https://www.epi.tn/" + "a".repeat(80));
        assertEquals(1, resultat.score());
    }

    @Test
    void lienVideEstRefuse() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze("   "));
    }

    @Test
    void schemaNonWebEstRefuse() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze("javascript:alert(1)"));
    }

    @Test
    void lienMalFormeEstRefuse() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze("http://exa mple.com"));
    }
}
