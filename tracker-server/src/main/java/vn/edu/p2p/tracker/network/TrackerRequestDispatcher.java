package vn.edu.p2p.tracker.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import vn.edu.p2p.common.dto.FileSourcesRequest;
import vn.edu.p2p.common.dto.FileSourcesResponse;
import vn.edu.p2p.common.dto.HeartbeatRequest;
import vn.edu.p2p.common.dto.HeartbeatResponse;
import vn.edu.p2p.common.dto.LoginRequest;
import vn.edu.p2p.common.dto.LoginResponse;
import vn.edu.p2p.common.dto.LogoutRequest;
import vn.edu.p2p.common.protocol.MessageEnvelope;
import vn.edu.p2p.common.protocol.MessageType;
import vn.edu.p2p.common.protocol.ProtocolCodec;
import vn.edu.p2p.tracker.auth.AuthException;
import vn.edu.p2p.tracker.auth.AuthService;
import vn.edu.p2p.tracker.peer.PeerSessionService;
import vn.edu.p2p.tracker.peer.SessionException;
import vn.edu.p2p.tracker.source.FileSourceException;
import vn.edu.p2p.tracker.source.FileSourceService;

public final class TrackerRequestDispatcher {
    private final AuthService authService;
    private final PeerSessionService peerSessionService;
    private final FileSourceService fileSourceService;
    private final RequestValidator validator;

    public TrackerRequestDispatcher(
            AuthService authService,
            PeerSessionService peerSessionService
    ) {
        this(authService, peerSessionService, null, new RequestValidator());
    }

    public TrackerRequestDispatcher(
            AuthService authService,
            PeerSessionService peerSessionService,
            FileSourceService fileSourceService
    ) {
        this(authService, peerSessionService, fileSourceService, new RequestValidator());
    }

    TrackerRequestDispatcher(
            AuthService authService,
            PeerSessionService peerSessionService,
            FileSourceService fileSourceService,
            RequestValidator validator
    ) {
        this.authService = authService;
        this.peerSessionService = peerSessionService;
        this.fileSourceService = fileSourceService;
        this.validator = validator;
    }

    public MessageEnvelope dispatch(MessageEnvelope request, String remoteIp) {
        try {
            validator.validateEnvelope(request);

            return switch (request.type()) {
                case LOGIN_REQUEST -> handleLogin(request, remoteIp);
                case LOGOUT_REQUEST -> handleLogout(request);
                case HEARTBEAT -> handleHeartbeat(request);
                case FILE_SOURCES_REQUEST -> handleFileSources(request);
                case LOGIN_RESPONSE, LOGOUT_RESPONSE, HEARTBEAT_ACK,
                        FILE_SOURCES_RESPONSE, ERROR ->
                        TrackerErrorResponses.forRequest(
                                request,
                                "UNSUPPORTED_MESSAGE",
                                "Tracker accepts request messages only."
                        );
            };
        } catch (RequestValidationException e) {
            return TrackerErrorResponses.forRequest(
                    request, e.errorCode(), e.getMessage()
            );
        } catch (JsonProcessingException e) {
            return TrackerErrorResponses.forRequest(
                    request,
                    "INVALID_REQUEST",
                    "Request payload does not match the expected format."
            );
        } catch (AuthException e) {
            return TrackerErrorResponses.forRequest(
                    request, e.errorCode(), e.getMessage()
            );
        } catch (SessionException e) {
            return TrackerErrorResponses.forRequest(
                    request, e.errorCode(), e.getMessage()
            );
        } catch (FileSourceException e) {
            return TrackerErrorResponses.forRequest(
                    request, e.errorCode(), e.getMessage()
            );
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return TrackerErrorResponses.forRequest(
                    request,
                    "SERVER_ERROR",
                    "Tracker failed to process the request."
            );
        }
    }

    private MessageEnvelope handleLogin(
            MessageEnvelope request,
            String remoteIp
    ) throws Exception {
        LoginRequest payload = ProtocolCodec.fromPayload(
                request.payload(), LoginRequest.class
        );
        validator.validateLogin(payload);
        LoginResponse response = authService.login(payload, remoteIp);

        return MessageEnvelope.success(
                MessageType.LOGIN_RESPONSE,
                request.requestId(),
                ProtocolCodec.toPayload(response)
        );
    }

    private MessageEnvelope handleLogout(MessageEnvelope request) throws Exception {
        LogoutRequest payload = ProtocolCodec.fromPayload(
                request.payload(), LogoutRequest.class
        );
        validator.validateLogout(payload);
        authService.logout(payload);

        return MessageEnvelope.success(
                MessageType.LOGOUT_RESPONSE,
                request.requestId(),
                null
        );
    }

    private MessageEnvelope handleHeartbeat(MessageEnvelope request) throws Exception {
        HeartbeatRequest payload = ProtocolCodec.fromPayload(
                request.payload(), HeartbeatRequest.class
        );
        validator.validateHeartbeat(payload);
        HeartbeatResponse response = peerSessionService.heartbeat(payload);

        return MessageEnvelope.success(
                MessageType.HEARTBEAT_ACK,
                request.requestId(),
                ProtocolCodec.toPayload(response)
        );
    }

    private MessageEnvelope handleFileSources(MessageEnvelope request) throws Exception {
        if (fileSourceService == null) {
            return TrackerErrorResponses.forRequest(
                    request,
                    "FEATURE_UNAVAILABLE",
                    "File-source discovery is not configured on this Tracker."
            );
        }

        FileSourcesRequest payload = ProtocolCodec.fromPayload(
                request.payload(), FileSourcesRequest.class
        );
        validator.validateFileSources(payload);
        FileSourcesResponse response = fileSourceService.findSources(payload);

        return MessageEnvelope.success(
                MessageType.FILE_SOURCES_RESPONSE,
                request.requestId(),
                ProtocolCodec.toPayload(response)
        );
    }
}
