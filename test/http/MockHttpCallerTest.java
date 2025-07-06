package http;

import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;


//это обёртка для таск менеджера
public class MockHttpCallerTest {
    private TaskManager taskManager;


    void setUp(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    //------------Доступ до функций таск менеджера прямо--------------
    void createEpic(Epic epic) {
        taskManager.addEpic(epic);
    }

    void createSubtask(Subtask subtask) {
        taskManager.addSubtask(subtask);
    }

    void createTask(Task task) {
        taskManager.addTask(task);
    }

    //не хочется верить, но можно понять, что без GET не обойтись тоже
    List<Epic> getAllEpics() {
        return taskManager.getAllEpics();
    }

    List<Subtask> getAllSubtasks() {
        return taskManager.getAllSubtasks();
    }

    List<Task> getAllTasks() {
        return taskManager.getAllTasks();
    }
}
