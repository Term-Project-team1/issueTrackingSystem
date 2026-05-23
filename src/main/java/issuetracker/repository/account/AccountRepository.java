package issuetracker.repository.account;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByUsername(String username);

    List<Account> findByRole(Role role);

    List<Account> findAll();

    boolean existsByUsername(String username);
}