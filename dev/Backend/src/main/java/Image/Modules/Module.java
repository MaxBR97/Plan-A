package Image.Modules;

public abstract class Module {
    /**
     * Common data and logic across all module types (constraints and preferences)
     */
    public Module(String name, String description) {
        this.name = name;
        this.description = description;
    }
    private String name;
    private String description;

    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}