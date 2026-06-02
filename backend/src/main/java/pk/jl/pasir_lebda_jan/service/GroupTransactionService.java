package pk.jl.pasir_lebda_jan.service;

import org.springframework.stereotype.Service;
import pk.jl.pasir_lebda_jan.dto.GroupTransactionDTO;
import pk.jl.pasir_lebda_jan.exception.EntityNotFoundException;
import pk.jl.pasir_lebda_jan.dto.GroupNotificationDTO;
import pk.jl.pasir_lebda_jan.model.*;
import pk.jl.pasir_lebda_jan.repository.DebtRepository;
import pk.jl.pasir_lebda_jan.repository.GroupRepository;
import pk.jl.pasir_lebda_jan.repository.MembershipRepository;
import pk.jl.pasir_lebda_jan.repository.TransactionRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupTransactionService {

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final DebtRepository debtRepository;
    private final MembershipService membershipService;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    public GroupTransactionService(
            GroupRepository groupRepository,
            MembershipRepository membershipRepository,
            DebtRepository debtRepository,
            MembershipService membershipService,
            TransactionRepository transactionRepository,
            NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.debtRepository = debtRepository;
        this.membershipService = membershipService;
        this.transactionRepository = transactionRepository;
        this.notificationService = notificationService;
    }

    public void addGroupTransaction(GroupTransactionDTO transactionDTO, User currentUser) {
        Group group = groupRepository.findById(transactionDTO.getGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Nie znaleziono Grupy"));

        membershipService.assertCurrentUserIsGroupMember(group.getId());

        List<Membership> members = membershipRepository.findByGroupId(group.getId());
List<Membership> selectedMembers = selectParticipants(transactionDTO, members, currentUser);
if (selectedMembers.isEmpty()) {
throw new IllegalStateException("Grupa nie ma czlonkow, nie mozna dodac transakcji.");
}

// 1. Dodaj pełną transakcję dla płacącego (aby bilans się zgadzał)
Transaction transaction = new Transaction();
transaction.setAmount(transactionDTO.getAmount());
transaction.setType("EXPENSE".equals(transactionDTO.getType()) ? TransactionType.EXPENSE : TransactionType.INCOME);
transaction.setUser(currentUser);
transaction.setTimestamp(LocalDateTime.now());
transaction.setNotes("Wydatek grupowy: " + transactionDTO.getTitle() + " w grupie " + group.getName());
transaction.setTags("Group");
transactionRepository.save(transaction);

double amountPerUser = transactionDTO.getAmount() / selectedMembers.size();
boolean expense = "EXPENSE".equals(transactionDTO.getType());
for (Membership member : selectedMembers){
User otherUser = member.getUser();
if (!otherUser.getId().equals(currentUser.getId())){
Debt debt = new Debt();
debt.setDebtor(expense ? otherUser : currentUser);
debt.setCreditor(expense ? currentUser : otherUser);
debt.setGroup(group);
debt.setAmount(amountPerUser);
debt.setTitle(transactionDTO.getTitle());
debtRepository.save(debt);

// 2. Wyślij powiadomienie do uczestnika
if (expense) {
    GroupNotificationDTO notification = GroupNotificationDTO.builder()
            .type("GROUP_EXPENSE_ADDED")
            .groupId(group.getId())
            .groupName(group.getName())
            .title(transactionDTO.getTitle())
            .amount(transactionDTO.getAmount())
            .userShare(amountPerUser)
            .createdByEmail(currentUser.getEmail())
            .message(String.format("%s dodał wydatek \"%s\" w grupie %s. Twoja część: %.2f zł.",
                    currentUser.getEmail(), transactionDTO.getTitle(), group.getName(), amountPerUser))
            .build();
    notificationService.sendNotification(otherUser.getEmail(), notification);
}
}
}
    }
    private List<Membership> selectParticipants(
GroupTransactionDTO transactionDTO,
List<Membership> members,
User currentUser) {
List<Long> selectedUserIds = transactionDTO.getSelectedUserIds();
if (selectedUserIds == null || selectedUserIds.isEmpty()) {
return members;
}
Set<Long> uniqueSelectedUserIds = new HashSet<>(selectedUserIds);
List<Membership> selectedMembers = members.stream()
.filter(membership -> uniqueSelectedUserIds.contains(membership.getUser().getId()))
.toList();
if (selectedMembers.size() != uniqueSelectedUserIds.size()) {
throw new IllegalStateException(
"Wszyscy wybrani uzytkownicy musza byc czlonkami grupy.");
}
boolean currentUserSelected = selectedMembers.stream()
.anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
if (!currentUserSelected) {
throw new IllegalStateException(
"Aktualny uzytkownik musi byc uczestnikiem transakcji grupowej.");
}
if (selectedMembers.size() < 2) {
throw new IllegalStateException("Transakcja grupowa wymaga co najmniej dwoch uczestnikow.");
}
return selectedMembers;
}
}
