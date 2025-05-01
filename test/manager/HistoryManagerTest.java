package manager;

import model.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void shouldAddTask() {
        Task task = new Task("Test", "Description");
        task.setId(1);
        historyManager.add(task);
        assertEquals(List.of(task), historyManager.getHistory());
    }

    @Test
    void shouldRemoveTaskById() {
        Task task = new Task("Test", "Description");
        task.setId(1);
        historyManager.add(task);
        historyManager.remove(1);
        assertTrue(historyManager.getHistory().isEmpty());
    }
}

