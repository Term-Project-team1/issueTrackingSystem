package issuetracker.domain.project;

import java.time.LocalDateTime;

public class Project {

    private Long id;
    private String name;
    private LocalDateTime createdDate;

    public Project(Long id, String name, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public LocalDateTime getCreatedDate() { return createdDate; }
}