package manager;

import model.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    // Узел двусвязного списка истории
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

    // Начало и конец списка просмотров
    private Node head;
    private Node tail;

    // Храним соответствие: id задачи - узел в списке
    private final Map<Integer, Node> nodeMap = new HashMap<>();

    // Добавляем задачу в конец истории
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId());
        linkLast(task);
        nodeMap.put(task.getId(), tail);
    }

    // Удаляем задачу из истории по id
    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    // Возвращаем список истории просмотров
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    // Метод добавляет задачу в конец двусвязного списка
    private void linkLast(Task task) {
        Node newNode = new Node(tail, task, null);
        if (tail != null) {
            tail.next = newNode;
        } else {
            head = newNode; // если список был пуст
        }
        tail = newNode;
        nodeMap.put(task.getId(), newNode);
    }

    // Удаляем узел из списка
    private void removeNode(Node node) {
        if (node == null) return;

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        // Удаляем все ссылки из узла, чтобы не мешал сборщику мусора
        node.prev = null;
        node.next = null;
    }

    // Возвращаем все задачи из истории в виде списка
    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head;
        while (current != null) {
            result.add(current.task);
            current = current.next;
        }
        return result;
    }
}

