package vn.edu.p2p.peer.session;

import vn.edu.p2p.common.dto.HeartbeatResponse;
import vn.edu.p2p.peer.auth.AuthClientException;

@FunctionalInterface
public interface HeartbeatSender {
    HeartbeatResponse send() throws AuthClientException;
}
