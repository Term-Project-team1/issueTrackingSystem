package issuetracker.controller;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.service.statistics.StatisticsService;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    public Map<IssueStatus, Long> countIssuesByStatus(Long projectId) {
        return statisticsService.countIssuesByStatus(projectId);
    }

    public Map<Priority, Long> countIssuesByPriority(Long projectId) {
        return statisticsService.countIssuesByPriority(projectId);
    }

    public Map<Account, Long> countFixedIssuesByDeveloper(Long projectId) {
        return statisticsService.countFixedIssuesByDeveloper(projectId);
    }

    public Map<LocalDate, Long> countIssuesByDay(Long projectId, YearMonth month) {
        return statisticsService.countIssuesByDay(projectId, month);
    }

    public Map<YearMonth, Long> countIssuesByMonth(Long projectId, int year) {
        return statisticsService.countIssuesByMonth(projectId, year);
    }
}
