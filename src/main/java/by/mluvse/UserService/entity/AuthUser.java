package by.mluvse.UserService.entity;

public record AuthUser(Long userId, String role) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
