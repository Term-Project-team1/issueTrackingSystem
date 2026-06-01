package issuetracker.controller;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.service.account.AccountService;
import java.util.List;

public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    public Account createAccount(String username, Role role) {
        return accountService.createAccount(username, role);
    }

    public List<Account> findAll() {
        return accountService.findAll();
    }

    public List<Account> findByRole(Role role) {
        return accountService.findByRole(role);
    }
}
