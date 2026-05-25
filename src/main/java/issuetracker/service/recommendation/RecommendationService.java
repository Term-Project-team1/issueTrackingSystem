package issuetracker.service.recommendation;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecommendationService {

    private final RecommendationQuery recommendationQuery;

    public RecommendationService(RecommendationQuery recommendationQuery) {
        this.recommendationQuery = recommendationQuery;
    }

    public List<Account> recommendAssignees(Issue issue, int limit) {
        // 추천 담당자 수 0명 이하로 요구시
        if (limit <= 0) {
            // 그냥 빈 목록 반환
            return Collections.emptyList();
        }

        Long projectId = issue.getProject().getId();
        List<Account> developers = recommendationQuery.findDevelopers();
        Map<Long, Long> fixedCounts = recommendationQuery.countFixedIssuesByDeveloper(projectId);

        List<Account> candidates = new ArrayList<>();
        for (Account developer : developers) {
            if (developer.getRole() == Role.DEV) {
                candidates.add(developer);
            }
        }

        candidates.sort(
                Comparator.<Account>comparingLong(
                        a -> fixedCounts.getOrDefault(a.getId(), 0L)
                ).reversed()
                        .thenComparing(a -> Objects.toString(a.getUsername(), ""))
        );

        if (candidates.size() > limit) {
            return new ArrayList<>(candidates.subList(0, limit));
        }
        return candidates;
    }
}
