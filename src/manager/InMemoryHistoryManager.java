package manager;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();
    private final Map<Integer, Task> historyMap = new HashMap<>();

    @Override
    public void add(Task task) {
        // Удаляем предыдущую версию, если такая есть
        remove(task.getId());

        // Добавляем задачу в конец
        history.add(task);
        historyMap.put(task.getId(), task);

        // Ограничиваем историю 10 задачами
        if (history.size() > 10) {
            Task oldest = history.remove(0);
            historyMap.remove(oldest.getId());
        }
    }

    @Override
    public void remove(int id) {
        Task toRemove = historyMap.remove(id);
        if (toRemove != null) {
            history.remove(toRemove);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}

