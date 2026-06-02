package pk.jl.pasir_lebda_jan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pk.jl.pasir_lebda_jan.dto.LoginDto;
import pk.jl.pasir_lebda_jan.dto.TransactionDTO;
import pk.jl.pasir_lebda_jan.dto.UserDto;
import pk.jl.pasir_lebda_jan.repository.TransactionRepository;
import pk.jl.pasir_lebda_jan.repository.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla TransactionController.
 * Odwzorowują wszystkie zapytania z kolekcji Postman (Rys. 4.3 z instrukcji Lab03):
 *
 *  0. Setup     – rejestracja Jakuba i Tomka
 *  1. Auth      – logowanie Jakuba i Tomka
 *  2. POST      – dodawanie transakcji (INCOME, EXPENSE, bez logowania, błędne dane)
 *  3. GET       – pobieranie transakcji (wszystkie, bez logowania, po ID, cudza)
 *  4. PUT       – aktualizacja transakcji (właściciel, cudza, bez logowania)
 *  5. DELETE    – usuwanie transakcji (właściciel, cudza, bez logowania)
 *
 * Używa bazy H2 in-memory (profil "test") oraz @Transactional z rollbackiem po każdym teście.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // Dane testowe – odpowiadają użytkownikom "Jakub" i "Tomek" z Postmana
    // -----------------------------------------------------------------------
    private static final String JAKUB_USERNAME = "Jakub";
    private static final String JAKUB_EMAIL    = "jakub@pk.pl";
    private static final String JAKUB_PASSWORD = "Password123";

    private static final String TOMEK_USERNAME = "Tomek";
    private static final String TOMEK_EMAIL    = "tomek@pk.pl";
    private static final String TOMEK_PASSWORD = "Password456";

    // -----------------------------------------------------------------------
    // Setup / TearDown
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        transactionRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -----------------------------------------------------------------------
    // Metody pomocnicze
    // -----------------------------------------------------------------------

    /** Rejestruje użytkownika i zwraca token JWT uzyskany po zalogowaniu. */
    private String registerAndLogin(String username, String email, String password) throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setEmail(email);
        userDto.setPassword(password);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)));

        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    /** Buduje TransactionDTO z podanymi wartościami. */
    private TransactionDTO buildTransaction(Double amount, String type, String notes) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(amount);
        dto.setType(type);
        dto.setNotes(notes);
        dto.setTags("test");
        return dto;
    }

    /** Dodaje transakcję jako wskazany użytkownik i zwraca jej ID. */
    private Long createTransactionAsUser(String token, double amount, String type, String notes) throws Exception {
        TransactionDTO dto = buildTransaction(amount, type, notes);
        MvcResult result = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("id").asLong();
    }

    /** Buduje LoginDto. */
    private LoginDto buildLoginDto(String email, String password) {
        LoginDto dto = new LoginDto();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    // =======================================================================
    // 0. SETUP – Rejestracja użytkowników
    // =======================================================================

    @Test
    @Order(1)
    @DisplayName("0.1 Rejestruj Jakub – powinien zwrócić dane zarejestrowanego użytkownika")
    void shouldRegisterJakub() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername(JAKUB_USERNAME);
        userDto.setEmail(JAKUB_EMAIL);
        userDto.setPassword(JAKUB_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(JAKUB_USERNAME))
                .andExpect(jsonPath("$.email").value(JAKUB_EMAIL))
                .andExpect(jsonPath("$.password").value(not(JAKUB_PASSWORD)));
    }

    @Test
    @Order(2)
    @DisplayName("0.2 Rejestruj Tomek – powinien zwrócić dane zarejestrowanego użytkownika")
    void shouldRegisterTomek() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername(TOMEK_USERNAME);
        userDto.setEmail(TOMEK_EMAIL);
        userDto.setPassword(TOMEK_PASSWORD);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TOMEK_USERNAME))
                .andExpect(jsonPath("$.email").value(TOMEK_EMAIL));
    }

    // =======================================================================
    // 1. AUTH – Logowanie
    // =======================================================================

    @Test
    @Order(3)
    @DisplayName("1.1 Logowanie Jakub – powinien zwrócić poprawny token JWT")
    void shouldLoginJakub() throws Exception {
        registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD); // rejestracja

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginDto(JAKUB_EMAIL, JAKUB_PASSWORD))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value(
                        matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$")));
    }

    @Test
    @Order(4)
    @DisplayName("1.2 Logowanie Tomek – powinien zwrócić poprawny token JWT")
    void shouldLoginTomek() throws Exception {
        registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD); // rejestracja

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildLoginDto(TOMEK_EMAIL, TOMEK_PASSWORD))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").value(
                        matchesPattern("^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$")));
    }

    // =======================================================================
    // 2. POST – Dodawanie transakcji
    // =======================================================================

    @Test
    @Order(5)
    @DisplayName("2.1 Jakub - INCOME – powinien dodać transakcję przychodu")
    void shouldAddIncomeTransactionAsJakub() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        TransactionDTO dto = buildTransaction(1500.00, "INCOME", "Wynagrodzenie");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jakubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @Order(6)
    @DisplayName("2.2 Jakub - EXPENSE – powinien dodać transakcję wydatku")
    void shouldAddExpenseTransactionAsJakub() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        TransactionDTO dto = buildTransaction(200.00, "EXPENSE", "Zakupy spożywcze");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jakubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @Order(7)
    @DisplayName("2.3 Tomek - INCOME – powinien dodać transakcję przychodu dla Tomka")
    void shouldAddIncomeTransactionAsTomek() throws Exception {
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);
        TransactionDTO dto = buildTransaction(3000.00, "INCOME", "Premia");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + tomekToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @Order(8)
    @DisplayName("2.4 POST bez logowania – powinien zwrócić 401/403")
    void shouldReturn401WhenAddTransactionWithoutToken() throws Exception {
        TransactionDTO dto = buildTransaction(500.00, "INCOME", "Brak tokenu");

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(9)
    @DisplayName("2.5 POST z błędnymi danymi (walidacja) – powinien zwrócić 400")
    void shouldReturn400WhenAddTransactionWithInvalidData() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);

        // Brak wymaganego pola amount (null) – powinno nie przejść walidacji
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(null);
        dto.setType("");
        dto.setNotes("Brak kwoty");

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + jakubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // =======================================================================
    // 3. GET – Pobieranie transakcji
    // =======================================================================

    @Test
    @Order(10)
    @DisplayName("3.1 Pobierz wszystkie - Jakub – powinien zwrócić tylko transakcje Jakuba")
    void shouldGetAllTransactionsForJakub() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);

        createTransactionAsUser(jakubToken, 1000.0, "INCOME", "Jakub income");
        createTransactionAsUser(tomekToken, 2000.0, "INCOME", "Tomek income");

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + jakubToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notes").value("Jakub income"));
    }

    @Test
    @Order(11)
    @DisplayName("3.2 Pobierz wszystkie - Tomek – powinien zwrócić tylko transakcje Tomka")
    void shouldGetAllTransactionsForTomek() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);

        createTransactionAsUser(jakubToken, 1000.0, "INCOME", "Jakub income");
        createTransactionAsUser(tomekToken, 2000.0, "INCOME", "Tomek income");

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + tomekToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].notes").value("Tomek income"));
    }

    @Test
    @Order(12)
    @DisplayName("3.3 Pobierz BEZ logowania – powinien zwrócić 401/403")
    void shouldReturn401WhenGetTransactionsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andDo(print())
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @Test
    @Order(13)
    @DisplayName("3.4 Pobierz po ID - Jakub (właściciel) – powinien zwrócić transakcję")
    void shouldGetTransactionByIdAsOwner() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        Long id = createTransactionAsUser(jakubToken, 750.0, "EXPENSE", "Rachunek");

        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + jakubToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.notes").value("Rachunek"));
    }

    @Test
    @Order(14)
    @DisplayName("3.5 Pobierz transakcję Jakuba jako Tomek – powinien zwrócić 403")
    void shouldReturn403WhenGetJakubTransactionAsTomek() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);
        Long jakubId = createTransactionAsUser(jakubToken, 500.0, "INCOME", "Jakub private");

        mockMvc.perform(get("/api/transactions/" + jakubId)
                        .header("Authorization", "Bearer " + tomekToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // =======================================================================
    // 4. PUT – Aktualizacja transakcji
    // =======================================================================

    @Test
    @Order(15)
    @DisplayName("4.1 Zaktualizuj transakcję - Jakub (właściciel) – powinien zaktualizować")
    void shouldUpdateTransactionAsOwner() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        Long id = createTransactionAsUser(jakubToken, 100.0, "EXPENSE", "Stara notatka");

        TransactionDTO update = buildTransaction(999.99, "INCOME", "Zaktualizowana notatka");

        mockMvc.perform(put("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + jakubToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(999.99))
                .andExpect(jsonPath("$.notes").value("Zaktualizowana notatka"))
                .andExpect(jsonPath("$.type").value("INCOME"));
    }

    @Test
    @Order(16)
    @DisplayName("4.2 Aktualizacja transakcji Jakuba jako Tomek – powinien zwrócić 403")
    void shouldReturn403WhenUpdateJakubTransactionAsTomek() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);
        Long jakubId = createTransactionAsUser(jakubToken, 100.0, "EXPENSE", "Jakub expense");

        TransactionDTO update = buildTransaction(1.0, "INCOME", "Próba włamania");

        mockMvc.perform(put("/api/transactions/" + jakubId)
                        .header("Authorization", "Bearer " + tomekToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(17)
    @DisplayName("4.3 Aktualizacja BEZ logowania – powinien zwrócić 401/403")
    void shouldReturn401WhenUpdateTransactionWithoutToken() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        Long id = createTransactionAsUser(jakubToken, 100.0, "EXPENSE", "Notatka");

        TransactionDTO update = buildTransaction(50.0, "EXPENSE", "Bez tokenu");

        mockMvc.perform(put("/api/transactions/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andDo(print())
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    // =======================================================================
    // 5. DELETE – Usuwanie transakcji
    // =======================================================================

    @Test
    @Order(18)
    @DisplayName("5.1 Usuń transakcję - Jakub (właściciel) – powinien usunąć")
    void shouldDeleteTransactionAsOwner() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        Long id = createTransactionAsUser(jakubToken, 300.0, "EXPENSE", "Do usunięcia");

        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + jakubToken))
                .andDo(print())
                .andExpect(status().isOk());

        // Weryfikacja – transakcja nie powinna już istnieć
        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + jakubToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(19)
    @DisplayName("5.2 Usuń transakcję Jakuba jako Tomek – powinien zwrócić 403")
    void shouldReturn403WhenDeleteJakubTransactionAsTomek() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        String tomekToken = registerAndLogin(TOMEK_USERNAME, TOMEK_EMAIL, TOMEK_PASSWORD);
        Long jakubId = createTransactionAsUser(jakubToken, 300.0, "EXPENSE", "Jakub expense");

        mockMvc.perform(delete("/api/transactions/" + jakubId)
                        .header("Authorization", "Bearer " + tomekToken))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(20)
    @DisplayName("5.3 Usuń BEZ logowania – powinien zwrócić 401/403")
    void shouldReturn401WhenDeleteTransactionWithoutToken() throws Exception {
        String jakubToken = registerAndLogin(JAKUB_USERNAME, JAKUB_EMAIL, JAKUB_PASSWORD);
        Long id = createTransactionAsUser(jakubToken, 300.0, "EXPENSE", "Notatka");

        mockMvc.perform(delete("/api/transactions/" + id))
                .andDo(print())
                .andExpect(status().is(anyOf(is(401), is(403))));
    }

    @AfterEach
    void tearDown() {
        // @Transactional zapewnia rollback – opcjonalne czyszczenie manualne
    }
}