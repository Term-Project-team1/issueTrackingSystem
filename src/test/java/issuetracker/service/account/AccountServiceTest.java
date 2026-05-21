package issuetracker.service.account;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.repository.account.AccountRepository;
import issuetracker.repository.account.AccountRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        AccountRepository accountRepository = new AccountRepositoryImpl();
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    @DisplayName("createAccount: 정상 생성")
    void createAccount_정상생성() {
        Account account = accountService.createAccount("testDev1", Role.DEV);

        assertNotNull(account);
        assertEquals("testDev1", account.getUsername());
        assertEquals(Role.DEV, account.getRole());
    }

    @Test
    @DisplayName("createAccount: username 중복이면 생성 실패")
    void createAccount_username이_중복이면_생성실패() {
        accountService.createAccount("duplicateDev", Role.DEV);

        assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount("duplicateDev", Role.DEV);
        });
    }

    @Test
    @DisplayName("createAccount: role이 null이면 생성 실패")
    void createAccount_role이_null이면_생성실패() {
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount("testDev2", null);
        });
    }

    @Test
    @DisplayName("findByRole: DEV 계정 목록 조회")
    void findByRole_DEV_계정목록조회() {
        accountService.createAccount("roleDev1", Role.DEV);
        accountService.createAccount("roleDev2", Role.DEV);
        accountService.createAccount("roleTester1", Role.TESTER);

        List<Account> devAccounts = accountService.findByRole(Role.DEV);

        assertTrue(devAccounts.stream().anyMatch(a -> a.getUsername().equals("roleDev1")));
        assertTrue(devAccounts.stream().anyMatch(a -> a.getUsername().equals("roleDev2")));
        assertTrue(devAccounts.stream().noneMatch(a -> a.getUsername().equals("roleTester1")));
        assertTrue(devAccounts.stream().allMatch(a -> a.getRole() == Role.DEV));
    }

    @Test
    @DisplayName("seedData: 데모 계정 존재 확인")
    void seedData_admin_PL_dev_tester_계정존재확인() {
        List<Account> accounts = accountService.findAll();

        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("admin")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("PL1")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("PL2")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("dev1")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("dev10")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("tester1")));
        assertTrue(accounts.stream().anyMatch(a -> a.getUsername().equals("tester5")));
    }
}