package manager;

import model.*;

public class TaskConverter {
    // Преобразует задачу в строку формата CSV
    public static String toString(Task task) {
        String base = String.join(",",
                String.valueOf(task.getId()),
                getType(task).name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription()
        );

        if (task instanceof Subtask subtask) {
            return base + "," + subtask.getEpicId();
        } else {
            return base;
        }
    }

    // Восстанавливает задачу из строки CSV
    public static Task fromString(String line) {
        String[] parts = line.split(",");

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case TASK -> {
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    // Определяет тип задачи по объекту
    private static TaskType getType(Task task) {
        if (task instanceof Epic) return TaskType.EPIC;
        if (task instanceof Subtask) return TaskType.SUBTASK;
        return TaskType.TASK;
    }
}
