package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDateTime;
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

    // Тест: удалённая задача пропадает из истории
    @Test
    void removedTaskShouldBeRemovedFromHistory() {
        Task task = new Task("Удаляемая", "Описание");
        manager.addTask(task);
        manager.getTaskById(task.getId());

        manager.removeTask(task.getId());

        List<Task> history = manager.getHistory();
        assertFalse(history.contains(task));
    }

    // Тест: удаление эпика удаляет его и подзадачи из истории
    @Test
    void removingEpicShouldAlsoClearItsSubtasksFromHistory() {
        Epic epic = new Epic("Эпик", "С подзадачами");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Подзадача 1", "Описание", epic.getId());
        Subtask s2 = new Subtask("Подзадача 2", "Описание", epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(s1.getId());
        manager.getSubtaskById(s2.getId());

        manager.removeEpic(epic.getId());

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty());
    }

    // Тест: подзадачи не дублируются при updateEpic
    @Test
    void subtaskLinksShouldBePreservedAfterEpicUpdate() {
        Epic epic = new Epic("Старый эпик", "Описание");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Одна", "Описание", epic.getId());
        Subtask s2 = new Subtask("Другая", "Описание", epic.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);

        Epic updated = new Epic("Обновлён", "Новое описание");
        updated.setId(epic.getId());
        manager.updateEpic(updated);

        Epic fromManager = manager.getEpicById(epic.getId());
        List<Integer> subtaskIds = fromManager.getSubtaskIds();

        assertEquals(2, subtaskIds.size());
        assertTrue(subtaskIds.contains(s1.getId()));
        assertTrue(subtaskIds.contains(s2.getId()));
    }

    // Тест: у задачи корректно работают поля времени и длительности
    @Test
    void taskTimeFieldsShouldWorkCorrectly() {
        Task task = new Task("Задача с временем", "Проверка времени");
        LocalDateTime start = LocalDateTime.of(2025, 6, 5, 10, 0);
        task.setStartTime(start);
        task.setDuration(Duration.ofMinutes(30));

        assertEquals(start, task.getStartTime());
        assertEquals(Duration.ofMinutes(30), task.getDuration());
        assertEquals(start.plusMinutes(30), task.getEndTime());
    }

    // Тест: у эпика правильно вычисляются время старта, окончания и суммарная длительность из подзадач
    @Test
    void epicTimeShouldBeCalculatedFromSubtasks() {
        Epic epic = new Epic("Эпик", "Тестовое время");
        manager.addEpic(epic);

        Subtask s1 = new Subtask("Подзадача 1", "Первая", epic.getId());
        s1.setStartTime(LocalDateTime.of(2025, 6, 5, 9, 0));
        s1.setDuration(Duration.ofMinutes(60));
        manager.addSubtask(s1);

        Subtask s2 = new Subtask("Подзадача 2", "Вторая", epic.getId());
        s2.setStartTime(LocalDateTime.of(2025, 6, 5, 11, 0));
        s2.setDuration(Duration.ofMinutes(30));
        manager.addSubtask(s2);

        Epic resultEpic = manager.getEpicById(epic.getId());

        assertEquals(LocalDateTime.of(2025, 6, 5, 9, 0), resultEpic.getStartTime());
        assertEquals(Duration.ofMinutes(90), resultEpic.getDuration());
        assertEquals(LocalDateTime.of(2025, 6, 5, 11, 30), resultEpic.getEndTime());
    }

    // Тест: getPrioritizedTasks возвращает задачи и подзадачи, отсортированные по времени старта
    @Test
    void getPrioritizedTasksShouldReturnTasksSortedByStartTime() {
        Task task1 = new Task("Таска1", "Описание");
        task1.setStartTime(LocalDateTime.of(2025, 6, 5, 8, 0));
        manager.addTask(task1);

        Task task2 = new Task("Таска2", "Описание");
        // Без времени старта — не попадёт в результат
        manager.addTask(task2);

        Subtask subtask1 = new Subtask("Подзадача1", "Описание", 0);
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 5, 7, 0));
        manager.addSubtask(subtask1);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size()); // только задачи с временем старта
        assertEquals(subtask1.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    // Тест: при добавлении пересекающейся задачи выбрасывается исключение
    @Test
    void addTaskShouldThrowExceptionIfTimeOverlap() {
        Task task1 = new Task("Таска1", "Описание1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 5, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addTask(task1);

        Task overlappingTask = new Task("Таска2", "Описание2");
        // Пересекается с task1
        overlappingTask.setStartTime(LocalDateTime.of(2025, 6, 5, 10, 30));
        overlappingTask.setDuration(Duration.ofMinutes(30));

        assertThrows(ManagerSaveException.class, () -> manager.addTask(overlappingTask));
    }

    // Тест: при обновлении задачи на пересекающееся время выбрасывается исключение
    @Test
    void updateTaskShouldThrowExceptionIfTimeOverlap() {
        Task task1 = new Task("Таска1", "Описание1");
        task1.setStartTime(LocalDateTime.of(2025, 6, 5, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.addTask(task1);

        Task task2 = new Task("Таска2", "Описание2");
        task2.setStartTime(LocalDateTime.of(2025, 6, 5, 11, 0));
        task2.setDuration(Duration.ofMinutes(60));
        manager.addTask(task2);
        // Пересекается с task1
        task2.setStartTime(LocalDateTime.of(2025, 6, 5, 10, 30));

        assertThrows(ManagerSaveException.class, () -> manager.updateTask(task2));
    }
}