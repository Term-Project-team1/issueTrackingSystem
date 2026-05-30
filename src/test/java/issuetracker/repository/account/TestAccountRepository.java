package issuetracker.repository.account;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestAccountRepository implements AccountRepository {

    private final List<Account> accounts = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public Account save(Account account) {
        Account savedAccount = new Account(
                sequence++,
                account.getUsername(),
                account.getRole()
        );

        accounts.add(savedAccount);

        return savedAccount;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accounts.stream()
                .filter(account -> account.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return accounts.stream()
                .filter(account -> account.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public List<Account> findByRole(Role role) {
        return accounts.stream()
                .filter(account -> account.getRole() == role)
                .toList();
    }

    @Override
    public List<Account> findAll() {
        return accounts;
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}