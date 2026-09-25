package vn.edu.p2p.common.dto;

/**
 * Ask Tracker for online Peer sources that currently share a file.
 */
public record FileSourcesRequest(
        String sessionId,
        long fileId
) {
}
