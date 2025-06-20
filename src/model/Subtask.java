package model;

public class Subtask extends Task {
    private int epicId; // ID эпика, к которому принадлежит подзадача

    // Конструктор с инициализацией имени, описания и ID эпика
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    // Получить ID эпика
    public int getEpicId() {
        return epicId;
    }

    // Установить ID эпика
    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }
}
