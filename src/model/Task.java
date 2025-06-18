package model;
import java.util.Objects;
import java.time.Duration;
import java.time.LocalDateTime;

public class Task {
    protected String name;
    protected String description;
    protected int id;
    protected TaskStatus status;
    // Новые поля для продолжительности и времени старта
    protected Duration duration = Duration.ofMinutes(0); // по умолчанию 0 минут
    protected LocalDateTime startTime = null; // время начала может быть не задано

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        // нас учили только приведению типов, но если я сюда передам не Task - это будет ошибка, поэтому проверю тип
        if (!(obj instanceof Task task)) return false;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Геттер и сеттер для duration
    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    // Геттер и сеттер для startTime
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    // Метод для получения времени окончания задачи
    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null; // если время старта не задано, окончания тоже нет
        }
        return startTime.plus(duration);
    }
}
