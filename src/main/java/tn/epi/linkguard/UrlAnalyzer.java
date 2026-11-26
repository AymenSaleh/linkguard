package tn.epi.linkguard;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class UrlAnalyzer {

    static final String FAIBLE = "FAIBLE";
    static final String MOYEN = "MOYEN";
    static final String ELEVE = "ELEVE";

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final List<String> MOTS_SUSPECTS =
            List.of("login", "signin", "verify", "account", "secure", "update", "bank", "confirm");
    private static final int LONGUEUR_MAX = 75;
    private static final int POINTS_MAX = 3;

    public AnalysisResult analyze(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Le lien est vide.");
        }
        String lien = url.trim();
        URI uri = parse(lien);
        String host = uri.getHost().toLowerCase(Locale.ROOT);

        int score = 0;
        List<String> raisons = new ArrayList<>();

        if (IPV4.matcher(host).matches()) {
            score += 3;
            raisons.add("Adresse IP à la place d'un nom de domaine.");
        }
        if (uri.getRawUserInfo() != null) {
            score += 3;
            raisons.add("Le symbole @ cache la vraie destination du lien.");
        }
        if (host.contains("xn--")) {
            score += 2;
            raisons.add("Nom de domaine en punycode (possible imitation de caractères).");
        }
        if (host.chars().filter(c -> c == '.').count() > POINTS_MAX) {
            score += 2;
            raisons.add("Trop de sous-domaines.");
        }
        if (!HTTPS.equalsIgnoreCase(uri.getScheme())) {
            score += 1;
            raisons.add("Connexion non chiffrée (pas de HTTPS).");
        }
        if (lien.length() > LONGUEUR_MAX) {
            score += 1;
            raisons.add("Lien anormalement long.");
        }
        String minuscule = lien.toLowerCase(Locale.ROOT);
        List<String> trouves = MOTS_SUSPECTS.stream().filter(minuscule::contains).toList();
        if (!trouves.isEmpty()) {
            score += 1;
            raisons.add("Mots souvent utilisés en phishing : " + String.join(", ", trouves) + ".");
        }

        return new AnalysisResult(lien, score, niveau(score), raisons);
    }

    private static URI parse(String lien) {
        URI uri;
        try {
            uri = new URI(lien);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Le lien n'est pas une URL valide.");
        }
        String scheme = uri.getScheme();
        if (!HTTP.equalsIgnoreCase(scheme) && !HTTPS.equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Seuls les liens http et https sont acceptés.");
        }
        if (uri.getHost() == null) {
            throw new IllegalArgumentException("Le lien n'a pas de nom d'hôte valide.");
        }
        return uri;
    }

    static String niveau(int score) {
        if (score >= 5) {
            return ELEVE;
        }
        if (score >= 3) {
            return MOYEN;
        }
        return FAIBLE;
    }
}
