package manager;

import model.*;

import java.io.File;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    // Метод сохраняет все задачи, эпики и подзадачи в файл в формате CSV.
    // В случае ошибки — выбрасывает непроверяемое исключение ManagerSaveException.
    protected void save() {
        try (var writer = new java.io.FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : tasks.values()) {
                writer.write(TaskConverter.toString(task) + "\n");
            }
            for (Epic epic : epics.values()) {
                writer.write(TaskConverter.toString(epic) + "\n");
            }
            for (Subtask subtask : subtasks.values()) {
                writer.write(TaskConverter.toString(subtask) + "\n");
            }

            writer.write("\n"); // пустая строка перед историей

            List<Task> history = getHistory();
            if (!history.isEmpty()) {
                String historyLine = HistoryConverter.historyToString(history);
                writer.write(historyLine);
            }
        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            List<String> lines = java.nio.file.Files.readAllLines(file.toPath());

            int maxId = 0;
            boolean isHistoryBlock = false;

            for (String line : lines) {
                if (line.isBlank()) {
                    isHistoryBlock = true;
                    continue;
                }

                if (!isHistoryBlock) {
                    if (line.startsWith("id,")) continue; // пропускаем заголовок

                    Task task = TaskConverter.fromString(line);
                    int id = task.getId();
                    maxId = Math.max(maxId, id);

                    switch (task) {
                        case Epic epic -> manager.epics.put(id, epic);
                        case Subtask subtask -> {
                            manager.subtasks.put(id, subtask);
                            Epic epic = manager.epics.get(subtask.getEpicId());
                            if (epic != null) {
                                epic.addSubtaskId(id);
                            }
                        }
                        default -> manager.tasks.put(id, task);
                    }
                } else {
                    List<Integer> historyIds = HistoryConverter.historyFromString(line);
                    for (Integer id : historyIds) {
                        if (manager.tasks.containsKey(id)) {
                            manager.historyManager.add(manager.tasks.get(id));
                        } else if (manager.subtasks.containsKey(id)) {
                            manager.historyManager.add(manager.subtasks.get(id));
                        } else if (manager.epics.containsKey(id)) {
                            manager.historyManager.add(manager.epics.get(id));
                        }
                    }
                }
            }

            manager.nextId = maxId + 1;

            // Обновляем время и длительность у всех эпиков после загрузки
            for (Epic epic : manager.epics.values()) {
                manager.updateEpicTimeAndDuration(epic);
            }

        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }

        return manager;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }
}