package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;
import model.TaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// Обработчик для работы с обычными задачами (Task)
public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    // Поддержка сериализации и десериализации LocalDateTime и Duration (в минутах)
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, type, context) -> LocalDateTime.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Duration.class, (JsonDeserializer<Duration>)
                    (json, type, context) -> Duration.ofMinutes(json.getAsLong()))
            .registerTypeAdapter(Duration.class, (JsonSerializer<Duration>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.toMinutes()))
            .create();

    // Конструктор с привязкой менеджера задач
    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Главный метод обработки HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String[] segments = path.split("/");

            // Получение всех обычных задач
            if ("GET".equals(method) && segments.length == 2) {
                List<Task> tasks = taskManager.getAllTasks();
                String json = gson.toJson(tasks);
                sendText(exchange, json);
                return;
            }

            // Получение задачи по ID
            if ("GET".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange);
                    return;
                }
                String json = gson.toJson(task);
                sendText(exchange, json);
                return;
            }

            // Создание новой задачи
            if ("POST".equals(method) && segments.length == 2) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                try {
                    Task task = gson.fromJson(body, Task.class);

                    // Если передан id
                    if (task.getId() != 0) {
                        try {
                            taskManager.updateTask(task);
                            exchange.sendResponseHeaders(201, 0); // Задача создана
                            exchange.getResponseBody().close();
                            return;
                        } catch (IllegalArgumentException e) {
                            sendHasInteractions(exchange);
                            return;
                        }
                    }

                    // Если статус не указан — проставляем NEW
                    if (task.getStatus() == null) {
                        task.setStatus(TaskStatus.NEW);
                    }

                    // Проверяем наличие обязательных полей
                    if (task.getName() == null || task.getDescription() == null) {
                        sendHasInteractions(exchange);
                        return;
                    }

                    try {
                        taskManager.addTask(task);
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                        return;
                    }

                    exchange.sendResponseHeaders(201, 0); // Задача создана
                    exchange.getResponseBody().close();
                } catch (JsonSyntaxException e) {
                    exchange.sendResponseHeaders(400, 0); // Неверный формат JSON
                    exchange.getResponseBody().close();
                }
                return;
            }

            // Удаление всех задач
            if ("DELETE".equals(method) && segments.length == 2) {
                taskManager.removeAllTasks();
                exchange.sendResponseHeaders(200, 0); // Успешно
                exchange.getResponseBody().close();
                return;
            }

            // Удаление задачи по ID
            if ("DELETE".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange);
                    return;
                }

                taskManager.removeTask(id);
                exchange.sendResponseHeaders(200, 0); // Удалено
                exchange.getResponseBody().close();
                return;
            }

            // Метод не поддерживается
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();

        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0); // Внутренняя ошибка сервера
            exchange.getResponseBody().close();
        }
    }

    // Парсинг ID из сегмента URL
    private int parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}