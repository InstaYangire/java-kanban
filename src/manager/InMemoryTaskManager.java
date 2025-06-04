package manager;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    // вот тут будем хранить задачи всех типов + история
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected HistoryManager historyManager;  // изменено с final на protected

    // Счётчик Id для всех новых задач
    protected int nextId = 1;

    // Конструктор с инициализацией historyManager
    public InMemoryTaskManager() {
        this.historyManager = new InMemoryHistoryManager();
    }

    // Генерируем уникальный ID
    private int generateId() {
        return nextId++;
    }

    // ____________Работа с Задачами (Task)_______________

    // Добавляет новую задачу
    @Override
    public void addTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    // Возвращает задачу по ID и добавляет её в историю
    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    // Возвращает список всех задач
    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Обновляет задачу по ID
    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    // Удаляет задачу по ID
    @Override
    public void removeTask(int id) {
        tasks.remove(id);
        // Удаляем задачу из истории после ее удаления
        historyManager.remove(id);
    }

    // Удаляет все задачи
    @Override
    public void removeAllTasks() {
        for (Integer id : tasks.keySet()) {
            // Удаляем каждую задачу из истории
            historyManager.remove(id);
        }
        tasks.clear();
    }

    // ____________Работа с Большими Задачами (Epic)______________

    // Добавляет новый эпик
    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    // Возвращает эпик по ID и добавляет в историю
    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    // Возвращает список всех эпиков
    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Обновляет эпик: заменяет имя/описание и сохраняет подзадачи
    @Override
    public void updateEpic(Epic epic) {
        Epic oldEpic = epics.get(epic.getId());
        // Очищаем список подзадач, чтобы избежать дублирования ID
        epic.clearSubtasks();
        if (oldEpic != null) {
            for (int subId : oldEpic.getSubtaskIds()) {
                epic.addSubtaskId(subId);
            }
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    // Удаляет эпик и связанные с ним подзадачи
    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (int subId : epic.getSubtaskIds()) {
                subtasks.remove(subId);
                // Удаляем подзадачи эпика из истории
                historyManager.remove(subId);
            }
            // Удаляем сам эпик из истории
            historyManager.remove(id);
        }
    }

    // Удаляет все эпики и подзадачи
    @Override
    public void removeAllEpics() {
        for (Epic epic : epics.values()) {
            // Удаляем эпик из истории
            historyManager.remove(epic.getId());
            for (int subId : epic.getSubtaskIds()) {
                // Удаляем все подзадачи эпика из истории
                historyManager.remove(subId);
            }
        }
        epics.clear();
        subtasks.clear();
    }

    // ____________Работа с Подзадачами для эпиков (SubTask)______________

    // Добавляет подзадачу и привязывает к эпику
    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
        }
    }

    // Возвращает подзадачу по ID и добавляет в историю
    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    // Возвращает список всех подзадач
    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Возвращает список подзадач конкретного эпика
    @Override
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

    // Обновляет подзадачу и статус эпика
    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(epics.get(subtask.getEpicId()));
    }

    // Удаляет подзадачу и обновляет статус эпика
    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
            // Удаляем подзадачу из истории
            historyManager.remove(id);
        }
    }

    // Удаляет все подзадачи и очищает ссылки у эпиков
    @Override
    public void removeAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            // Удаляем каждую подзадачу из истории
            historyManager.remove(id);
        }
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    // ____________История просмотров______________

    // Возвращает список последних просмотренных задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Обновляет статус эпика на основе статусов подзадач
    private void updateEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();
        if (subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (int subId : subtaskIds) {
            TaskStatus status = subtasks.get(subId).getStatus();
            if (status != TaskStatus.NEW) {
                allNew = false;
            }
            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}

