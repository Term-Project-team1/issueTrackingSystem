package issuetracker.service.account;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.repository.account.AccountRepository;

import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(String username, Role role) {
        validateUsername(username);
        validateRole(role);

        if (accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }

        Account account = new Account(null, username, role);
        return accountRepository.save(account);
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public List<Account> findByRole(Role role) {
        validateRole(role);
        return accountRepository.findByRole(role);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be empty.");
        }
    }

    private void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }
    }
}