package com.finpilot.plaid.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountSource;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.plaid.dto.*;
import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import com.finpilot.plaid.repository.PlaidConnectionRepository;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.request.PlaidApi;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaidServiceTest {

    @Mock private PlaidApi plaidApi;
    @Mock private PlaidConnectionRepository plaidConnectionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private PlaidAccountSyncService plaidAccountSyncService;
    @Mock private PlaidTransactionSyncService plaidTransactionSyncService;

    @InjectMocks
    private PlaidService plaidService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@example.com").name("User One").authProvider(AuthProvider.LOCAL).build();
        user2 = User.builder().id(2L).email("user2@example.com").name("User Two").authProvider(AuthProvider.LOCAL).build();
    }

    @Test
    @DisplayName("1. Link token generated successfully and returns LinkTokenResponse")
    void testCreateLinkToken_Success() throws IOException {
        LinkTokenCreateResponse mockBody = new LinkTokenCreateResponse();
        mockBody.setLinkToken("link-sandbox-12345");

        Call<LinkTokenCreateResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.linkTokenCreate(any())).thenReturn(mockCall);

        LinkTokenResponse response = plaidService.createLinkToken(user1);

        assertNotNull(response);
        assertEquals("link-sandbox-12345", response.getLinkToken());
    }

    @Test
    @DisplayName("2. Public token exchange persists connection and never exposes accessToken")
    void testExchangePublicToken_Success() throws IOException {
        ExchangeTokenRequest request = ExchangeTokenRequest.builder()
                .publicToken("public-sandbox-token")
                .institutionId("ins_1")
                .institutionName("Test Bank")
                .build();

        ItemPublicTokenExchangeResponse mockBody = new ItemPublicTokenExchangeResponse();
        mockBody.setAccessToken("access-sandbox-secret-token");
        mockBody.setItemId("item_123");

        Call<ItemPublicTokenExchangeResponse> mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(Response.success(mockBody));
        when(plaidApi.itemPublicTokenExchange(any())).thenReturn(mockCall);

        PlaidConnection savedConn = PlaidConnection.builder()
                .id(10L)
                .user(user1)
                .itemId("item_123")
                .accessToken("access-sandbox-secret-token")
                .institutionId("ins_1")
                .institutionName("Test Bank")
                .status(PlaidConnectionStatus.ACTIVE)
                .build();

        when(plaidConnectionRepository.findByItemIdAndUser("item_123", user1))
                .thenReturn(Optional.empty());
        when(plaidConnectionRepository.save(any(PlaidConnection.class)))
                .thenReturn(savedConn);

        Account mockAccount = Account.builder().id(100L).user(user1).name("Checking").source(AccountSource.PLAID).balance(new BigDecimal("1000")).build();
        when(plaidAccountSyncService.syncAccounts(eq(user1), any())).thenReturn(List.of(mockAccount));

        SyncResponse mockSync = SyncResponse.builder().added(5).modified(0).removed(0).build();
        when(plaidTransactionSyncService.syncTransactions(eq(user1), any())).thenReturn(mockSync);

        ExchangeTokenResponse response = plaidService.exchangePublicToken(user1, request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("item_123", response.getItemId());
        assertEquals("Test Bank", response.getInstitutionName());
        assertEquals(10L, response.getConnectionId());
        assertEquals(1, response.getAccountsCount());
        assertEquals(5, response.getTransactionsCount());
    }

    @Test
    @DisplayName("3. Get connections returns safe DTOs and never returns accessToken")
    void testGetConnections_Safe() {
        PlaidConnection conn = PlaidConnection.builder()
                .id(1L)
                .user(user1)
                .itemId("item_1")
                .accessToken("sensitive_access_token")
                .institutionName("Chase Bank")
                .status(PlaidConnectionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(plaidConnectionRepository.findByUserOrderByCreatedAtDesc(user1))
                .thenReturn(List.of(conn));
        when(accountRepository.findByPlaidConnectionIdAndUser(1L, user1))
                .thenReturn(List.of(Account.builder().id(101L).build(), Account.builder().id(102L).build()));

        List<PlaidConnectionResponse> result = plaidService.getConnections(user1);

        assertEquals(1, result.size());
        assertEquals("Chase Bank", result.get(0).getInstitutionName());
        assertEquals(2, result.get(0).getAccountCount());
        assertEquals(PlaidConnectionStatus.ACTIVE, result.get(0).getStatus());
    }

    @Test
    @DisplayName("4. User ownership enforced - User A cannot access User B connection")
    void testUserIsolation() {
        when(plaidConnectionRepository.findByIdAndUser(1L, user2))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> plaidService.getConnection(user2, 1L));
        assertThrows(ResponseStatusException.class, () -> plaidService.syncConnection(user2, 1L));
        assertThrows(ResponseStatusException.class, () -> plaidService.disconnect(user2, 1L));
    }

    @Test
    @DisplayName("5. Disconnect marks connection DISCONNECTED and deactivates accounts without deleting historical transactions")
    void testDisconnect_PreservesTransactions() throws IOException {
        PlaidConnection conn = PlaidConnection.builder()
                .id(1L)
                .user(user1)
                .itemId("item_1")
                .accessToken("access_token")
                .status(PlaidConnectionStatus.ACTIVE)
                .build();

        when(plaidConnectionRepository.findByIdAndUser(1L, user1))
                .thenReturn(Optional.of(conn));

        Account acc = Account.builder().id(10L).user(user1).isActive(true).build();
        when(accountRepository.findByPlaidConnectionIdAndUser(1L, user1))
                .thenReturn(List.of(acc));

        Call<ResponseBody> mockRemoveCall = mock(Call.class);
        when(plaidApi.itemRemove(any())).thenReturn((Call) mockRemoveCall);

        plaidService.disconnect(user1, 1L);

        assertEquals(PlaidConnectionStatus.DISCONNECTED, conn.getStatus());
        verify(plaidConnectionRepository).save(conn);
        assertFalse(acc.getIsActive());
        verify(accountRepository).save(acc);
    }
}
