package manager;
import model.*;
import java.util.*;

public class TaskManager {
    // вот тут будем хранить задачи всех типов
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Счётчик Id для всех новых задач
    private int nextId = 1;

    // Генерируем уникальный ID
    private int generateId() {
        return nextId++;
    }

    // ____________Работа с Задачами (Task)_______________

    // Получить все задачи
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Получить задачу по ID
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    // Добавить новую задачу
    public void addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    // Обновить существующую задачу
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    // Удаляем задачу по ID
    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    //Удаляем все задачи
    public void removeAllTasks() {
        tasks.clear();
    }

    // ____________Работа с Большими Задачами (Epic)______________

    // Получить список всех эпиков
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Получить эпик по ID
    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    // Добавить эпик
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    // Обновить эпик (без изменения его подзадач)
    // копируем список подзадач из старого эпика, чтобы не потерять связь
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        if (oldEpic != null) {
            for (int subId : oldEpic.getSubtaskIds()) {
                epic.addSubtaskId(subId);
            }
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    // Удалить эпик и все связанные с ним подзадачи
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
            }
        }
    }

    //Удаляем все эпики (с его подзадачами, само собой)
    public void removeAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    // ____________Работа с Подзадачами для эпиков (SubTask)______________

    // Получить список всех подзадач
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получить подзадачу по ID
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    // Получить список подзадач, относящихся к конкретному эпику
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                result.add(subtasks.get(subId));
            }
        }
        return result;
    }

    // Добавить подзадачу и связать ее с эпиком
    public void addSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(id);
            updateEpicStatus(epic);
        }
    }

    // Обновить подзадачу и статус связанного эпика
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
    }

    // Удалить подзадачу и удалить ее ID из эпика
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    //Удаляем все подзадачи из эпика
    public void removeAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    //____________Работа со статусами______________

    // Метод для автоматического пересчета статуса эпика
    private void updateEpicStatus(Epic epic) {
        List<Integer> subIds = epic.getSubtaskIds();
        if (subIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int id : subIds) {
            TaskStatus status = subtasks.get(id).getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}

