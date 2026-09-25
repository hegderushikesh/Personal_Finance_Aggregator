package com.finpilot.plaid.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountSource;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.user.entity.User;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.AccountSubtype;
import com.plaid.client.model.AccountsGetRequest;
import com.plaid.client.model.AccountsGetResponse;
import com.plaid.client.request.PlaidApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import retrofit2.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaidAccountSyncService {

    private final PlaidApi plaidApi;
    private final AccountRepository accountRepository;

    @Transactional
    public List<Account> syncAccounts(User user, PlaidConnection connection) {
        log.info("Fetching Plaid accounts for connection ID {} (user ID {})", connection.getId(), user.getId());

        try {
            AccountsGetRequest request = new AccountsGetRequest()
                    .accessToken(connection.getAccessToken());

            Response<AccountsGetResponse> response = plaidApi.accountsGet(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                log.error("Failed to fetch Plaid accounts: code={}, error={}", response.code(), errorBody);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch accounts from Plaid: " + response.code());
            }

            List<AccountBase> plaidAccounts = response.body().getAccounts();
            List<Account> syncedAccounts = new ArrayList<>();

            for (AccountBase pa : plaidAccounts) {
                Account account = accountRepository.findByExternalAccountIdAndUser(pa.getAccountId(), user)
                        .orElseGet(() -> Account.builder()
                                .user(user)
                                .source(AccountSource.PLAID)
                                .externalAccountId(pa.getAccountId())
                                .plaidConnectionId(connection.getId())
                                .build());

                // Map balance
                Double rawBalance = pa.getBalances() != null && pa.getBalances().getCurrent() != null
                        ? pa.getBalances().getCurrent()
                        : (pa.getBalances() != null && pa.getBalances().getAvailable() != null ? pa.getBalances().getAvailable() : 0.0);
                BigDecimal balance = BigDecimal.valueOf(rawBalance);

                // Map type
                AccountType mappedType = mapAccountType(pa.getType(), pa.getSubtype());

                account.setName(pa.getName());
                account.setType(mappedType);
                account.setBalance(balance);
                account.setInstitutionName(connection.getInstitutionName());
                account.setAccountNumberLast4(pa.getMask());
                account.setIsActive(true);
                account.setSource(AccountSource.PLAID);
                account.setExternalAccountId(pa.getAccountId());
                account.setPlaidConnectionId(connection.getId());

                if (pa.getBalances() != null && pa.getBalances().getIsoCurrencyCode() != null) {
                    account.setCurrency(pa.getBalances().getIsoCurrencyCode());
                }

                Account saved = accountRepository.save(account);
                syncedAccounts.add(saved);
                log.info("Synced Plaid account: ID={}, Name={}, Type={}, Balance={}",
                        saved.getId(), saved.getName(), saved.getType(), saved.getBalance());
            }

            return syncedAccounts;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while syncing Plaid accounts for connection ID {}: {}", connection.getId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to synchronize Plaid accounts: " + e.getMessage());
        }
    }

    private AccountType mapAccountType(com.plaid.client.model.AccountType type, AccountSubtype subtype) {
        if (type == null) {
            return AccountType.CHECKING;
        }

        return switch (type) {
            case DEPOSITORY -> {
                if (subtype != null && (subtype == AccountSubtype.SAVINGS || subtype == AccountSubtype.MONEY_MARKET)) {
                    yield AccountType.SAVINGS;
                }
                yield AccountType.CHECKING;
            }
            case CREDIT -> AccountType.CREDIT_CARD;
            case INVESTMENT -> AccountType.INVESTMENT;
            case LOAN -> AccountType.OTHER;
            default -> AccountType.CHECKING;
        };
    }
}
