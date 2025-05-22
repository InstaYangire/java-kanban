import manager.InMemoryTaskManager;
import model.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");

        // Проверки строго по заданию
        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Создаем две обычные задачи
        Task task1 = new Task("Купить продукты", "Хлеб, яйца, пельмешки");
        Task task2 = new Task("Сходить в аптеку", "Купить пустырник");
        manager.addTask(task1);
        manager.addTask(task2);

        // Создаем эпик с двумя подзадачами
        Epic epic1 = new Epic("Переезд", "Подготовка к переезду");
        manager.addEpic(epic1);

        Subtask sub1 = new Subtask("Упаковать вещи", "Не забыть миску кота", epic1.getId());
        Subtask sub2 = new Subtask("Сказать пока", "Сверлящему соседу", epic1.getId());
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);

        // Создаем эпик с одной подзадачей
        Epic epic2 = new Epic("Праздник", "Организовать день рождения");
        manager.addEpic(epic2);

        Subtask sub3 = new Subtask("Купить торт", "Ванильный", epic2.getId());
        manager.addSubtask(sub3);

        // Печатаем все задачи
        System.out.println("\nВсе задачи:");
        for (Task t : manager.getAllTasks()) {
            System.out.println(t.getName() + ", статус: " + t.getStatus());
        }

        System.out.println("\nВсе эпики:");
        for (Epic e : manager.getAllEpics()) {
            System.out.println(e.getName() + ", статус: " + e.getStatus());
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask s : manager.getAllSubtasks()) {
            System.out.println(s.getName() + " (эпик: " + s.getEpicId() + ") статус: " + s.getStatus());
        }

        // Меняем статусы подзадач и задач
        task1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task1);

        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubtask(sub1);
        manager.updateSubtask(sub2);

        // Печатаем после изменений
        System.out.println("\nПосле изменения статусов:");
        System.out.println("Эпик \"" + epic1.getName() + "\" имеет статус: " + manager.getEpicById(epic1.getId()).getStatus());

        // Удалим одну задачу и один эпик
        manager.removeTask(task2.getId());
        manager.removeEpic(epic2.getId());

        System.out.println("\nПосле удаления одной задачи и одного эпика:");
        System.out.println("Оставшиеся задачи: " + manager.getAllTasks().size());
        System.out.println("Оставшиеся эпики: " + manager.getAllEpics().size());

        // Проверка истории просмотров
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(sub1.getId());

        System.out.println("\nИстория просмотров:");
        for (Task viewed : manager.getHistory()) {
            System.out.println(viewed.getName() + " (ID: " + viewed.getId() + ")");
        }

        System.out.println("\n===== ДОПОЛНИТЕЛЬНЫЙ СЦЕНАРИЙ =====");

        // Создаём две задачи
        Task t1 = new Task("Задача A", "Описание A");
        Task t2 = new Task("Задача B", "Описание B");
        manager.addTask(t1);
        manager.addTask(t2);

        // Создаём эпик с тремя подзадачами
        Epic epicWithSubs = new Epic("Эпик с подзадачами", "Сценарий");
        manager.addEpic(epicWithSubs);

        Subtask s1 = new Subtask("Подзадача 1", "...", epicWithSubs.getId());
        Subtask s2 = new Subtask("Подзадача 2", "...", epicWithSubs.getId());
        Subtask s3 = new Subtask("Подзадача 3", "...", epicWithSubs.getId());
        manager.addSubtask(s1);
        manager.addSubtask(s2);
        manager.addSubtask(s3);

        // Создаём эпик без подзадач
        Epic epicEmpty = new Epic("Эпик без подзадач", "Тест");
        manager.addEpic(epicEmpty);

        // Просматриваем задачи и подзадачи в разном порядке
        manager.getTaskById(t1.getId());
        manager.getEpicById(epicEmpty.getId());
        manager.getSubtaskById(s2.getId());
        manager.getEpicById(epicWithSubs.getId());
        manager.getTaskById(t2.getId());
        manager.getSubtaskById(s1.getId());
        manager.getSubtaskById(s3.getId());

        // Выводим историю
        System.out.println("\nИстория после просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getName() + " (ID: " + task.getId() + ")");
        }

        // Повторный просмотр одной задачи — должна переместиться в конец
        manager.getTaskById(t1.getId());
        System.out.println("\nИстория после повторного просмотра t1:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getName() + " (ID: " + task.getId() + ")");
        }

        // Удаляем задачу t2
        manager.removeTask(t2.getId());
        System.out.println("\nИстория после удаления t2:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getName() + " (ID: " + task.getId() + ")");
        }

        // Удаляем эпик с подзадачами
        manager.removeEpic(epicWithSubs.getId());
        System.out.println("\nИстория после удаления эпика с подзадачами:");
        for (Task task : manager.getHistory()) {
            System.out.println(task.getName() + " (ID: " + task.getId() + ")");
        }
    }
}
