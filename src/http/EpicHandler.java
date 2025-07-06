package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.TaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

// Обработчик для работы с Эпиками (Epic)
public class EpicHandler extends BaseHttpHandler implements HttpHandler {
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
    public EpicHandler(TaskManager taskManager) {
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

            // Получение всех эпиков
            if ("GET".equals(method) && segments.length == 2) {
                List<Epic> epics = taskManager.getAllEpics();
                String json = gson.toJson(epics);
                sendText(exchange, json);
                return;
            }

            // Получение эпика по ID
            if ("GET".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange); // Эпик не найден
                    return;
                }
                String json = gson.toJson(epic);
                sendText(exchange, json);
                return;
            }

            // Получение всех подзадач эпика
            if ("GET".equals(method) && segments.length == 4 && "subtasks".equals(segments[3])) {
                int id = parseId(segments[2]);
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange); // Эпик не найден
                    return;
                }
                List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epic.getId());
                String json = gson.toJson(subtasks);
                sendText(exchange, json);
                return;
            }

            // Создание нового эпика
            if ("POST".equals(method) && segments.length == 2) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                try {
                    Epic epic = gson.fromJson(body, Epic.class);
                    //

                    // Если статус не указан — проставляем NEW
                    if (epic.getStatus() == null) {
                        epic.setStatus(TaskStatus.NEW);
                    }

                    // Проверка наличия обязательных полей
                    if (epic.getName() == null || epic.getDescription() == null) {
                        sendHasInteractions(exchange);
                        return;
                    }

                    taskManager.addEpic(epic); // Добавляем эпик
                    exchange.sendResponseHeaders(201, 0); // Успешное создание
                    exchange.getResponseBody().close();

                } catch (JsonSyntaxException e) {
                    exchange.sendResponseHeaders(400, 0); // Неверный JSON
                    exchange.getResponseBody().close();
                }
                return;
            }

            // Удаление эпика по ID
            if ("DELETE".equals(method) && segments.length == 3) {
                int id = parseId(segments[2]);
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange);
                    return;
                }
                taskManager.removeEpic(id);
                exchange.sendResponseHeaders(200, 0); // Успешное удаление
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

    // Парсинг ID из строки сегмента
    private int parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
