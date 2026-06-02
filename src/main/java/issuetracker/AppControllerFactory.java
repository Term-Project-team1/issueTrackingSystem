package issuetracker;

import issuetracker.controller.*;
import issuetracker.database.DatabaseInitializer;
import issuetracker.database.SqliteConnectionManager;
import issuetracker.repository.account.AccountRepository;
import issuetracker.repository.account.AccountRepositoryImpl;
import issuetracker.repository.comment.CommentRepository;
import issuetracker.repository.comment.CommentRepositoryImpl;
import issuetracker.repository.issue.IssueRepository;
import issuetracker.repository.issue.IssueRepositoryImpl;
import issuetracker.repository.project.ProjectRepository;
import issuetracker.repository.project.ProjectRepositoryImpl;
import issuetracker.repository.recommendation.RecommendationRepository;
import issuetracker.repository.recommendation.RecommendationRepositoryImpl;
import issuetracker.repository.statistics.StatisticsRepository;
import issuetracker.repository.statistics.StatisticsRepositoryImpl;
import issuetracker.service.account.AccountService;
import issuetracker.service.account.AccountServiceImpl;
import issuetracker.service.comment.CommentService;
import issuetracker.service.comment.CommentServiceImpl;
import issuetracker.service.issue.IssueService;
import issuetracker.service.issue.IssueServiceImpl;
import issuetracker.service.project.ProjectService;
import issuetracker.service.project.ProjectServiceImpl;
import issuetracker.service.recommendation.RecommendationService;
import issuetracker.service.recommendation.RecommendationServiceImpl;
import issuetracker.service.search.SearchService;
import issuetracker.service.search.SearchServiceImpl;
import issuetracker.service.statistics.StatisticsService;
import issuetracker.service.statistics.StatisticsServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;

public final class AppControllerFactory {
    private AppControllerFactory() {
    }

    public static AppControllers createWithRealServices() {
        DatabaseInitializer.initialize();

        AccountRepository accountRepository = new AccountRepositoryImpl();
        ProjectRepository projectRepository = new ProjectRepositoryImpl();
        IssueRepository issueRepository = new IssueRepositoryImpl();
        StatisticsRepository statisticsRepository = new StatisticsRepositoryImpl();
        RecommendationRepository recommendationRepository = new RecommendationRepositoryImpl();
        CommentRepository commentRepository = new CommentRepositoryImpl(openCommentConnection());

        AccountService accountService = new AccountServiceImpl(accountRepository);
        ProjectService projectService = new ProjectServiceImpl(projectRepository);
        IssueService issueService = new IssueServiceImpl(issueRepository);
        CommentService commentService = new CommentServiceImpl(commentRepository, issueRepository);
        SearchService searchService = new SearchServiceImpl(issueRepository);
        StatisticsService statisticsService = new StatisticsServiceImpl(statisticsRepository);
        RecommendationService recommendationService = new RecommendationServiceImpl(recommendationRepository);

        return new AppControllers(
                new AccountController(accountService),
                new ProjectController(projectService),
                new IssueController(issueService),
                new CommentController(commentService),
                new SearchController(searchService),
                new StatisticsController(statisticsService),
                new RecommendationController(recommendationService)
        );
    }

    private static Connection openCommentConnection() {
        try {
            return SqliteConnectionManager.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open comment repository connection.", e);
        }
    }
}
