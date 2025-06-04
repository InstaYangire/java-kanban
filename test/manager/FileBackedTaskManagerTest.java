package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        tempFile.deleteOnExit();
        manager = new FileBackedTaskManager(tempFile);
    }

    // Проверяем, что менеджер корректно сохраняет и загружает пустое состояние
    @Test
    void saveAndLoadEmptyManager() {
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
        assertTrue(loaded.getHistory().isEmpty());
    }

    // Проверяем сохранение и загрузку задач, эпиков и подзадач с сохранением их связей
    @Test
    void saveAndLoadTasksEpicsSubtasks() {
        Task task = new Task("Task 1", "Description 1");
        manager.addTask(task);

        Epic epic = new Epic("Epic 1", "Epic Description");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask 1", "Subtask Description", epic.getId());
        manager.addSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedTasks = loaded.getAllTasks();
        assertEquals(1, loadedTasks.size());
        assertEquals(task.getName(), loadedTasks.get(0).getName());

        List<Epic> loadedEpics = loaded.getAllEpics();
        assertEquals(1, loadedEpics.size());
        assertEquals(epic.getName(), loadedEpics.get(0).getName());

        List<Subtask> loadedSubtasks = loaded.getAllSubtasks();
        assertEquals(1, loadedSubtasks.size());
        assertEquals(subtask.getName(), loadedSubtasks.get(0).getName());

        assertEquals(epic.getId(), loadedSubtasks.get(0).getEpicId());
    }

    // Проверяем, что история просмотров корректно сохраняется и загружается
    @Test
    void historyIsSavedAndLoadedCorrectly() {
        Task task = new Task("Task A", "Desc A");
        manager.addTask(task);

        Epic epic = new Epic("Epic B", "Desc B");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask C", "Desc C", epic.getId());
        manager.addSubtask(subtask);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> history = loaded.getHistory();

        assertEquals(3, history.size());
        assertEquals(task.getId(), history.get(0).getId());
        assertEquals(epic.getId(), history.get(1).getId());
        assertEquals(subtask.getId(), history.get(2).getId());
    }
}