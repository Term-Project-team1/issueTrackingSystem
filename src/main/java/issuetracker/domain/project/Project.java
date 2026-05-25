package issuetracker.domain.project;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {

    private Long id;

    private String name;

    private LocalDateTime createdDate;

    public Project() {
    }

    public Project(Long id, String name) {
        this(id, name, null);
    }

    public Project(Long id, String name, LocalDateTime createdDate) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
