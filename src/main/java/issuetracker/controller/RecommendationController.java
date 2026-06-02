package issuetracker.controller;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.service.recommendation.RecommendationService;

import java.util.List;

public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    public List<Account> recommendAssignees(Issue issue, int limit) {
        return recommendationService.recommendAssignees(issue, limit);
    }
}
