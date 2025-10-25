package ca.gbc.comp3074.comp3074.model;

public class Game {
    private String title;
    private String status; // "played", "playing", "backlog"
    private String emoji;
    
    public Game(String title, String status, String emoji) {
        this.title = title;
        this.status = status;
        this.emoji = emoji;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
