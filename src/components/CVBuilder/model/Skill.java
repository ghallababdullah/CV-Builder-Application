
package components.CVBuilder.model;

public class Skill {
    private int id;
    private int cvId;
    private String name;
    private int level; // 1-5

    public Skill() {
        // Default constructor
    }

    public Skill(String name, int level) {
        this.name = name;
        this.level = level;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCvId() {
        return cvId;
    }

    public void setCvId(int cvId) {
        this.cvId = cvId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
