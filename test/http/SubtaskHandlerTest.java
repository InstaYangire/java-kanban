package http;

import com.google.gson.*;
import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SubtaskHandlerTest {
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
    void createSubtask() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast();
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        List<Subtask> oldTasks = mockTest.getAllSubtasks();
        servTest.createSubtask(sToCreate);


        assertEquals(201, servTest.getResponse().statusCode(), "Ошибка при создании (код возврата)");
        assertEquals(oldTasks.size() + 1, mockTest.getAllSubtasks().size(), "Не добавлено новых записей (сабтаск)");
        if (mockTest.getAllSubtasks().isEmpty()) {
            fail("Нет никаких записей сабтаск");
        }
        assertEquals(sToCreate.getName(), mockTest.getAllSubtasks().getLast().getName(), "Сохранена неверная запись (сабтаск)");
    }

    //создадим два сабтаска и проверим, что через сервер два и вернутся
    @Test
    void readAllSubtasks() {
        //создаю
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast();
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);
        sToCreate = new Subtask("Sname2", "Sdesc2", checkEpic.getId());
        mockTest.createSubtask(sToCreate);

        //прошу все объекты
        servTest.getAllSubtasks();
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
        JsonArray jsonArray = servTest.getJsonArray();
        assertEquals(2, jsonArray.size(), "Вернулось неверное количество объектов");
        List<Subtask> returnedSubtasks = new java.util.ArrayList<>(List.of());
        for (JsonElement i : jsonArray) {
            returnedSubtasks.add(gson.fromJson(i, Subtask.class));
        }
        assertEquals(mockTest.getAllSubtasks(), returnedSubtasks, "Вернулся неверный состав объектов");
    }

    @Test
    void readSubtaskById() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast();
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);
        Subtask checkSubtask = mockTest.getAllSubtasks().getLast();

        servTest.getSubtaskById(checkSubtask.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при чтении объекта (код возврата)");
        Subtask result = gson.fromJson(servTest.getResponse().body(), Subtask.class);
        assertEquals(result.getName(), sToCreate.getName(), "Вернулся неверный объект");
    }

    @Test
    void deleteSubtaskById() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast(); //чтобы знать id
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);
        Subtask checkSubtask = mockTest.getAllSubtasks().getLast();

        servTest.deleteSubtaskById(checkSubtask.getId());
        assertEquals(200, servTest.getResponse().statusCode(), "Ошибка при удалении объекта (код возврата)");
        assertEquals(0, mockTest.getAllSubtasks().size(), "Не происходит удаления");
    }

    @Test
    void modifySubtask() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast(); //чтобы знать id
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);
        Subtask checkSubtask = mockTest.getAllSubtasks().getLast(); //сейчас его поменяем

        Subtask changedSubtask = new Subtask("Sname2", "Sdesc2", checkEpic.getId());
        changedSubtask.setId(checkSubtask.getId());

        servTest.createSubtask(changedSubtask);

        assertEquals(201, servTest.getResponse().statusCode(), "Ошибка при создании (код возврата)");
        assertEquals(1, mockTest.getAllSubtasks().size(), "Не добавлено новых записей (сабтаск)");
        assertEquals(changedSubtask.getName(), mockTest.getAllSubtasks().getLast().getName(), "Изменена неверная запись (сабтаск)");
    }

    @Test
    void errorsTestPost() {
        Epic toCreate = new Epic("Name1", "Desc1");
        mockTest.createEpic(toCreate);
        Epic checkEpic = mockTest.getAllEpics().getLast(); //чтобы знать id
        Subtask sToCreate = new Subtask("Sname1", "Sdesc1", checkEpic.getId());
        mockTest.createSubtask(sToCreate);

        Subtask changedSubtask = new Subtask("Sname2", "Sdesc2", checkEpic.getId());
        //id фейк у поменянного
        changedSubtask.setId(999);

        servTest.createSubtask(changedSubtask);

        assertEquals(406, servTest.getResponse().statusCode(), "Ошибка при создании (код возврата)");
    }

    @Test
    void errorsTest() {
        //id фейк (ничего нет)
        servTest.getSubtaskById(999);
        assertEquals(404, servTest.getResponse().statusCode(), "Ошибка при чтении всех объектов (код возврата)");
    }
}
