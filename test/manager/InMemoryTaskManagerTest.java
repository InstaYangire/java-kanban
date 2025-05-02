package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private InMemoryTaskManager manager;

    @BeforeEach
    void init() {
        manager = new InMemoryTaskManager();
    }

    // Тест: добавление и получение задачи по идентификатору
    @Test
    void addAndGetTaskShouldReturnSameTask() {
        Task task = new Task("Проверка", "Описание задачи");
        manager.addTask(task);
        Task fetched = manager.getTaskById(task.getId());

        assertNotNull(fetched);
        assertEquals(task.getId(), fetched.getId());
        assertEquals(task.getName(), fetched.getName());
    }

    // Тест: история должна содержать просмотренные задачи в правильном порядке
    @Test
    void historyShouldContainRecentlyViewedTasks() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        manager.addTask(task1);
        manager.addTask(task2);

        manager.getTaskById(task1.getId());
        manager.getTaskById(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }

    // Тест: добавление эпика и получение по идентификатору
    @Test
    void addAndGetEpicShouldWorkCorrectly() {
        Epic epic = new Epic("Переезд", "Описание эпика");
        manager.addEpic(epic);
        Epic result = manager.getEpicById(epic.getId());

        assertNotNull(result);
        assertEquals(epic.getId(), result.getId());
        assertEquals(epic.getName(), result.getName());
    }

    // Тест: подзадача должна быть связана с эпиком
    @Test
    void subtaskShouldLinkToEpicCorrectly() {
        Epic epic = new Epic("Организация", "Подготовка");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Упаковка", "Коробки", epic.getId());
        manager.addSubtask(subtask);

        Subtask result = manager.getSubtaskById(subtask.getId());
        assertNotNull(result);
        assertEquals(epic.getId(), result.getEpicId());
    }

    // Тест: статус эпика должен автоматически обновляться при изменении статуса подзадач
    @Test
    void epicStatusShouldBeUpdatedFromSubtasks() {
        Epic epic = new Epic("Праздник", "Подготовка");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Торт", "Ванильный", epic.getId());
        Subtask sub2 = new Subtask("Шарики", "Для вечеринки", epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    // Тест: задача должна быть удалена по идентификатору
    @Test
    void removeTaskShouldDeleteTask() {
        Task task = new Task("Удалить задачу", "Описание задачи");
        manager.addTask(task);
        int taskId = task.getId();

        manager.removeTask(taskId);
        Task fetched = manager.getTaskById(taskId);

        assertNull(fetched);
    }

    // Тест: статус задачи должен обновляться
    @Test
    void taskStatusShouldBeUpdated() {
        Task task = new Task("Изменить статус", "Описание задачи");
        manager.addTask(task);

        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        Task updatedTask = manager.getTaskById(task.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    // Тест: удаление эпика должно удалить все его подзадачи
    @Test
    void removeEpicShouldDeleteAllSubtasks() {
        Epic epic = new Epic("Праздник", "Организация дня");
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Купить торт", "Ванильный", epic.getId());
        manager.addSubtask(subtask);

        manager.removeEpic(epic.getId());

        Subtask deletedSubtask = manager.getSubtaskById(subtask.getId());
        assertNull(deletedSubtask);
    }

    // Тест: каждая задача должна иметь уникальный идентификатор
    @Test
    void taskShouldHaveUniqueId() {
        Task task1 = new Task("Первая задача", "Описание");
        Task task2 = new Task("Вторая задача", "Описание");

        manager.addTask(task1);
        manager.addTask(task2);

        assertNotEquals(task1.getId(), task2.getId());
    }

    // Тест: при обновлении эпика подзадачи не дублируются
    @Test
    void updateEpicShouldNotDuplicateSubtaskIds() {
        Epic epic = new Epic("Мероприятие", "Организация");
        manager.addEpic(epic);

        Subtask sub1 = new Subtask("Заказать зал", "С залом", epic.getId());
        Subtask sub2 = new Subtask("Купить напитки", "Сок и вода", epic.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        Epic updatedEpic = new Epic("Мероприятие обновлено", "Новое описание");
        updatedEpic.setId(epic.getId());
        manager.updateEpic(updatedEpic);

        Epic result = manager.getEpicById(epic.getId());
        List<Integer> subtaskIds = result.getSubtaskIds();

        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(sub1.getId()));
        assertTrue(subtaskIds.contains(sub2.getId()));
    }
}