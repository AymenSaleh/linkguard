package tn.epi.linkguard;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LinkController {

    private final UrlAnalyzer analyzer;

    public LinkController(UrlAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @GetMapping("/api/analyze")
    public ResponseEntity<AnalysisResult> analyze(@RequestParam("url") String url) {
        return ResponseEntity.ok(analyzer.analyze(url));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidUrl(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("erreur", exception.getMessage()));
    }
}
