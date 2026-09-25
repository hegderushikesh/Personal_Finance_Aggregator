package com.finpilot.plaid.controller;

import com.finpilot.plaid.dto.*;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import com.finpilot.plaid.service.PlaidService;
import com.finpilot.security.UserPrincipal;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaidControllerTest {

    @Mock private PlaidService plaidService;
    @InjectMocks private PlaidController plaidController;

    private UserPrincipal testUserPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("FinPilot User")
                .email("user@finpilot.com")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();
        testUserPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    @DisplayName("POST /api/plaid/link-token returns 200 OK when authenticated")
    void testCreateLinkToken_Authenticated() {
        when(plaidService.createLinkToken(testUser))
                .thenReturn(LinkTokenResponse.builder().linkToken("link-sandbox-test").build());

        ResponseEntity<LinkTokenResponse> response = plaidController.createLinkToken(testUserPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("link-sandbox-test", response.getBody().getLinkToken());
    }

    @Test
    @DisplayName("POST /api/plaid/link-token returns 401 UNAUTHORIZED when not authenticated")
    void testCreateLinkToken_Unauthenticated() {
        ResponseEntity<LinkTokenResponse> response = plaidController.createLinkToken(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("POST /api/plaid/exchange-token returns 200 OK when authenticated")
    void testExchangeToken_Authenticated() {
        ExchangeTokenRequest req = ExchangeTokenRequest.builder()
                .publicToken("public-test")
                .institutionName("Bank")
                .build();

        when(plaidService.exchangePublicToken(eq(testUser), any()))
                .thenReturn(ExchangeTokenResponse.builder().success(true).itemId("item_1").build());

        ResponseEntity<ExchangeTokenResponse> response = plaidController.exchangePublicToken(testUserPrincipal, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("GET /api/plaid/connections returns 200 OK when authenticated")
    void testGetConnections() {
        PlaidConnectionResponse mockResp = PlaidConnectionResponse.builder()
                .id(1L)
                .institutionName("Test Bank")
                .status(PlaidConnectionStatus.ACTIVE)
                .accountCount(2)
                .createdAt(LocalDateTime.now())
                .build();

        when(plaidService.getConnections(testUser)).thenReturn(List.of(mockResp));

        ResponseEntity<List<PlaidConnectionResponse>> response = plaidController.getConnections(testUserPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Bank", response.getBody().get(0).getInstitutionName());
    }

    @Test
    @DisplayName("POST /api/plaid/sync/{id} returns 200 OK")
    void testSyncConnection() {
        SyncResponse mockSync = SyncResponse.builder().added(2).modified(0).removed(0).build();
        when(plaidService.syncConnection(testUser, 1L)).thenReturn(mockSync);

        ResponseEntity<SyncResponse> response = plaidController.syncConnection(testUserPrincipal, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getAdded());
    }

    @Test
    @DisplayName("DELETE /api/plaid/connections/{id} returns 204 NO CONTENT")
    void testDisconnect() {
        doNothing().when(plaidService).disconnect(testUser, 1L);

        ResponseEntity<Void> response = plaidController.disconnect(testUserPrincipal, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(plaidService).disconnect(testUser, 1L);
    }
}
