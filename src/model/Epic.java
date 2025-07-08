package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    // Список ID подзадач, связанных с эпиком
    private ArrayList<Integer> subtaskIds;

    // Время окончания эпика (максимальное время окончания среди подзадач)
    private LocalDateTime endTime;

    // Конструктор - инициализируем имя, описание, подзадачи, а также duration и startTime по умолчанию
    public Epic(String name, String description) {
        super(name, description);
        this.subtaskIds = new ArrayList<>();
        this.duration = Duration.ZERO; // по умолчанию длительность 0 минут
        this.startTime = null;          // время начала пока не задано
        this.endTime = null;            // время окончания пока не задано
    }

    // Возвращает список ID подзадач эпика
    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    // Добавляет ID подзадачи в список
    public void addSubtaskId(int id) {
        if (subtaskIds == null) {
            this.subtaskIds = new ArrayList<>();
        }
        subtaskIds.add(id);
    }

    // Удаляет ID подзадачи из списка
    public void removeSubtaskId(int id) {
        if (subtaskIds != null) {
            subtaskIds.remove((Integer) id);
        }
    }

    // Очищает список подзадач (например, при удалении всех подзадач)
    public void clearSubtasks() {
        if (subtaskIds != null) {
            subtaskIds.clear();
        }
    }

    // Геттер для времени окончания эпика
    public LocalDateTime getEndTime() {
        return endTime;
    }

    // Сеттер для времени окончания эпика (вычисляется в менеджере)
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}