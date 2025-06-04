package manager;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class HistoryConverter {

    // Преобразует историю в строку ID через запятую
    public static String historyToString(List<Task> history) {
        List<String> ids = new ArrayList<>();
        for (Task task : history) {
            ids.add(String.valueOf(task.getId()));
        }
        return String.join(",", ids);
    }

    // Преобразует строку ID в список чисел
    public static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        if (value == null || value.isBlank()) return history;

        String[] ids = value.split(",");
        for (String id : ids) {
            history.add(Integer.parseInt(id));
        }
        return history;
    }
}