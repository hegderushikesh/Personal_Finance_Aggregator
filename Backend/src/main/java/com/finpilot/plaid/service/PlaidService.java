package com.finpilot.plaid.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.plaid.dto.*;
import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import com.finpilot.plaid.repository.PlaidConnectionRepository;
import com.finpilot.user.entity.User;
import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaidService {

    private final PlaidApi plaidApi;
    private final PlaidConnectionRepository plaidConnectionRepository;
    private final AccountRepository accountRepository;
    private final PlaidAccountSyncService plaidAccountSyncService;
    private final PlaidTransactionSyncService plaidTransactionSyncService;

    /**
     * Creates a Link Token for initializing Plaid Link in the frontend.
     */
    public LinkTokenResponse createLinkToken(User user) {
        log.info("Creating Plaid link token for user ID {}", user.getId());

        try {
            LinkTokenCreateRequestUser linkUser = new LinkTokenCreateRequestUser()
                    .clientUserId(String.valueOf(user.getId()));

            LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                    .user(linkUser)
                    .clientName("FinPilot")
                    .products(Collections.singletonList(Products.TRANSACTIONS))
                    .countryCodes(Collections.singletonList(CountryCode.US))
                    .language("en");

            Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                log.error("Plaid link token creation failed with HTTP {}: {}", response.code(), errorBody);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create Link token with Plaid: " + response.code());
            }

            String linkToken = response.body().getLinkToken();
            log.info("Successfully generated Plaid link token for user ID {}", user.getId());
            return LinkTokenResponse.builder().linkToken(linkToken).build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during link token creation: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not initialize bank connection: " + e.getMessage());
        }
    }

    /**
     * Exchanges public token for access token and item ID, saves the connection,
     * and triggers automatic initial synchronization of accounts and transactions.
     */
    @Transactional
    public ExchangeTokenResponse exchangePublicToken(User user, ExchangeTokenRequest request) {
        log.info("Exchanging public token for user ID {}", user.getId());

        try {
            ItemPublicTokenExchangeRequest exchangeRequest = new ItemPublicTokenExchangeRequest()
                    .publicToken(request.getPublicToken());

            Response<ItemPublicTokenExchangeResponse> response = plaidApi.itemPublicTokenExchange(exchangeRequest).execute();

            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                log.error("Public token exchange failed with HTTP {}: {}", response.code(), errorBody);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to exchange public token with Plaid: " + response.code());
            }

            String accessToken = response.body().getAccessToken();
            String itemId = response.body().getItemId();

            // Check if connection already exists for this itemId and user
            PlaidConnection connection = plaidConnectionRepository.findByItemIdAndUser(itemId, user)
                    .orElseGet(() -> PlaidConnection.builder()
                            .user(user)
                            .itemId(itemId)
                            .build());

            connection.setAccessToken(accessToken);
            connection.setInstitutionId(request.getInstitutionId());
            connection.setInstitutionName(
                    request.getInstitutionName() != null && !request.getInstitutionName().isBlank()
                            ? request.getInstitutionName()
                            : "Connected Bank"
            );
            connection.setStatus(PlaidConnectionStatus.ACTIVE);

            PlaidConnection savedConnection = plaidConnectionRepository.save(connection);
            log.info("Saved PlaidConnection ID {} (Item ID {}) for user ID {}",
                    savedConnection.getId(), itemId, user.getId());

            // 1. Initial Account Sync
            List<Account> syncedAccounts = plaidAccountSyncService.syncAccounts(user, savedConnection);

            // 2. Initial Transaction Sync
            SyncResponse syncResponse = plaidTransactionSyncService.syncTransactions(user, savedConnection);

            return ExchangeTokenResponse.builder()
                    .success(true)
                    .itemId(itemId)
                    .institutionName(savedConnection.getInstitutionName())
                    .connectionId(savedConnection.getId())
                    .accountsCount(syncedAccounts.size())
                    .transactionsCount(syncResponse.getAdded())
                    .build();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception during token exchange: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to connect bank account: " + e.getMessage());
        }
    }

    /**
     * Gets all Plaid connections for the authenticated user. Never returns access tokens.
     */
    @Transactional(readOnly = true)
    public List<PlaidConnectionResponse> getConnections(User user) {
        List<PlaidConnection> connections = plaidConnectionRepository.findByUserOrderByCreatedAtDesc(user);
        return connections.stream()
                .map(c -> {
                    int accountCount = accountRepository.findByPlaidConnectionIdAndUser(c.getId(), user).size();
                    return PlaidConnectionResponse.from(c, accountCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets a single Plaid connection by ID for the authenticated user.
     */
    @Transactional(readOnly = true)
    public PlaidConnectionResponse getConnection(User user, Long connectionId) {
        PlaidConnection conn = plaidConnectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank connection not found or access denied"));

        int accountCount = accountRepository.findByPlaidConnectionIdAndUser(conn.getId(), user).size();
        return PlaidConnectionResponse.from(conn, accountCount);
    }

    /**
     * Manually triggers account and transaction synchronization for a connection.
     */
    @Transactional
    public SyncResponse syncConnection(User user, Long connectionId) {
        PlaidConnection conn = plaidConnectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank connection not found or access denied"));

        if (conn.getStatus() == PlaidConnectionStatus.DISCONNECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot synchronize a disconnected bank connection.");
        }

        // Sync accounts
        plaidAccountSyncService.syncAccounts(user, conn);

        // Sync transactions
        return plaidTransactionSyncService.syncTransactions(user, conn);
    }

    /**
     * Disconnects a Plaid Item connection. Preserves historical transaction records.
     */
    @Transactional
    public void disconnect(User user, Long connectionId) {
        PlaidConnection conn = plaidConnectionRepository.findByIdAndUser(connectionId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank connection not found or access denied"));

        log.info("Disconnecting Plaid connection ID {} for user ID {}", connectionId, user.getId());

        // Try to remove from Plaid
        try {
            ItemRemoveRequest removeRequest = new ItemRemoveRequest()
                    .accessToken(conn.getAccessToken());
            plaidApi.itemRemove(removeRequest).execute();
        } catch (Exception e) {
            log.warn("Could not remove item from Plaid API for connection {}: {}", connectionId, e.getMessage());
        }

        // Mark connection disconnected
        conn.setStatus(PlaidConnectionStatus.DISCONNECTED);
        plaidConnectionRepository.save(conn);

        // Mark associated accounts inactive
        List<Account> accounts = accountRepository.findByPlaidConnectionIdAndUser(connectionId, user);
        for (Account a : accounts) {
            a.setIsActive(false);
            accountRepository.save(a);
        }

        log.info("Successfully disconnected Plaid connection ID {}. Preserved {} transactions.",
                connectionId, conn.getId());
    }
}
