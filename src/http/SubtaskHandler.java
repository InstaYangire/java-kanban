package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;
import model.TaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// Обработчик для работы с Подзадачами (Subtask)
public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
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
    public SubtaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    // Главный метод обработки HTTP-запросов
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath(); // /subtasks, /subtasks/{id}
            String[] segments = path.split("/");

            // Получение всех подзадач
            if ("GET".equals(method) && segments.length == 2) {
                List<Subtask> subtasks = taskManager.getAllSubtasks();
                String json = gson.toJson(subtasks);
                sendText(exchange, json);
                return;
            }

            // Получение подзадачи по ID
            if ("GET".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange);
                    return;
                }
                String json = gson.toJson(subtask);
                sendText(exchange, json);
                return;
            }

            // Создание новой подзадачи
            if ("POST".equals(method) && segments.length == 2) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                try {
                    Subtask subtask = gson.fromJson(body, Subtask.class);

                    // Если эпика не существует
                    if (subtask.getEpicId() == 0 || taskManager.getEpicById(subtask.getEpicId()) == null) {
                        exchange.sendResponseHeaders(404, 0);
                        exchange.getResponseBody().close();
                        return;
                    }

                    // Если передан id
                    if (subtask.getId() != 0) {
                        try {
                            taskManager.updateSubtask(subtask);
                            exchange.sendResponseHeaders(201, 0); // Задача создана
                            exchange.getResponseBody().close();
                            return;
                        } catch (IllegalArgumentException e) {
                            sendHasInteractions(exchange);
                            return;
                        }
                    }

                    // Если статус не указан — проставляем NEW
                    if (subtask.getStatus() == null) {
                        subtask.setStatus(TaskStatus.NEW);
                    }

                    // Проверяем наличие обязательных полей
                    if (subtask.getName() == null || subtask.getDescription() == null) {
                        sendHasInteractions(exchange);
                        return;
                    }
                    try {
                        taskManager.addSubtask(subtask);
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                        return;
                    }
                    exchange.sendResponseHeaders(201, 0); // Подзадача создана
                    exchange.getResponseBody().close();

                } catch (JsonSyntaxException e) {
                    exchange.sendResponseHeaders(400, 0); // Неверный формат JSON
                    exchange.getResponseBody().close();
                }
                return;
            }

            // Удаление всех подзадач
            if ("DELETE".equals(method) && segments.length == 2) {
                taskManager.removeAllSubtasks();
                exchange.sendResponseHeaders(200, 0); // Успешно
                exchange.getResponseBody().close();
                return;
            }

            // Удаление подзадачи по ID
            if ("DELETE".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange);
                    return;
                }

                taskManager.removeSubtask(id);
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
