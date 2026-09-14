package com.studyflow.model;

/**
 * A course the student is enrolled in. Tasks belong to a course.
 */
public class Course {

    private int id;
    private int userId;
    private String name;
    private String code;
    private String semester;

    /** Creates an unsaved course (no id yet). */
    public Course(int userId, String name, String code, String semester) {
        this(0, userId, name, code, semester);
    }

    public Course(int id, int userId, String name, String code, String semester) {
        this.id = id;
        setUserId(userId);
        setName(name);
        this.code = code;
        this.semester = semester;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public final void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("userId must be positive, was: " + userId);
        }
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public final void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be null or blank");
        }
        this.name = name.trim();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    /** Label for the UI: "TX00EY27 — Ohjelmistotuotantoprojekti 1". */
    public String displayName() {
        return (code == null || code.isBlank()) ? name : code + " — " + name;
    }

    @Override
    public String toString() {
        return "Course{id=%d, userId=%d, name='%s', code='%s', semester='%s'}"
                .formatted(id, userId, name, code, semester);
    }
}
