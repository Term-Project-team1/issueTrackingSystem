package issuetracker.service.account;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;

import java.util.List;

public interface AccountService {
    Account createAccount(String username, Role role);

    List<Account> findAll();

    List<Account> findByRole(Role role);
}