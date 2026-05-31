package issuetracker.service.recommendation;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.repository.recommendation.RecommendationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RecommendationServiceImpl implements RecommendationService {

    private static final double FIXED_COUNT_WEIGHT = 1.0;
    private static final double SIMILARITY_WEIGHT = 5.0;

    private final RecommendationRepository recommendationRepository;

    public RecommendationServiceImpl(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Override
    public List<Account> recommendAssignees(Issue issue, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        Long projectId = issue.getProject().getId();

        List<Account> developers = recommendationRepository.findDevelopers();
        Map<Long, Long> fixedCounts = recommendationRepository.countFixedIssuesByDeveloper(projectId);
        List<Issue> solvedIssues = recommendationRepository.findSolvedIssues(projectId);

        Map<Long, Double> similarityScores = calculateSimilarityScores(issue, solvedIssues);

        List<Account> candidates = new ArrayList<>();
        for (Account developer : developers) {
            if (developer.getRole() == Role.DEV) {
                candidates.add(developer);
            }
        }

        candidates.sort(
                Comparator.<Account>comparingDouble(
                                developer -> calculateTotalScore(
                                        developer,
                                        fixedCounts,
                                        similarityScores
                                )
                        )
                        .reversed()
                        .thenComparing(developer -> Objects.toString(developer.getUsername(), ""))
        );

        if (candidates.size() > limit) {
            return new ArrayList<>(candidates.subList(0, limit));
        }

        return candidates;
    }

    private double calculateTotalScore(Account developer,
                                       Map<Long, Long> fixedCounts,
                                       Map<Long, Double> similarityScores) {
        long fixedCount = fixedCounts.getOrDefault(developer.getId(), 0L);
        double similarity = similarityScores.getOrDefault(developer.getId(), 0.0);

        return fixedCount * FIXED_COUNT_WEIGHT + similarity * SIMILARITY_WEIGHT;
    }

    private Map<Long, Double> calculateSimilarityScores(Issue targetIssue,
                                                        List<Issue> solvedIssues) {
        Map<Long, Double> result = new HashMap<>();

        Map<String, Double> targetVector = toTermFrequencyVector(getIssueText(targetIssue));

        for (Issue solvedIssue : solvedIssues) {
            if (solvedIssue.getFixer() == null) {
                continue;
            }

            Map<String, Double> solvedVector = toTermFrequencyVector(getIssueText(solvedIssue));
            double similarity = calculateCosineSimilarity(targetVector, solvedVector);

            Long fixerId = solvedIssue.getFixer().getId();
            result.put(fixerId, result.getOrDefault(fixerId, 0.0) + similarity);
        }

        return result;
    }

    private double calculateCosineSimilarity(Map<String, Double> vectorA,
                                             Map<String, Double> vectorB) {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(vectorA.keySet());
        allWords.addAll(vectorB.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String word : allWords) {
            double a = vectorA.getOrDefault(word, 0.0);
            double b = vectorB.getOrDefault(word, 0.0);

            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private Map<String, Double> toTermFrequencyVector(String text) {
        List<String> words = extractWords(text);
        Map<String, Double> vector = new HashMap<>();

        if (words.isEmpty()) {
            return vector;
        }

        for (String word : words) {
            vector.put(word, vector.getOrDefault(word, 0.0) + 1.0);
        }

        for (String word : vector.keySet()) {
            vector.put(word, vector.get(word) / words.size());
        }

        return vector;
    }

    private String getIssueText(Issue issue) {
        return issue.getTitle() + " " + issue.getDescription();
    }

    private List<String> extractWords(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9가-힣 ]", " ");

        String[] tokens = normalized.split("\\s+");

        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            if (token.length() >= 2) {
                result.add(token);
            }
        }

        return result;
    }
}