package issuetracker.controller;

public record AppControllers(
        AccountController account,
        ProjectController project,
        IssueController issue,
        CommentController comment,
        SearchController search,
        StatisticsController statistics,
        RecommendationController recommendation
) {
}
