package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void init() {
        manager = new InMemoryTaskManager();
    }

    // Тест: добавление и получение задачи по интерфейсу TaskManager
    @Test
    void addAndGetTaskViaInterface() {
        Task task = new Task("Проверка", "Описание");
        manager.addTask(task);
        Task result = manager.getTaskById(task.getId());

        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
    }

    // Тест: добавление и получение эпика через интерфейс
    @Test
    void addAndGetEpicViaInterface() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.addEpic(epic);
        Epic result = manager.getEpicById(epic.getId());

        assertNotNull(result);
        assertEquals(epic.getId(), result.getId());
    }

    // Тест: добавление и получение подзадачи через интерфейс
    @Test
    void addAndGetSubtaskViaInterface() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", epic.getId());
        manager.addSubtask(subtask);

        Subtask result = manager.getSubtaskById(subtask.getId());
        assertNotNull(result);
        assertEquals(epic.getId(), result.getEpicId());
    }
}