package vn.edu.p2p.common.protocol;

/**
 * Protocol v1 message types shared by Tracker and Peer.
 *
 * Other team members should extend this enum instead of creating
 * another command format.
 */
public enum MessageType {
    LOGIN_REQUEST,
    LOGIN_RESPONSE,

    LOGOUT_REQUEST,
    LOGOUT_RESPONSE,

    HEARTBEAT,
    HEARTBEAT_ACK,

    FILE_SOURCES_REQUEST,
    FILE_SOURCES_RESPONSE,

    /** Generic protocol-level error when no request-specific response exists. */
    ERROR
}
