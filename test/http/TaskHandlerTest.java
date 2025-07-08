package http;

import com.google.gson.*;
import manager.Managers;
import manager.TaskManager;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskHandlerTest {
    private TaskManager taskManager;
    RealHttpCallerTest servTest = new RealHttpCallerTest();
    MockHttpCallerTest mockTest = new MockHttpCallerTest();
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

    @BeforeEach
    void setUp() {
        taskManager = Managers.getDefault();
        mockTest.setUp(taskManager);
        servTest.setUp(taskManager);
    }

    @AfterEach
    void stopServ() {
        servTest.stopServ();
    }

    //_______________________Тесты_____________________
    //создадим через сервер и проверим прямо через taskManager
    @Test
    void createTask() {
        Task toCreate = new Task("Name1", "Desc1");
        List<Task> oldTasks = mockTest.getAllTasks();
        servTest.createTask(toCreate);
        HttpResponse<String> response = servTest.getResponse();
        assertEquals(201, response.statusCode(), "Ошибка при создании (код возврата)");
        assertEquals(oldTasks.size() + 1, mockTest.getAllTasks().size(), "Не добавлено новых записей (эпик)");
        if (mockTest.getAllTasks().isEmpty()) {
            fail("Нет никаких записей эпик");
        }
        assertEquals(toCreate.getName(), mockTest.getAllTasks().getLast().getName(), "Сохранена неверная запись (эпик)");
    }

    //создадим два эпика и проверим, что через сервер два и вернутся
    @Test
    void readAllTasks() {
        //создаю
        Task toCreate = new Task("Name1", "Desc1");
        mockTest.createTask(toCreate);
        toCreate = new Task("Name2", "Desc2");
        mockTest.createTask(toCreate);

        //прошу все объекты
        servTest.getAllTasks();
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
        JsonArray jsonArray = servTest.getJsonArray();
        assertEquals(2, jsonArray.size(), "Вернулось неверное количество объектов");
        List<Task> returnedTasks = new java.util.ArrayList<>(List.of());
        for (JsonElement i : jsonArray) {
            returnedTasks.add(gson.fromJson(i, Task.class));
        }
        assertEquals(mockTest.getAllTasks(), returnedTasks, "Вернулся неверный состав объектов");
    }

    @Test
    void readTaskById() {
        Task toCreate = new Task("Name1", "Desc1");
        mockTest.createTask(toCreate);
        Task checkTask = mockTest.getAllTasks().getLast();

        servTest.getTaskById(checkTask.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении объекта (код возврата)");
        Task result = gson.fromJson(servTest.getResponse().body(), Task.class);
        assertEquals(result.getName(), toCreate.getName(), "Вернулся неверный объект");
    }

    @Test
    void deleteTaskById() {
        Task toCreate = new Task("Name1", "Desc1");
        mockTest.createTask(toCreate);
        Task checkTask = mockTest.getAllTasks().getLast();

        servTest.deleteTaskById(checkTask.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при удалении объекта (код возврата)");
        assertEquals(0, mockTest.getAllEpics().size(), "Не происходит удаления");
    }

    @Test
    void modifySubtask() {
        Task toCreate = new Task("Name1", "Desc1");
        mockTest.createTask(toCreate);
        Task checkTask = mockTest.getAllTasks().getLast();

        Task changedTask = new Subtask("Sname2", "Sdesc2", checkTask.getId());
        changedTask.setId(checkTask.getId());

        servTest.createTask(changedTask);

        assertEquals(201, servTest.getResponse().statusCode(), "Ошибка при создании (код возврата)");
        assertEquals(1, mockTest.getAllTasks().size(), "Не добавлено новых записей (таск)");
        assertEquals(changedTask.getName(), mockTest.getAllTasks().getLast().getName(), "Изменена неверная запись (таск)");
    }

    @Test
    void errorsTestPost() {
        Task toCreate = new Task("Name1", "Desc1");
        mockTest.createTask(toCreate);
        Task checkTask = mockTest.getAllTasks().getLast();

        Task changedTask = new Subtask("Sname2", "Sdesc2", checkTask.getId());
        //id фейк у поменянного
        changedTask.setId(999);

        servTest.createTask(changedTask);

        assertEquals(406, servTest.getResponse().statusCode(), "Ошибка при изменении (код возврата)");
    }

    @Test
    void errorsTest() {
        //id фейк (ничего нет)
        servTest.getTaskById(999);
        assertEquals(404, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
    }
}