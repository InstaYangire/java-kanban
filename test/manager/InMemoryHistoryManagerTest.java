package manager;

import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    // Тест: повторное добавление не создает дубликатов, а перемещает в конец
    @Test
    void shouldNotAddDuplicateTask() {
        Task task = new Task("Повтор", "Описание");
        task.setId(1);

        historyManager.add(task);
        historyManager.add(task); // повторное добавление

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size()); // только один элемент
        assertEquals(task, history.get(0)); // он в конце
    }

    // Тест: добавление нескольких задач, порядок сохраняется
    @Test
    void shouldPreserveViewOrder() {
        Task t1 = new Task("1", "A"); t1.setId(1);
        Task t2 = new Task("2", "B"); t2.setId(2);
        Task t3 = new Task("3", "C"); t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(t1, t2, t3), history);
    }

    // Тест: удаление задачи по ID
    @Test
    void shouldRemoveTaskFromHistory() {
        Task t1 = new Task("1", "A"); t1.setId(1);
        Task t2 = new Task("2", "B"); t2.setId(2);
        Task t3 = new Task("3", "C"); t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(2); // удалим из середины

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(t1, t3), history);
    }

    // Тест: удаление первой и последней задач
    @Test
    void shouldRemoveFromBeginningAndEnd() {
        Task t1 = new Task("1", "A"); t1.setId(1);
        Task t2 = new Task("2", "B"); t2.setId(2);
        Task t3 = new Task("3", "C"); t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(1); // начало
        historyManager.remove(3); // конец

        List<Task> history = historyManager.getHistory();
        assertEquals(List.of(t2), history);
    }
}
