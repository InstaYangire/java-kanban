package http;

import com.google.gson.*;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.fail;

public class RealHttpCallerTest {
    private HttpTaskServer server;
    private HttpResponse<String> response;
    private TaskManager taskManager;
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


    HttpResponse<String> getResponse() {
        return response;
    }

    void setUp(TaskManager taskManager) {
        this.taskManager = taskManager;
        try {
            server = new HttpTaskServer(this.taskManager);
        } catch (IOException e) {
            return;
        }
        server.start();
    }

    void stopServ() {
        server.stop();
    }

    HttpClient getClient() {
        return HttpClient.newHttpClient();
    }

    void sendRequest(HttpRequest request) {
        try {
            response = getClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            fail("Не получается отправить запрос");
        }
    }

    JsonArray getJsonArray() {
        if (response != null) {
            String body = response.body();
            return JsonParser.parseString(body).getAsJsonArray();
        } else {
            return new JsonArray();
        }
    }

    //------------Доступ до функций сервера--------------
    void createEpic(Epic epic) {
        String json = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        //execute sending
        sendRequest(request);
    }

    void createSubtask(Subtask subtask) {
        String json = gson.toJson(subtask);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        //execute sending
        sendRequest(request);
    }

    void createTask(Task task) {
        String json = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        //execute sending
        sendRequest(request);
    }

    void deleteEpicById(int id) {
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        //execute sending
        sendRequest(request);
    }

    void deleteSubtaskById(int id) {
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        //execute sending
        sendRequest(request);
    }

    void deleteTaskById(int id) {
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        //execute sending
        sendRequest(request);
    }

    void getAllEpics() {
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getAllSubtasks() {
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getAllTasks() {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getEpicById(int id) {
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getSubtaskById(int id) {
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getTaskById(int id) {
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }

    void getEpicSubtasksById(int id) {
        URI url = URI.create("http://localhost:8080/epics/" + id + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        sendRequest(request);
    }
}
