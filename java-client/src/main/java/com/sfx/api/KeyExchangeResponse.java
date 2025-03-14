package com.sfx.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response model for key exchange initialization
 */
public class KeyExchangeResponse {
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("public_key")
    private String publicKey;
    
    @JsonProperty("parameters")
    private String parameters;

    // Default constructor for Jackson
    public KeyExchangeResponse() {}

    public KeyExchangeResponse(String sessionId, String publicKey, String parameters) {
        this.sessionId = sessionId;
        this.publicKey = publicKey;
        this.parameters = parameters;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
