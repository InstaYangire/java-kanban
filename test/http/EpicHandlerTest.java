package http;

import com.google.gson.*;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicHandlerTest {
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
    void createEpic() {
        Epic toCreate = new Epic("Name1", "Desc1");
        List<Epic> oldTasks = mockTest.getAllEpics();
        servTest.createEpic(toCreate);
        HttpResponse<String> response = servTest.getResponse();
        assertEquals(201, response.statusCode(), "Ошибка при создании (код возврата)");
        assertEquals(oldTasks.size() + 1, mockTest.getAllEpics().size(), "Не добавлено новых записей (эпик)");
        if (mockTest.getAllEpics().isEmpty()) {
            fail("Нет никаких записей эпик");
        }
        assertEquals(toCreate.getName(), mockTest.getAllEpics().getLast().getName(), "Сохранена неверная запись (эпик)");
    }

    //создадим два эпика и проверим, что через сервер два и вернутся
    @Test
    void readAllEpics() {
        //создаю
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        toCreate = new Epic("Name2", "Desc2");
        mockTest.createEpic(toCreate);

        //прошу все объекты
        servTest.getAllEpics();
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
        JsonArray jsonArray = servTest.getJsonArray();
        assertEquals(2, jsonArray.size(), "Вернулось неверное количество объектов");
        List<Epic> returnedEpics = new java.util.ArrayList<>(List.of());
        for (JsonElement i : jsonArray) {
            returnedEpics.add(gson.fromJson(i, Epic.class));
        }
        assertEquals(mockTest.getAllEpics(), returnedEpics, "Вернулся неверный состав объектов");
    }

    @Test
    void readEpicById() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast();

        servTest.getEpicById(checkEpic.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении объекта (код возврата)");
        Epic result = gson.fromJson(servTest.getResponse().body(), Epic.class);
        assertEquals(result.getName(), toCreate.getName(), "Вернулся неверный объект");
    }

    @Test
    void deleteEpicById() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast(); //чтобы знать id

        servTest.deleteEpicById(checkEpic.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при удалении объекта (код возврата)");
        assertEquals(0, mockTest.getAllEpics().size(), "Не происходит удаления");
    }

    //релевантно к epic, если путь epics/...
    @Test
    void getAllEpicSubtasks() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast(); //чтобы знать id
        //для усложнения создам второй
        toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic2 = mockTest.getAllEpics().getLast(); //чтобы знать id последнего

        //к каждому эпику по сабтаску
        Subtask sToCreate = new Subtask("St1", "StDesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);
        sToCreate = new Subtask("St2", "StDesc2", checkEpic2.getId());
        mockTest.createSubtask(sToCreate);

        //проверим второй
        servTest.getEpicSubtasksById(checkEpic2.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
        JsonArray jsonArray = servTest.getJsonArray();
        assertEquals(1, jsonArray.size(), "Вернулось неверное количество объектов");
        Subtask result = gson.fromJson(jsonArray.asList().getLast(), Subtask.class);
        assertEquals(result.getName(), sToCreate.getName(), "Вернулся неверный объект");

    }

    @Test
    void errorsTest() {
        //id фейк (ничего нет)
        servTest.getEpicById(999);
        assertEquals(404, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");

        //id фейк + нет сабтасков
        servTest.getEpicSubtasksById(888);
        assertEquals(404, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
    }
}