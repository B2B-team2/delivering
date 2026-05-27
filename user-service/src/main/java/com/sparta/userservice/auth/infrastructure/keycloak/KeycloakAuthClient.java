package com.sparta.userservice.auth.infrastructure.keycloak;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sparta.userservice.global.config.keycloak.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAuthClient {

    private final KeycloakProperties keycloakProperties;
    private final RestTemplate restTemplate;

    private org.keycloak.admin.client.resource.UsersResource getUsersResource(Keycloak keycloak) {
        return keycloak.realm(keycloakProperties.getRealm()).users();
    }

    public String getEmailByKeycloakId(String keycloakId) {
        try (Keycloak keycloak = buildAdminKeycloak()) {
            var userRepresentation = keycloak.realm(keycloakProperties.getRealm())
                    .users()
                    .get(keycloakId)
                    .toRepresentation();
            return userRepresentation.getEmail();
        }
    }

    public UUID createUser(String email, String password, String role) {
        try (Keycloak keycloak = buildAdminKeycloak()) {

            CredentialRepresentation credential = createPasswordCredential(password);
            UserRepresentation user = createUserRepresentation(email, credential, role);

            var realmResource = keycloak.realm(keycloakProperties.getRealm());

            try (var response = realmResource.users().create(user)) {

                if (response.getStatus() == 409) {
                    throw new IllegalStateException("[Keycloak] 이미 존재하는 유저입니다.");
                }
                if (response.getStatus() >= 400) {
                    throw new IllegalStateException("[Keycloak] 유저 생성 실패 - status=" + response.getStatus());
                }

                String userId = response.getLocation().getPath().replaceAll(".*/", "");
                var roleRepresentation = realmResource.roles().get(role).toRepresentation();
                realmResource.users().get(userId).roles().realmLevel().add(List.of(roleRepresentation));

                log.info("[Keycloak] 유저 생성 및 Role 할당 완료. - email={}, role={}", email, role);
                return UUID.fromString(userId);
            }
        }
    }

    public boolean existsUser(String email) {
        try (Keycloak keycloak = buildAdminKeycloak()) {
            var users = keycloak.realm(keycloakProperties.getRealm())
                    .users()
                    .searchByUsername(email, true);
            return !users.isEmpty();
        }
    }

    public void deleteUser(String email) {
        try (Keycloak keycloak = buildAdminKeycloak()) {
            var usersResource = keycloak.realm(keycloakProperties.getRealm()).users();
            var users = usersResource.searchByUsername(email, true);
            if (users.isEmpty()) {
                return;
            }
            try (var response = usersResource.delete(users.get(0).getId())) {
                if (response.getStatus() >= 400) {
                    throw new IllegalStateException(
                            "[Keycloak] 유저 삭제 실패 - status=" + response.getStatus()
                    );
                }
                log.info("[Keycloak] 유저 삭제 완료 - email={}", email);
            }
        }
    }

    public void enableUser(String email) {
        updateUserEnabled(email, true);
    }

    public void disableUser(String email) {
        updateUserEnabled(email, false);
    }

    public KeycloakTokenResponse login(String email, String password) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "password");
        body.add("client_id", keycloakProperties.getClientId());
        body.add("client_secret", keycloakProperties.getClientSecret());
        body.add("username", email);
        body.add("password", password);

        Map<String, Object> response = requestToken(body);

        return new KeycloakTokenResponse(
                (String) response.get("access_token"),
                (String) response.get("refresh_token"),
                (String) response.get("token_type"),
                ((Number) response.get("expires_in")).longValue(),
                ((Number) response.get("refresh_expires_in")).longValue()
        );
    }

    public KeycloakTokenResponse refresh(String refreshToken) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", "refresh_token");
        body.add("client_id", keycloakProperties.getClientId());
        body.add("client_secret", keycloakProperties.getClientSecret());
        body.add("refresh_token", refreshToken);

        Map<String, Object> response = requestToken(body);

        return new KeycloakTokenResponse(
                (String) response.get("access_token"),
                (String) response.get("refresh_token"),
                (String) response.get("token_type"),
                ((Number) response.get("expires_in")).longValue(),
                ((Number) response.get("refresh_expires_in")).longValue()
        );
    }

    public String extractEmail(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.decode(refreshToken);
            String keycloakId = decodedJWT.getSubject();
            return getEmailByKeycloakId(keycloakId);
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
        }
    }

    public void logout(String refreshToken) {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("client_id", keycloakProperties.getClientId());
        body.add("client_secret", keycloakProperties.getClientSecret());
        body.add("refresh_token", refreshToken);

        restTemplate.postForObject(
                keycloakProperties.getLogoutUrl(),
                buildFormRequest(body),
                Void.class
        );

        log.info("[Keycloak] 로그아웃 완료");
    }

    private void updateUserEnabled(String email, boolean enabled) {
        try (Keycloak keycloak = buildAdminKeycloak()) {

            var usersResource = getUsersResource(keycloak);

            var users = usersResource.searchByUsername(email, true);

            if (users.isEmpty()) {
                return;
            }

            var userResource = usersResource.get(users.get(0).getId());
            var userRep = userResource.toRepresentation();

            userRep.setEnabled(enabled);
            userResource.update(userRep);

            log.info(
                    "[Keycloak] 유저 {} 완료 - email={}",
                    enabled ? "활성화" : "비활성화",
                    email
            );
        }
    }
    
    public void updateUserAttribute(String email, String key, String value) {
        try (Keycloak keycloak = buildAdminKeycloak()) {

            var usersResource = getUsersResource(keycloak);
            var users = usersResource.searchByUsername(email, true);

            if (users.isEmpty()) {
                return;
            }

            var userResource = usersResource.get(users.get(0).getId());
            var userRep = userResource.toRepresentation();

            Map<String, List<String>> attrs = userRep.getAttributes();
            if (attrs == null) {
                attrs = new java.util.HashMap<>();
            }
            attrs.put(key, List.of(value));
            userRep.setAttributes(attrs);
            userResource.update(userRep);

            log.info("[Keycloak] 유저 attribute 업데이트 완료 - email={}, {}={}", email, key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requestToken(MultiValueMap<String, String> body) {
        return restTemplate.postForObject(
                keycloakProperties.getTokenUrl(),
                buildFormRequest(body),
                Map.class
        );
    }

    private HttpEntity<MultiValueMap<String, String>> buildFormRequest(
            MultiValueMap<String, String> body
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(body, headers);
    }

    private CredentialRepresentation createPasswordCredential(String password) {

        CredentialRepresentation credential = new CredentialRepresentation();

        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        return credential;
    }

    private UserRepresentation createUserRepresentation(
            String email,
            CredentialRepresentation credential,
            String role
    ) {

        UserRepresentation user = new UserRepresentation();

        user.setUsername(email);
        user.setEmail(email);

        user.setEnabled(false);
        user.setEmailVerified(true);

        user.setCredentials(List.of(credential));
        user.setAttributes(Map.of("role", List.of(role)));

        return user;
    }

    private Keycloak buildAdminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(keycloakProperties.getAdminUsername())
                .password(keycloakProperties.getAdminPassword())
                .build();
    }
}