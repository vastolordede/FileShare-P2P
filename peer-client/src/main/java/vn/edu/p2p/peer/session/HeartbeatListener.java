package vn.edu.p2p.peer.session;

import vn.edu.p2p.common.dto.HeartbeatResponse;

public interface HeartbeatListener {
    void onHeartbeat(HeartbeatResponse response);

    void onStateChanged(PeerConnectionState state, String message);
}
