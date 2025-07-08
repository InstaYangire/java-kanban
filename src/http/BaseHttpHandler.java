package http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Базовый класс для всех HTTP-обработчиков — содержит общие методы ответа
public class BaseHttpHandler {

    // Отправка стандартного успешного ответа с текстом (обычно JSON)
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    // Отправка ответа 404 — если объект не найден
    protected void sendNotFound(HttpExchange h) throws IOException {
        String response = "Объект не найден";
        byte[] resp = response.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    // Отправка ответа 406 — если задача пересекается с уже существующей
    protected void sendHasInteractions(HttpExchange h) throws IOException {
        String response = "Задача пересекается с уже существующей";
        byte[] resp = response.getBytes(StandardCharsets.UTF_8);
        h.sendResponseHeaders(406, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}