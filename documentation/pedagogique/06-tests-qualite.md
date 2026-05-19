# Tests et Qualité — Guide

## Pyramide des tests

```
        /\
       /  \      E2E Tests (Cypress)
      /    \     → Testent le système complet
     /──────\
    /        \   Integration Tests (Spring Boot Test)
   /          \  → Testent les couches ensemble
  /────────────\
 /              \ Unit Tests (JUnit 5 + Mockito)
/________________\ → Testent une classe isolée
```

| Type | Vitesse | Portée | Quantité |
|------|---------|--------|----------|
| Unitaire | ~1ms | Une classe | Beaucoup (70%) |
| Intégration | ~1s | Plusieurs couches | Modéré (20%) |
| E2E | ~10s | Système entier | Peu (10%) |

---

## 1. Tests unitaires (Backend)

### Outils
- **JUnit 5** : Framework de test
- **Mockito** : Mocking des dépendances
- **AssertJ** : Assertions fluides et lisibles

### Exemple : Tester TicketService

```java
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepositoryPort ticketRepository;

    @Mock
    private ProjectRepositoryPort projectRepository;

    @Mock
    private TicketHistoryRepositoryPort historyRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void create_shouldCreateTicketWithCorrectNumber() {
        // GIVEN
        UUID projectId = UUID.randomUUID();
        UUID reporterId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setKey("PROJ");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(ticketRepository.getNextNumber(projectId)).thenReturn(42L);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        // WHEN
        Ticket result = ticketService.create(projectId, "Fix login bug",
                "Description", "BUG", "HIGH", null, null, null,
                null, null, null, null, null, null, reporterId);

        // THEN
        assertThat(result.getTitle()).isEqualTo("Fix login bug");
        assertThat(result.getKey()).isEqualTo("PROJ");
        assertThat(result.getNumber()).isEqualTo(42L);
        assertThat(result.getFullKey()).isEqualTo("PROJ-42");
        assertThat(result.getStatus()).isEqualTo(TicketStatus.BACKLOG);

        verify(ticketRepository).save(any(Ticket.class));
        verify(historyRepository).save(any(TicketHistory.class));
    }

    @Test
    void create_shouldThrowWhenProjectNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.create(projectId, "Title",
                null, "STORY", null, null, null, null,
                null, null, null, null, null, null, UUID.randomUUID()))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Project");
    }

    @Test
    void transition_shouldRejectSameStatus() {
        UUID ticketId = UUID.randomUUID();
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.transition(ticketId,
                TicketStatus.IN_PROGRESS, UUID.randomUUID()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already in status");
    }
}
```

### Convention de nommage des tests

```
methodName_shouldExpectedBehavior_whenCondition()
```

Exemples :
- `create_shouldCreateTicket_whenValidData()`
- `transition_shouldThrow_whenSameStatus()`
- `getById_shouldReturnTicket_whenExists()`

---

## 2. Tests d'intégration (Backend)

### Outils
- **@SpringBootTest** : Charge le contexte Spring complet
- **TestContainers** : Lance un PostgreSQL en Docker pour les tests
- **MockMvc** : Teste les contrôleurs HTTP sans serveur réel

### Exemple : Test d'intégration du contrôleur

```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TicketControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void createTicket_shouldReturn201() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
            "New feature request", "Detailed description",
            "STORY", "MEDIUM", null, null, null, 5, null, null, null, null, null);

        mockMvc.perform(post("/tickets/project/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New feature request"))
            .andExpect(jsonPath("$.status").value("BACKLOG"))
            .andExpect(jsonPath("$.fullKey").value("PROJ-1"));
    }

    @Test
    void createTicket_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/tickets/project/{projectId}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void createTicket_shouldReturn400_whenTitleTooShort() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest(
            "Hi", null, "STORY", null, null, null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/tickets/project/{projectId}", projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
```

---

## 3. Tests du Repository (couche persistence)

```java
@DataJpaTest
@Testcontainers
class TicketRepositoryAdapterTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private JpaTicketRepository jpaRepository;

    private TicketRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TicketRepositoryAdapter(jpaRepository);
    }

    @Test
    void save_shouldPersistAndReturnTicket() {
        Ticket ticket = new Ticket(projectId, "PROJ", 1L, "Test", TicketType.STORY, TicketPriority.MEDIUM, reporterId);

        Ticket saved = adapter.save(ticket);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test");
    }

    @Test
    void findByProjectIdAndStatus_shouldFilterCorrectly() {
        // Setup : 3 tickets, 2 BACKLOG, 1 IN_PROGRESS
        // ...

        List<Ticket> backlogTickets = adapter.findByProjectIdAndStatus(projectId, TicketStatus.BACKLOG);

        assertThat(backlogTickets).hasSize(2);
    }
}
```

---

## 4. Tests Frontend (Angular)

### Outils
- **Jasmine** : Framework de test (assertions, mocks)
- **Karma** : Test runner dans le navigateur
- **Angular Testing Library** : Tests orientés utilisateur

### Exemple : Test d'un service

```typescript
describe('TicketService', () => {
  let service: TicketService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(TicketService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should fetch tickets by project', () => {
    const mockTickets: Ticket[] = [
      { id: '1', title: 'Bug fix', status: 'BACKLOG' } as Ticket
    ];

    service.getByProject('project-123').subscribe(tickets => {
      expect(tickets.length).toBe(1);
      expect(tickets[0].title).toBe('Bug fix');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/tickets/project/project-123`);
    expect(req.request.method).toBe('GET');
    req.flush(mockTickets);
  });
});
```

### Exemple : Test d'un composant

```typescript
describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: { login: jasmine.createSpy() } },
        { provide: Router, useValue: { navigate: jasmine.createSpy() } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should disable submit button when form is invalid', () => {
    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
  });

  it('should call login service on valid form submit', () => {
    component.email.set('user@test.com');
    component.password.set('password123');
    fixture.detectChanges();

    component.onSubmit();

    expect(inject(AuthService).login).toHaveBeenCalledWith('user@test.com', 'password123');
  });
});
```

---

## 5. Tests E2E (Cypress)

```typescript
// cypress/e2e/ticket-creation.cy.ts
describe('Ticket Creation', () => {
  beforeEach(() => {
    cy.login('developer@test.com', 'password');
    cy.visit('/board/project-id');
  });

  it('should create a ticket from backlog', () => {
    cy.get('[data-testid="new-ticket-btn"]').click();

    cy.get('[data-testid="ticket-title"]').type('Implement dark mode');
    cy.get('[data-testid="ticket-type"]').select('STORY');
    cy.get('[data-testid="ticket-priority"]').select('HIGH');
    cy.get('[data-testid="ticket-points"]').type('5');
    cy.get('[data-testid="submit-ticket"]').click();

    cy.get('.notification').should('contain', 'Ticket créé');
    cy.get('.backlog-column').should('contain', 'Implement dark mode');
  });

  it('should drag ticket between columns', () => {
    cy.get('[data-testid="ticket-PROJ-1"]')
      .drag('[data-testid="column-IN_PROGRESS"]');

    cy.get('[data-testid="column-IN_PROGRESS"]')
      .should('contain', 'PROJ-1');
  });
});
```

---

## 6. Lancer les tests

### Backend

```bash
# Tous les tests
mvn test

# Tests d'une classe spécifique
mvn test -Dtest=TicketServiceTest

# Tests avec couverture (JaCoCo)
mvn test jacoco:report
# Rapport dans target/site/jacoco/index.html

# Ignorer les tests (pour compilation rapide)
mvn compile -DskipTests
```

### Frontend

```bash
# Tests unitaires (Karma)
ng test

# Tests avec couverture
ng test --code-coverage
# Rapport dans coverage/index.html

# Tests E2E (Cypress)
npx cypress open     # Mode interactif
npx cypress run      # Mode headless (CI)
```

---

## 7. Métriques de qualité

| Métrique | Cible | Outil |
|----------|-------|-------|
| Couverture de code | > 80% | JaCoCo (backend), Istanbul (frontend) |
| Duplication | < 3% | SonarQube |
| Bugs | 0 bloquant | SonarQube |
| Vulnérabilités | 0 critique | SonarQube, OWASP |
| Complexité cyclomatique | < 15/méthode | SonarQube |
| Temps de build | < 5 minutes | CI/CD |

---

## 8. CI/CD Quality Gates

```yaml
# .github/workflows/ci.yml (exemple)
name: CI
on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: agileforge_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ['5432:5432']

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: mvn test -B
        working-directory: agileforge-backend
      - run: mvn jacoco:report
        working-directory: agileforge-backend
      - name: Check coverage threshold
        run: |
          COVERAGE=$(grep -oP 'Total.*?(\d+)%' target/site/jacoco/index.html | grep -oP '\d+')
          if [ "$COVERAGE" -lt 80 ]; then
            echo "Coverage $COVERAGE% is below 80% threshold"
            exit 1
          fi

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci
        working-directory: agileforge-frontend
      - run: ng test --watch=false --browsers=ChromeHeadless
        working-directory: agileforge-frontend
```

---

## Bonnes pratiques de test

1. **Nommer clairement** : Le nom du test doit décrire le comportement attendu
2. **AAA** : Arrange (setup) → Act (exécution) → Assert (vérification)
3. **Un test = un comportement** : Ne pas tester plusieurs choses dans un seul test
4. **Tests indépendants** : Chaque test doit pouvoir s'exécuter seul
5. **Pas de logique dans les tests** : Pas de if/for dans les tests, c'est un signe de test trop complexe
6. **Mocker à la frontière** : Mocker les ports (interfaces), pas les classes internes
7. **Tester les cas limites** : null, vide, valeurs extrêmes, erreurs
