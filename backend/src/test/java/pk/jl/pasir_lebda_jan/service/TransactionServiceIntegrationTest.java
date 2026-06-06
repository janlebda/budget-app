package pk.jl.pasir_lebda_jan.service;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import pk.jl.pasir_lebda_jan.dto.TransactionDTO;
import pk.jl.pasir_lebda_jan.dto.BalanceDTO;
import pk.jl.pasir_lebda_jan.model.Transaction;
import pk.jl.pasir_lebda_jan.model.TransactionType;
import pk.jl.pasir_lebda_jan.model.User;
import pk.jl.pasir_lebda_jan.repository.TransactionRepository;
import pk.jl.pasir_lebda_jan.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("TestUser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsCurrentUser() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null));
    }

    @Test
    void shouldCalculateUserBalanceForPeriod() {
        LocalDateTime now = LocalDateTime.now();

        Transaction recentIncome = new Transaction();
        recentIncome.setAmount(100.0);
        recentIncome.setType(TransactionType.INCOME);
        recentIncome.setTags("salary");
        recentIncome.setNotes("Monthly salary");
        recentIncome.setTimestamp(now.minusDays(1));
        recentIncome.setUser(testUser);

        Transaction oldExpense = new Transaction();
        oldExpense.setAmount(20.0);
        oldExpense.setType(TransactionType.EXPENSE);
        oldExpense.setTags("coffee");
        oldExpense.setNotes("Coffee shop");
        oldExpense.setTimestamp(now.minusDays(10));
        oldExpense.setUser(testUser);

        transactionRepository.saveAll(List.of(recentIncome, oldExpense));

        BalanceDTO balance = transactionService.getUserBalance(testUser, 7f);

        assertEquals(100.0, balance.getTotalIncome());
        assertEquals(0.0, balance.getTotalExpense());
        assertEquals(100.0, balance.getBalance());
    }

    @Test
    void shouldCreateTransactionWhenAuthenticated() {
        authenticateAsCurrentUser();

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(42.5);
        transactionDTO.setType("INCOME");
        transactionDTO.setTags("test-tag");
        transactionDTO.setNotes("Test note");

        Transaction savedTransaction = transactionService.createTransaction(transactionDTO);

        assertNotNull(savedTransaction.getId());
        assertEquals(42.5, savedTransaction.getAmount());
        assertEquals(TransactionType.INCOME, savedTransaction.getType());
        assertEquals(testUser.getId(), savedTransaction.getUser().getId());
        assertNotNull(savedTransaction.getTimestamp());
    }
}
