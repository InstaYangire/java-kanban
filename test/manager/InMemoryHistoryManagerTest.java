package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void init() {
        historyManager = new InMemoryHistoryManager();
    }

    // Тест: история изначально пуста
    @Test
    void historyShouldBeEmptyInitially() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    // Тест: добавление задачи в историю
    @Test
    void shouldAddTaskToHistory() {
        Task task = new Task("Задача", "Описание");
        task.setId(1);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    // Тест: удаление задачи из истории по ID
    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");
        task1.setId(1);
        task2.setId(2);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1); // удаляем первую задачу по ID
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    // Тест: не добавляет дубликат, а перемещает в конец
    @Test
    void shouldNotAddDuplicateTask() {
        Task task = new Task("Повтор", "Описание");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // повторное добавление

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    // Тест: история должна содержать не более 10 задач
    @Test
    void historyShouldNotExceedLimit() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task("Задача " + i, "Описание");
            task.setId(i);
            historyManager.add(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(10, history.size());
        assertEquals(6, history.get(0).getId());
        assertEquals(15, history.get(9).getId());
    }

    // Тест: удаление из начала, середины и конца истории
    @Test
    void shouldRemoveFromBeginningMiddleEnd() {
        for (int i = 1; i <= 5; i++) {
            Task task = new Task("Задача " + i, "Описание");
            task.setId(i);
            historyManager.add(task);
        }

        historyManager.remove(1); // начало
        historyManager.remove(3); // середина
        historyManager.remove(5); // конец

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(2, history.get(0).getId());
        assertEquals(4, history.get(1).getId());
    }
}
