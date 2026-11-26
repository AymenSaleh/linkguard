package tn.epi.linkguard;

import java.util.List;

public record AnalysisResult(String url, int score, String level, List<String> reasons) {
}
