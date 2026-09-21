package vn.edu.p2p.common.dto;

public record LoginRequest(
        String username,
        String password,
        int listeningPort
) {
}
