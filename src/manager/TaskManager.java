package manager;

import model.*;
import java.util.List;

public interface TaskManager {

    //Методы для Task
    void addTask(Task task);

    Task getTaskById(int id);

    List<Task> getAllTasks();

    void updateTask(Task task);

    void removeTask(int id);

    void removeAllTasks();

    //Методы для Epic
    void addEpic(Epic epic);

    Epic getEpicById(int id);

    List<Epic> getAllEpics();

    void updateEpic(Epic epic);

    void removeEpic(int id);

    void removeAllEpics();

    //Методы для Subtask
    void addSubtask(Subtask subtask);

    Subtask getSubtaskById(int id);

    List<Subtask> getAllSubtasks();

    List<Subtask> getSubtasksByEpicId(int epicId);

    void updateSubtask(Subtask subtask);

    void removeSubtask(int id);

    void removeAllSubtasks();

    //История просмотров
    List<Task> getHistory();
}