package http;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

// HTTP-сервер для обработки API-запросов пользователя
public class HttpTaskServer {
    private static final int PORT = 8080;
    private HttpServer server;
    private final TaskManager taskManager;

    // Конструктор сервера, инициализация менеджера задач
    public HttpTaskServer(TaskManager taskManagerIn) throws IOException {
        this.taskManager = taskManagerIn;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Регистрируем обработчик Задач
        server.createContext("/tasks", new TaskHandler(taskManager));
        // Регистрируем обработчик Эпиков
        server.createContext("/epics", new EpicHandler(taskManager));
        // Регистрируем обработчик Подзадач
        server.createContext("/subtasks", new SubtaskHandler(taskManager));
        // Регистрируем список задач по приоритету
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
        //история
        server.createContext("/history", new HistoryHandler(taskManager));
    }

    // Метод запуска сервера
    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    // Метод остановки сервера
    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    // Метод main — точка входа для запуска приложения
    public static void main(String[] args) {
        try {
            HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
            httpTaskServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске HTTP-сервера: " + e.getMessage());
        }
    }
}
